package com.mahitotsu.synerdesk.invoker;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mahitotsu.synerdesk.definition.AgentDefinition;
import com.mahitotsu.synerdesk.definition.AgentDefinitionLookup;
import com.mahitotsu.synerdesk.definition.ReturnControlToolDefinition;
import com.mahitotsu.synerdesk.definition.ToolDefinitionLookup;
import com.mahitotsu.synerdesk.definition.ToolInstanceLookup;

import software.amazon.awssdk.services.bedrockagentruntime.BedrockAgentRuntimeAsyncClient;
import software.amazon.awssdk.services.bedrockagentruntime.model.AgentActionGroup;
import software.amazon.awssdk.services.bedrockagentruntime.model.Collaborator;
import software.amazon.awssdk.services.bedrockagentruntime.model.CollaboratorConfiguration;
import software.amazon.awssdk.services.bedrockagentruntime.model.ContentBody;
import software.amazon.awssdk.services.bedrockagentruntime.model.FunctionInvocationInput;
import software.amazon.awssdk.services.bedrockagentruntime.model.FunctionResult;
import software.amazon.awssdk.services.bedrockagentruntime.model.InlineSessionState;
import software.amazon.awssdk.services.bedrockagentruntime.model.InvocationInputMember;
import software.amazon.awssdk.services.bedrockagentruntime.model.InvocationResultMember;
import software.amazon.awssdk.services.bedrockagentruntime.model.InvokeInlineAgentRequest;
import software.amazon.awssdk.services.bedrockagentruntime.model.InvokeInlineAgentResponseHandler;
import software.amazon.awssdk.services.bedrockagentruntime.model.InvokeInlineAgentResponseHandler.Visitor;
import software.amazon.awssdk.services.bedrockagentruntime.model.ResponseState;

@Service
public class BedrockInlineAgentService {

    @Autowired
    private BedrockAgentRuntimeAsyncClient bedrockAgentRuntimeAsyncClient;

    @Autowired
    private AgentDefinitionLookup agentDefinitionLookup;

    @Autowired
    private ToolDefinitionLookup toolDefinitionLookup;

    @Autowired
    private ToolInstanceLookup toolInstanceLookup;

    @Autowired
    private ObjectMapper objectMapper;

    public String[] invoke(final String agentName, final String... prompts) {
        return this.invoke(agentName, null, prompts);
    }

    public String[] invoke(final String agentName, final UUID sessionId, final String... prompts) {

        final UUID sessionIdToUse = sessionId == null ? UUID.randomUUID() : sessionId;
        final List<String> answers = new ArrayList<>();
        for (int i = 0; i < prompts.length; i++) {
            answers.add(this.invoke(agentName, sessionIdToUse, prompts[i]));
        }
        return answers.toArray(new String[answers.size()]);
    }

    public String invoke(final String agentName, final String prompt) {
        return this.invoke(agentName, null, prompt);
    }

    public String invoke(final String agentName, final UUID sessionId, final String prompt) {

        final AgentDefinition agentDefinition = this.agentDefinitionLookup.getAgentDefinition(agentName);
        if (agentDefinition == null) {
            throw new IllegalArgumentException(
                    "No available agent found with the specified name. agentName: " + agentName);
        }

        final String sessionIdStr = sessionId == null ? UUID.randomUUID().toString() : sessionId.toString();
        final StringBuilder outputText = new StringBuilder();
        final List<InlineSessionState> sessionState = new ArrayList<>(1);

        sessionState.add(null);
        do {
            this.bedrockAgentRuntimeAsyncClient.invokeInlineAgent(
                    this.buildInlineAgentRequest(agentDefinition, sessionIdStr, prompt,
                            sessionState.get(0)),
                    this.buildInvokeInlineAgentResponseHandler(outputText, sessionState))
                    .join();
        } while (outputText.length() == 0);

        return outputText.toString();
    }

    private InvokeInlineAgentRequest buildInlineAgentRequest(final AgentDefinition agentDefinition,
            final String sessionId, final String inputText, final InlineSessionState sessionState) {

        final Collection<CollaboratorConfiguration> collaboratorConfigurations = agentDefinition
                .getCollaboratorConfigurations();
        final Collection<Collaborator> collaborators = collaboratorConfigurations.stream()
                .map(cc -> this.agentDefinitionLookup.getAgentDefinition(cc.collaboratorName()))
                .filter(ad -> ad != null)
                .map(ad -> this.buildCollaborator(ad)).toList();

        return InvokeInlineAgentRequest.builder()
                .foundationModel(agentDefinition.getFoundationModel())
                .instruction(agentDefinition.getInstruction())
                .actionGroups(this.lookupAgentAcctionGroups(agentDefinition.getActionGroupNames()))
                .collaboratorConfigurations(collaboratorConfigurations)
                .collaborators(collaborators)
                .agentCollaboration(agentDefinition.getAgentCollaboration())
                .sessionId(sessionId)
                .inlineSessionState(sessionState)
                .inputText(sessionState == null ? inputText : null)
                .enableTrace(false)
                .build();
    }

    private Collection<AgentActionGroup> lookupAgentAcctionGroups(final Collection<String> actionGroupNames) {

        return actionGroupNames.stream()
                .map(name -> Optional.ofNullable(this.toolDefinitionLookup.getToolDefinition(name))
                        .map(td -> td.getAgentActionGroup()).orElse(null))
                .filter(ag -> ag != null)
                .toList();
    }

    private InvokeInlineAgentResponseHandler buildInvokeInlineAgentResponseHandler(final StringBuilder outputText,
            final List<InlineSessionState> sessionState) {

        return InvokeInlineAgentResponseHandler.builder()
                .onEventStream(publisher -> publisher.subscribe(event -> event.accept(Visitor.builder()
                        .onChunk(c -> outputText.append(c.bytes().asString(Charset.defaultCharset())))
                        .onReturnControl(c -> {
                            final List<InvocationResultMember> invocationResultMembers = c.invocationInputs().stream()
                                    .map(member -> this.processInvocationInput(member)).toList();
                            sessionState.set(0, InlineSessionState.builder()
                                    .invocationId(c.invocationId())
                                    .returnControlInvocationResults(invocationResultMembers)
                                    .build());
                        })
                        .build())))
                .build();
    }

    private Collaborator buildCollaborator(final AgentDefinition agentDefinition) {

        return Collaborator.builder()
                .agentName(agentDefinition.getAgentName())
                .foundationModel(agentDefinition.getFoundationModel())
                .instruction(agentDefinition.getInstruction())
                .actionGroups(this.lookupAgentAcctionGroups(agentDefinition.getActionGroupNames()))
                .build();
    }

    private InvocationResultMember processInvocationInput(final InvocationInputMember inputMember) {

        final FunctionInvocationInput input = inputMember.functionInvocationInput();
        final ReturnControlToolDefinition<?> toolDefinition = ReturnControlToolDefinition.class
                .cast(this.toolDefinitionLookup
                        .getToolDefinition(input.actionGroup()));

        String result;
        ResponseState responseState;
        try {
            result = this.objectMapper
                    .writeValueAsString(
                            toolDefinition.invokeFunction(this.toolInstanceLookup, input));
            responseState = null;
        } catch (final Exception e) {
            result = e.toString();
            responseState = ResponseState.FAILURE;
        }

        final InvocationResultMember invocationResultMember = InvocationResultMember.builder()
                .functionResult(FunctionResult.builder()
                        .agentId(input.agentId())
                        .actionGroup(input.actionGroup())
                        .function(input.function())
                        .responseState(responseState)
                        .responseBody(Collections.singletonMap("TEXT",
                                ContentBody.builder().body(result).build()))
                        .build())
                .build();

        return invocationResultMember;
    }
}

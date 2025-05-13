package com.mahitotsu.synerdesk.invoker;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
import software.amazon.awssdk.services.bedrockagentruntime.model.InlineAgentReturnControlPayload;
import software.amazon.awssdk.services.bedrockagentruntime.model.InlineSessionState;
import software.amazon.awssdk.services.bedrockagentruntime.model.InvocationInputMember;
import software.amazon.awssdk.services.bedrockagentruntime.model.InvocationResultMember;
import software.amazon.awssdk.services.bedrockagentruntime.model.InvokeInlineAgentRequest;
import software.amazon.awssdk.services.bedrockagentruntime.model.InvokeInlineAgentResponseHandler;
import software.amazon.awssdk.services.bedrockagentruntime.model.InvokeInlineAgentResponseHandler.Visitor;
import software.amazon.awssdk.services.bedrockagentruntime.model.ResponseState;

@Service
public class BedrockInlineAgentService {

        private Logger logger = LoggerFactory.getLogger(this.getClass());

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

        public String invoke(final String agentName, final String prompt) {

                final AgentDefinition agentDefinition = this.agentDefinitionLookup.getAgentDefinition(agentName);
                if (agentDefinition == null) {
                        throw new IllegalArgumentException(
                                        "No available agent found with the specified name. agentName: " + agentName);
                }

                final String sessionId = UUID.randomUUID().toString();
                final StringBuilder outputText = new StringBuilder();

                final List<InlineSessionState> sessionState = new ArrayList<>(1);
                final List<InlineAgentReturnControlPayload> returnControlPayloads = new ArrayList<>();
                final List<InvocationResultMember> invocationResultMembers = new ArrayList<>();

                sessionState.add(null);
                do {
                        returnControlPayloads.clear();
                        this.bedrockAgentRuntimeAsyncClient.invokeInlineAgent(
                                        this.buildInlineAgentRequest(agentDefinition, sessionId, prompt,
                                                        sessionState.get(0)),
                                        this.buildInvokeInlineAgentResponseHandler(outputText, returnControlPayloads))
                                        .join();

                        invocationResultMembers.clear();
                        for (final InlineAgentReturnControlPayload payload : returnControlPayloads) {
                                for (InvocationInputMember inputMember : payload.invocationInputs()) {
                                        invocationResultMembers.add(this.processInvocationInput(inputMember));
                                }
                        }
                        if (returnControlPayloads.size() > 0) {
                                sessionState.set(0, InlineSessionState.builder()
                                                .invocationId(returnControlPayloads.get(0).invocationId())
                                                .returnControlInvocationResults(invocationResultMembers)
                                                .build());
                        }
                } while (returnControlPayloads.size() > 0);

                this.logger.info("Final answer: " + outputText.toString());
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
                        final Collection<InlineAgentReturnControlPayload> inlineAgentReturnControlPayloads) {

                return InvokeInlineAgentResponseHandler.builder()
                                .onEventStream(publisher -> publisher.subscribe(event -> event.accept(Visitor.builder()
                                                .onChunk(c -> outputText
                                                                .append(c.bytes().asString(Charset.defaultCharset())))
                                                .onReturnControl(c -> inlineAgentReturnControlPayloads.add(c))
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

                this.logger.info("Process the return control payload. payload: " + inputMember);

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
                        this.logger.error("Failed to invoke the tool.", e);
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

                this.logger.info("Process the return control payload. result: " + invocationResultMember);
                return invocationResultMember;
        }
}

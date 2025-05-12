package com.mahitotsu.synerdesk.invoker;

import java.nio.charset.Charset;
import java.util.Collection;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.mahitotsu.synerdesk.definition.AgentDefinition;
import com.mahitotsu.synerdesk.definition.AgentDefinitionLookup;

import software.amazon.awssdk.services.bedrockagentruntime.BedrockAgentRuntimeAsyncClient;
import software.amazon.awssdk.services.bedrockagentruntime.model.Collaborator;
import software.amazon.awssdk.services.bedrockagentruntime.model.CollaboratorConfiguration;
import software.amazon.awssdk.services.bedrockagentruntime.model.InvokeInlineAgentRequest;
import software.amazon.awssdk.services.bedrockagentruntime.model.InvokeInlineAgentResponseHandler;
import software.amazon.awssdk.services.bedrockagentruntime.model.InvokeInlineAgentResponseHandler.Visitor;

@Service
public class BedrockInlineAgentService {

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private BedrockAgentRuntimeAsyncClient bedrockAgentRuntimeAsyncClient;

    @Autowired
    private AgentDefinitionLookup agentDefinitionLookup;

    public String invoke(final String agentName, final String prompt) {

        final AgentDefinition agentDefinition = this.agentDefinitionLookup.getAgentDefinition(agentName);
        if (agentDefinition == null) {
            throw new IllegalArgumentException(
                    "No available agent found with the specified name. agentName: " + agentName);
        }

        final String sessionId = UUID.randomUUID().toString();
        final StringBuilder outputText = new StringBuilder();

        do {
            this.bedrockAgentRuntimeAsyncClient.invokeInlineAgent(
                    this.buildInlineAgentRequest(agentDefinition, sessionId, prompt),
                    this.buildInvokeInlineAgentResponseHandler(outputText)).join();
        } while (false);

        this.logger.info("Final answer: " + outputText.toString());
        return outputText.toString();
    }

    private InvokeInlineAgentRequest buildInlineAgentRequest(final AgentDefinition agentDefinition,
            final String sessionId, final String inputText) {

        final Collection<CollaboratorConfiguration> collaboratorConfigurations = agentDefinition
                .getCollaboratorConfigurations();
        final Collection<Collaborator> collaborators = collaboratorConfigurations.stream()
                .map(cc -> this.agentDefinitionLookup.getAgentDefinition(cc.collaboratorName()))
                .map(ad -> this.buildCollaborator(ad)).toList();

        return InvokeInlineAgentRequest.builder()
                .foundationModel(agentDefinition.getFoundationModel())
                .instruction(agentDefinition.getInstruction())
                .actionGroups(agentDefinition.getActionGroups())
                .collaboratorConfigurations(collaboratorConfigurations)
                .collaborators(collaborators)
                .agentCollaboration(agentDefinition.getAgentCollaboration())
                .sessionId(sessionId)
                .inputText(inputText)
                .build();
    }

    private InvokeInlineAgentResponseHandler buildInvokeInlineAgentResponseHandler(final StringBuilder outputText) {

        return InvokeInlineAgentResponseHandler.builder()
                .onEventStream(publisher -> publisher.subscribe(event -> event.accept(Visitor.builder()
                        .onChunk(c -> outputText.append(c.bytes().asString(Charset.defaultCharset())))
                        .build())))
                .build();
    }

    private Collaborator buildCollaborator(final AgentDefinition agentDefinition) {

        return Collaborator.builder()
                .agentName(agentDefinition.getAgentName())
                .foundationModel(agentDefinition.getFoundationModel())
                .instruction(agentDefinition.getInstruction())
                .actionGroups(agentDefinition.getActionGroups())
                .build();
    }
}

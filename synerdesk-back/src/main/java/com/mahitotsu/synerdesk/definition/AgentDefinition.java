package com.mahitotsu.synerdesk.definition;

import java.util.Collection;
import java.util.Collections;

import software.amazon.awssdk.services.bedrockagentruntime.model.AgentCollaboration;
import software.amazon.awssdk.services.bedrockagentruntime.model.CollaboratorConfiguration;

public interface AgentDefinition {

    default String getAgentName() {
        return this.getClass().getSimpleName();
    }

    default String getFoundationModel() {
        return "apac.amazon.nova-micro-v1:0";
    }

    String getInstruction();

    default Collection<String> getActionGroupNames() {
        return Collections.emptySet();
    }

    default Collection<CollaboratorConfiguration> getCollaboratorConfigurations() {
        return Collections.emptySet();
    }

    default AgentCollaboration getAgentCollaboration() {
        return this.getCollaboratorConfigurations().isEmpty()
                ? AgentCollaboration.DISABLED
                : AgentCollaboration.SUPERVISOR;
    }
}

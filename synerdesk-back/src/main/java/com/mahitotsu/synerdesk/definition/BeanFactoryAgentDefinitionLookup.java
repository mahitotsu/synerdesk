package com.mahitotsu.synerdesk.definition;

import java.util.Collection;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.ObjectUtils;

public class BeanFactoryAgentDefinitionLookup implements AgentDefinitionLookup {

    @Autowired(required = false)
    private Collection<AgentDefinition> agentDefinitions;

    @Override
    public AgentDefinition getAgentDefinition(final String agentName) {
        return this.agentDefinitions == null ? null
                : this.agentDefinitions.stream()
                        .filter(def -> ObjectUtils.nullSafeEquals(agentName, def.getAgentName()))
                        .findFirst().orElse(null);
    }
}

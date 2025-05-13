package com.mahitotsu.synerdesk.definition;

import java.util.Collection;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.ObjectUtils;

public class BeanFactoryToolDefinitionLookup implements ToolDefinitionLookup {

    @Autowired(required = false)
    private Collection<ToolDefinition> toolDefinitions;

    @Override
    public ToolDefinition getToolDefinition(final String toolName) {
        return this.toolDefinitions == null ? null
                : this.toolDefinitions.stream()
                        .filter(def -> ObjectUtils.nullSafeEquals(toolName,
                                Optional.ofNullable(def.getAgentActionGroup().actionGroupName()).orElse(null)))
                        .findFirst().orElse(null);
    }
}

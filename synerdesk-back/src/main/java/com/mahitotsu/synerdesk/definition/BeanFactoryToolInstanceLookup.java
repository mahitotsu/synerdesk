package com.mahitotsu.synerdesk.definition;

import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.BeanNotOfRequiredTypeException;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.annotation.Autowired;

public class BeanFactoryToolInstanceLookup implements ToolInstanceLookup {

    @Autowired(required = false)
    private Set<ReturnControlToolDefinition<?>> toolDefinitions;

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Override
    public <T> T getToolInstance(final String actionGroupName, final Class<T> toolApi) {
        try {
            return this.toolDefinitions == null ? null
                    : this.toolDefinitions.stream()
                            .filter(td -> toolApi.isAssignableFrom(td.getClass()))
                            .filter(td -> td.getAgentActionGroup().actionGroupName().equals(actionGroupName))
                            .map(td -> toolApi.cast(td))
                            .findFirst().orElse(null);
        } catch (NoSuchBeanDefinitionException | BeanNotOfRequiredTypeException e) {
            this.logger.error("The required type bean is not found.", e);
            return null;
        }
    }

}

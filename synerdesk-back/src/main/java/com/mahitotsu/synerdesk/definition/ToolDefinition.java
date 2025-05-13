package com.mahitotsu.synerdesk.definition;

import software.amazon.awssdk.services.bedrockagentruntime.model.AgentActionGroup;

public interface ToolDefinition {
    
    AgentActionGroup getAgentActionGroup();
}

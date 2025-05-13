package com.mahitotsu.synerdesk.definition;

import software.amazon.awssdk.services.bedrockagentruntime.model.FunctionInvocationInput;

public interface ReturnControlToolDefinition<T> extends ToolDefinition {

    Object invokeFunction(ToolInstanceLookup toolInstanceLookup, FunctionInvocationInput functionInvocationInput)
            throws Exception;
}

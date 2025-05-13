package com.mahitotsu.synerdesk.definition;

public interface ToolInstanceLookup {

    <T> T getToolInstance(String actionGroupName, Class<T> toolApi);
}

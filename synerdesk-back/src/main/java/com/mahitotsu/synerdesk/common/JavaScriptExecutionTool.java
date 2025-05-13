package com.mahitotsu.synerdesk.common;

import org.springframework.stereotype.Component;
import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.Value;
import org.graalvm.polyglot.io.IOAccess;

import com.mahitotsu.synerdesk.definition.DefaultReturnControlToolDefinition;

@Component
public class JavaScriptExecutionTool extends DefaultReturnControlToolDefinition<JavaScriptExecutor>
        implements JavaScriptExecutor {

    protected JavaScriptExecutionTool() {
        super(JavaScriptExecutor.class, "JavascriptExecutor");
    }

    @Override
    public String execute(String script) throws Exception {
        try {
            final Value result = Context.newBuilder("python")
                    .allowIO(IOAccess.ALL)
                    .allowAllAccess(true)
                    .build()
                    .eval("python", script);
            return result == null ? null : result.asString();
        } catch (Exception e) {
            throw new IllegalStateException("An error occurred while running the script.", e);
        }
    }

}

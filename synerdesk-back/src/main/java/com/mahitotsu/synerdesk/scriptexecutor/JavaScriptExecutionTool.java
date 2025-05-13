package com.mahitotsu.synerdesk.scriptexecutor;

import java.io.ByteArrayOutputStream;
import java.nio.charset.Charset;

import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.io.IOAccess;
import org.springframework.stereotype.Component;

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
            final ByteArrayOutputStream out = new ByteArrayOutputStream();
            Context.newBuilder("js")
                    .allowIO(IOAccess.ALL)
                    .allowAllAccess(true)
                    .out(out)
                    .build()
                    .eval("js", script);
            return new String(out.toByteArray(), Charset.defaultCharset());
        } catch (Exception e) {
            throw new IllegalStateException("An error occurred while running the script.", e);
        }
    }

}

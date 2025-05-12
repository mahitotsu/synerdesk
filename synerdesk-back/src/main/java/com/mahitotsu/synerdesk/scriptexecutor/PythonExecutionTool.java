package com.mahitotsu.synerdesk.scriptexecutor;

import java.io.ByteArrayOutputStream;
import java.nio.charset.Charset;

import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.io.IOAccess;
import org.springframework.stereotype.Component;

import com.mahitotsu.synerdesk.definition.DefaultReturnControlToolDefinition;

@Component
public class PythonExecutionTool extends DefaultReturnControlToolDefinition<JavaScriptExecutor>
        implements JavaScriptExecutor {

    protected PythonExecutionTool() {
        super(JavaScriptExecutor.class, "PythonExecutor");
    }

    @Override
    public String execute(String script) throws Exception {
        try {
            final ByteArrayOutputStream out = new ByteArrayOutputStream();
            Context.newBuilder("python")
                    .allowIO(IOAccess.ALL)
                    .allowAllAccess(true)
                    .out(out)
                    .build()
                    .eval("python", script);
            return new String(out.toByteArray(), Charset.defaultCharset());
        } catch (Exception e) {
            throw new IllegalStateException("An error occurred while running the script.", e);
        }
    }

}

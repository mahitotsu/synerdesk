package com.mahitotsu.synerdesk.scriptexecutor;

import java.util.Arrays;
import java.util.Collection;

import org.springframework.stereotype.Component;

import com.mahitotsu.synerdesk.definition.AgentDefinition;

@Component
public class ScriptExecutionAgent implements AgentDefinition {

    @Override
    public String getInstruction() {
        return """
                スクリプトを実行して結果を返すことが出来るエージェントです。
                以下の作業をサポートします。
                * 指定されたスクリプトを実行し手結果を返す。
                * 要求に応えるためのスクリプトを生成して実行し結果を返す。

                以下のプログラム言語をサポートします。
                * javascript
                * python
                * SQL
                """;
    }

    @Override
    public Collection<String> getActionGroupNames() {
        return Arrays.asList("JavascriptExecutor", "PythonExecutor", "SqlExecutor");
    }

}

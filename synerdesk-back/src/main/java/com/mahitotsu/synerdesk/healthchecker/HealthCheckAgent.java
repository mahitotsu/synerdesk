package com.mahitotsu.synerdesk.healthchecker;

import org.springframework.stereotype.Component;

import com.mahitotsu.synerdesk.definition.AgentDefinition;

@Component
public class HealthCheckAgent implements AgentDefinition {

    @Override
    public String getInstruction() {
        return """
            あなたはシステムの健全性をチェックして応答することが出来るエージェントです。
            ユーザーの要求に従って必要な項目の健全性をすべてチェックした後に、全ての項目の結果を香料して最終的な健全性を判断します。
            ユーザーの要求が不明確なため回答できないと判断した場合は、ユーザーが明確にすべき内容を最終的な回答として返します。
                """;
    }
}

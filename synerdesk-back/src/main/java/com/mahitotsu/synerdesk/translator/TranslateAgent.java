package com.mahitotsu.synerdesk.translator;

import org.springframework.stereotype.Component;

import com.mahitotsu.synerdesk.definition.AgentDefinition;

@Component
public class TranslateAgent implements AgentDefinition {

    @Override
    public String getInstruction() {
        return """
                あなたは優秀な翻訳者です。
                指定された文章を可能な限り自然な文体で指定された言語に翻訳して返します。
                翻訳結果のみを回答します。
                ユーザーの要求が不明確なため回答できないと判断した場合は、ユーザーが明確にすべき内容を最終的な回答として返します。
                """;
    }

}

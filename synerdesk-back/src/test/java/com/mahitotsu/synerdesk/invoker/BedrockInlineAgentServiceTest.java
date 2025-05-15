package com.mahitotsu.synerdesk.invoker;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.mahitotsu.synerdesk.TestMain;

public class BedrockInlineAgentServiceTest extends TestMain {

    @Autowired
    private BedrockInlineAgentService bedrockInlineAgentService;

    @Test
    public void testInvoke() {
        final String answer = this.bedrockInlineAgentService.invoke("ScriptExecutionAgent",
                "登録済みのユーザーを教えてください。ユーザーが格納されているテーブルはツールを使って探してください。");
        System.out.println(answer);
    }
}

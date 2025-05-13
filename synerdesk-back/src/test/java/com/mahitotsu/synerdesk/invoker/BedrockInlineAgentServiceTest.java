package com.mahitotsu.synerdesk.invoker;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class BedrockInlineAgentServiceTest {

    @Autowired
    private BedrockInlineAgentService bedrockInlineAgentService;

    @Test
    public void testServiceDesk() {
        this.bedrockInlineAgentService.invoke("ScriptExecutionAgent", "今日から100日後の日付を教えてください。");
    }
}

package com.mahitotsu.synerdesk.invoker;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class BedrockInlineAgentServiceTest {

    @Autowired
    private BedrockInlineAgentService bedrockInlineAgentService;

    @Test
    public void testHealthChecker() {
        this.bedrockInlineAgentService.invoke("HealthCheckAgent", "正常に動いていますか？");
    }

    @Test
    public void testTranslator() {
        this.bedrockInlineAgentService.invoke("TranslateAgent", "「これは鉛筆です。」を英語に翻訳してください。");
    }

    @Test
    public void testServiceDesk() {
        this.bedrockInlineAgentService.invoke("ServiceDeskAgent", "कृपया जाँच लें कि सिस्टम ठीक से काम कर रहा है।");
    }
}

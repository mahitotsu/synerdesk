package com.mahitotsu.synerdesk.invoker;

import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class BedrockInlineAgentServiceTest {

    @Autowired
    private BedrockInlineAgentService bedrockInlineAgentService;

    @Test
    public void testInvoke() {

        final String sessionId = UUID.randomUUID().toString();
        this.bedrockInlineAgentService.invoke(sessionId, "ScriptExecutionAgent", "タスクを格納するテーブルを見つけてください。");
        this.bedrockInlineAgentService.invoke(sessionId, "ScriptExecutionAgent", "データベース製品を確認してください。");
        this.bedrockInlineAgentService.invoke(sessionId, "ScriptExecutionAgent", """
        サンプルデータを5件格納するためのSQL文を作成してください。
        SQL文は確認したデータベース製品で実行できるものにしてください。ただし、SQL文の実行はしないでください。
        SQL文はsqlファイルに記述するのと同じ形式で記述してください。
        1行目に作成したSQL文の説明をコメント形式で記述してください。
        それ以外のコメントは記述しないでください。
                        """);
    }

}

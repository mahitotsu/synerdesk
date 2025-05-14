package com.mahitotsu.synerdesk.invoker;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class BedrockInlineAgentServiceTest {

    @Autowired
    private BedrockInlineAgentService bedrockInlineAgentService;

    @Test
    public void testInvoke() {

        this.bedrockInlineAgentService.invoke("ScriptExecutionAgent", new String[] {
                "タスクを格納するテーブルを見つけてください。",
                "テーブルのカラム定義を確認してください。",
                "データベース製品を確認してください。",
                """
                        サンプルデータを5件格納するためのSQL文を作成してください。
                        サンプルデータは日本語で作成してください。
                        サンプルデータは出来るだけ値が重複しないようにしてください。
                        1文で1レコードを格納するSQL文にしてください。
                        SQLの構文や関数は確認したデータベース製品に最適なものを選択してください。

                        以下のフォーマットで回答してください。
                        <format>
                        -- 今回の作業内容の概要
                        INSERT INTO ... データを格納するSQL文。1行に1レコード。行末にセミコロン(;)
                        ... 作成したSQLを全て出力
                        </format>
                        """
        });
    }

}

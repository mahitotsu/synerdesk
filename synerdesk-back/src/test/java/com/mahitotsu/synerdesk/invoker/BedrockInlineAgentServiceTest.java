package com.mahitotsu.synerdesk.invoker;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.mahitotsu.synerdesk.TestMain;

public class BedrockInlineAgentServiceTest extends TestMain {

    @Autowired
    private BedrockInlineAgentService bedrockInlineAgentService;

    @Test
    public void testInvoke() {
        this.bedrockInlineAgentService.invoke("ScriptExecutionAgent",
                """
                        チケットを新起票する際に格納先になるテーブルを特定してください。
                        """,
                """
                        格納先テーブルのカラム定義を羅べてください。
                        """,
                """
                        格納先テーブルに外部参照がある場合、外部参照先のテーブルの構造も調べてください。
                        """,
                """
                        外部参照先のテーブルに格納されているレコードを取得して、チケットの格納先のカラムの値として選択可能な値を調べてください。
                        ただし、ユーザー情報は選択可能な値を調べないでください。
                        """,
                """
                        'test@ai.com'というusernameのユーザーがチケットを起票するためのSQLのサンプルを書いてください。
                        コード値を指定するカラムは確認した結果に基づいて、選択可能な値の中から選んでください。
                        日付は本日にしてください。
                        """);
    }
}

package com.mahitotsu.synerdesk.common;

import com.mahitotsu.synerdesk.definition.Description;

@Description("""
        Javascriptのコードを実行できるツールです。
        """)
public interface JavaScriptExecutor {

    @Description("""
            指定されたjavascriptのコードを実行して結果を返します。
            最後に評価された式の結果がコード全体の実行結果となります。
            返される結果は文字列型に変換されます。
            """)
    String execute(
            @Description("""
                    実行したいjavascriptのコードを指定します。
                    """) String script) throws Exception;
}

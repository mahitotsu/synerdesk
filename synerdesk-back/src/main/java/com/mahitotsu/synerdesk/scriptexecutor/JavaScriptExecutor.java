package com.mahitotsu.synerdesk.scriptexecutor;

import com.mahitotsu.synerdesk.definition.Description;

@Description("""
        Javascriptのコードを実行できるツールです。
        """)
public interface JavaScriptExecutor {

    @Description("""
            指定されたjavascriptのコードを実行して結果を返します。
            標準出力に出力された内容が実行結果となります。
            """)
    String execute(
            @Description("""
                    実行したいjavascriptのコードを指定します。
                    """) String script) throws Exception;
}

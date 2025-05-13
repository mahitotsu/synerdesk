package com.mahitotsu.synerdesk.common;

import com.mahitotsu.synerdesk.definition.Description;

@Description("""
        Pythonのコードを実行できるツールです。
        """)
public interface PythonExecutor {

    @Description("""
            指定されたpythonのコードを実行して結果を返します。
            標準出力に出力された内容が実行結果となります。
            """)
    String execute(
            @Description("""
                    実行したいpythonのコードを指定します。
                    """) String script) throws Exception;
}

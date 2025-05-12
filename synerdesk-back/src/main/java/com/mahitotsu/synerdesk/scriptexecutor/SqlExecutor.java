package com.mahitotsu.synerdesk.scriptexecutor;

import java.util.List;
import java.util.Map;

import com.mahitotsu.synerdesk.definition.Description;

@Description("""
        SQL文を実行して結果を返すツールです。
        SQL文の組み立てに有用なテーブル定義やカラム定義、データベースの製品情報を取得することもできます。
        """)
public interface SqlExecutor {

    @Description("""
            データが格納されているデータベースの製品情報を取得して返します。
            """)
    Map<String, Object> getProductInfo();

    @Description("""
            データが格納されているデータベースのテーブル定義の一覧を取得して返します。
            """)
    List<Map<String, Object>> getTableInfo();

    @Description("""
            データが格納されているデータベースのカラム定義を取得して返します。
            """)
    List<Map<String, Object>> getColumnInfo(
            @Description("""
                    テーブル名を指定してください。
                    """) String tableName);
}

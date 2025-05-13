package com.mahitotsu.synerdesk.scriptexecutor;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ConnectionCallback;
import org.springframework.jdbc.core.JdbcOperations;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

import com.mahitotsu.synerdesk.definition.DefaultReturnControlToolDefinition;

@Component
public class SqlExecutionTool extends DefaultReturnControlToolDefinition<SqlExecutor> implements SqlExecutor {

    public SqlExecutionTool() {
        super(SqlExecutor.class, "SqlExecutor");
    }

    @Autowired
    private JdbcOperations JdbcOperations;

    @Override
    public Map<String, Object> getProductInfo() {

        return this.JdbcOperations.execute(new ConnectionCallback<Map<String, Object>>() {
            public Map<String, Object> doInConnection(@NonNull Connection con)
                    throws SQLException, DataAccessException {
                final DatabaseMetaData metaData = con.getMetaData();
                final Map<String, Object> result = Map.of(
                        "productName", metaData.getDatabaseProductName(),
                        "productVersion", metaData.getDatabaseProductVersion(),
                        "driverName", metaData.getDriverName(),
                        "driverVersion", metaData.getDriverVersion(),
                        "url", metaData.getURL(),
                        "userName", metaData.getUserName());
                return result;
            }
        });
    }

    @Override
    public List<Map<String, Object>> getTableInfo() {

        return this.JdbcOperations.execute(new ConnectionCallback<List<Map<String, Object>>>() {
            public List<Map<String, Object>> doInConnection(@NonNull Connection con)
                    throws SQLException, DataAccessException {
                final List<Map<String, Object>> result = new ArrayList<>();
                final DatabaseMetaData metaData = con.getMetaData();
                final ResultSet rs = metaData.getTables(con.getCatalog(), con.getSchema(), "%",
                        new String[] { "TABLE" });
                while (rs.next()) {
                    final Map<String, Object> tableInfo = new HashMap<>();
                    tableInfo.put("tableName", rs.getString("TABLE_NAME"));
                    tableInfo.put("tableType", rs.getString("TABLE_TYPE"));
                    tableInfo.put("tableCat", rs.getString("TABLE_CAT"));
                    tableInfo.put("tableSchem", rs.getString("TABLE_SCHEM"));
                    tableInfo.put("remarks", rs.getString("REMARKS"));
                    result.add(tableInfo);
                }
                return result;
            }
        });
    }

    @Override
    public List<Map<String, Object>> getColumnInfo(final String tableName) {

        return this.JdbcOperations.execute(new ConnectionCallback<List<Map<String, Object>>>() {
            public List<Map<String, Object>> doInConnection(@NonNull Connection con)
                    throws SQLException, DataAccessException {
                final List<Map<String, Object>> result = new ArrayList<>();
                final DatabaseMetaData metaData = con.getMetaData();

                final String schema = con.getSchema() == null ? "public" : con.getSchema();
                final ResultSet rs = metaData.getColumns(null, schema, tableName, "%");
                while (rs.next()) {
                    final Map<String, Object> columnInfo = new HashMap<>();
                    columnInfo.put("tableName", rs.getString("TABLE_NAME"));
                    columnInfo.put("columnName", rs.getString("COLUMN_NAME"));
                    columnInfo.put("dataType", rs.getInt("DATA_TYPE"));
                    columnInfo.put("typeName", rs.getString("TYPE_NAME"));
                    columnInfo.put("columnSize", rs.getInt("COLUMN_SIZE"));
                    columnInfo.put("decimalDigits", rs.getInt("DECIMAL_DIGITS"));
                    columnInfo.put("nullable", rs.getInt("NULLABLE"));
                    columnInfo.put("remarks", rs.getString("REMARKS"));
                    result.add(columnInfo);
                }
                return result;
            }
        });
    }
}

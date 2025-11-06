package com.example.dbManager.Controllers;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import lombok.extern.slf4j.Slf4j;

import java.sql.*;
import java.util.*;

@Slf4j
@RestController
public class MysqlDbExtractorController {

    @GetMapping("/")
    public String home() {
        return "MySQL DB Extractor is running.";
    }

    @GetMapping("/extract")
    public Map<String, Object> extract(
            @RequestParam String host,
            @RequestParam int port,
            @RequestParam String database,
            @RequestParam String username,
            @RequestParam String password) {
        Map<String, Object> schema = new HashMap<>();

        String url = "jdbc:mysql://" + host + ":" + port + "/" + database + "?useSSL=false&serverTimezone=UTC";
        /* List<String> tablesDB = new ArrayList<>(); */
        try (Connection conn = DriverManager.getConnection(url, username, password)) {

            DatabaseMetaData metaData = conn.getMetaData();
            ResultSet tables = metaData.getTables(database, null, "%", new String[] { "TABLE" });

            log.info("---- Log Db Params ----");
            log.info("DB Config -> host: {}, port: {}, database: {}, username: {}, password: {}",
                    host, port, database, username, password);

            log.info("---- Log Db Params ----");
            log.info("---- TABLES IN DATABASE ----");

            /*
             * while (tables.next()) {
             * String tableName = tables.getString("TABLE_NAME");
             * String tableType = tables.getString("TABLE_TYPE");
             * 
             * log.info("Table: {} | Type: {}", tableName, tableType);
             * }
             */
            while (tables.next()) {
                String tableName = tables.getString("TABLE_NAME");

                /* tablesDB.add(tableName); */

                Map<String, Object> tableInfo = new LinkedHashMap<>();
                List<Map<String, Object>> columnList = new ArrayList<>();

                ResultSet columns = metaData.getColumns(database, null, tableName, "%");
                while (columns.next()) {
                    Map<String, Object> col = new LinkedHashMap<>();
                    col.put("name", columns.getString("COLUMN_NAME"));
                    col.put("type", columns.getString("TYPE_NAME"));
                    col.put("size", columns.getInt("COLUMN_SIZE"));
                    col.put("nullable", columns.getString("IS_NULLABLE"));
                    columnList.add(col);
                }

                // Primary keys
                List<String> primaryKeys = new ArrayList<>();
                ResultSet pk = metaData.getPrimaryKeys(database, null, tableName);
                while (pk.next()) {
                    primaryKeys.add(pk.getString("COLUMN_NAME"));
                }

                // Foreign keys
                List<Map<String, String>> foreignKeys = new ArrayList<>();
                ResultSet fk = metaData.getImportedKeys(database, null, tableName);
                while (fk.next()) {
                    Map<String, String> fkInfo = new LinkedHashMap<>();
                    fkInfo.put("column", fk.getString("FKCOLUMN_NAME"));
                    fkInfo.put("ref_table", fk.getString("PKTABLE_NAME"));
                    fkInfo.put("ref_column", fk.getString("PKCOLUMN_NAME"));
                    foreignKeys.add(fkInfo);
                }

                tableInfo.put("columns", columnList);
                tableInfo.put("primary_keys", primaryKeys);
                tableInfo.put("foreign_keys", foreignKeys);

                schema.put(tableName, tableInfo);
            }

        } catch (Exception e) {
            e.printStackTrace();
            schema.put("error", e.getMessage());
        }

        return schema;
    }
}

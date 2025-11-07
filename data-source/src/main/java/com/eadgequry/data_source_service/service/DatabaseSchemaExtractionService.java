package com.eadgequry.data_source_service.service;

import com.eadgequry.data_source_service.model.DatabaseConfig;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.sql.*;
import java.util.*;

@Service
public class DatabaseSchemaExtractionService {

    private static final Logger logger = LoggerFactory.getLogger(DatabaseSchemaExtractionService.class);
    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Extract database schema (tables, columns, relationships) and return as JSON string
     */
    public String extractSchema(DatabaseConfig config) {
        Connection connection = null;
        try {
            String jdbcUrl = buildJdbcUrl(config);
            connection = DriverManager.getConnection(jdbcUrl, config.getUsername(), config.getPassword());

            DatabaseMetaData metaData = connection.getMetaData();
            Map<String, Object> schema = new LinkedHashMap<>();

            schema.put("databaseName", config.getDatabaseName());
            schema.put("databaseType", config.getType());
            schema.put("extractedAt", new java.util.Date());

            // Extract tables
            List<Map<String, Object>> tables = extractTables(metaData, config);
            schema.put("tables", tables);

            // Convert to JSON
            return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(schema);

        } catch (Exception e) {
            logger.error("Failed to extract schema for database: {}", config.getDatabaseName(), e);
            throw new RuntimeException("Schema extraction failed: " + e.getMessage(), e);
        } finally {
            if (connection != null) {
                try {
                    connection.close();
                } catch (SQLException e) {
                    logger.warn("Error closing connection", e);
                }
            }
        }
    }

    private List<Map<String, Object>> extractTables(DatabaseMetaData metaData, DatabaseConfig config) throws SQLException {
        List<Map<String, Object>> tables = new ArrayList<>();

        String catalog = getCatalog(config);
        String schemaPattern = getSchemaPattern(config);

        // Get all tables
        try (ResultSet rs = metaData.getTables(catalog, schemaPattern, "%", new String[]{"TABLE"})) {
            while (rs.next()) {
                Map<String, Object> table = new LinkedHashMap<>();
                String tableName = rs.getString("TABLE_NAME");

                table.put("name", tableName);
                table.put("type", rs.getString("TABLE_TYPE"));
                table.put("remarks", rs.getString("REMARKS"));

                // Extract columns for this table
                List<Map<String, Object>> columns = extractColumns(metaData, catalog, schemaPattern, tableName);
                table.put("columns", columns);

                // Extract primary keys
                List<String> primaryKeys = extractPrimaryKeys(metaData, catalog, schemaPattern, tableName);
                table.put("primaryKeys", primaryKeys);

                // Extract foreign keys (relationships)
                List<Map<String, Object>> foreignKeys = extractForeignKeys(metaData, catalog, schemaPattern, tableName);
                table.put("foreignKeys", foreignKeys);

                // Extract indexes
                List<Map<String, Object>> indexes = extractIndexes(metaData, catalog, schemaPattern, tableName);
                table.put("indexes", indexes);

                tables.add(table);
            }
        }

        return tables;
    }

    private List<Map<String, Object>> extractColumns(DatabaseMetaData metaData, String catalog,
                                                       String schema, String tableName) throws SQLException {
        List<Map<String, Object>> columns = new ArrayList<>();

        try (ResultSet rs = metaData.getColumns(catalog, schema, tableName, "%")) {
            while (rs.next()) {
                Map<String, Object> column = new LinkedHashMap<>();
                column.put("name", rs.getString("COLUMN_NAME"));
                column.put("type", rs.getString("TYPE_NAME"));
                column.put("size", rs.getInt("COLUMN_SIZE"));
                column.put("nullable", rs.getInt("NULLABLE") == DatabaseMetaData.columnNullable);
                column.put("defaultValue", rs.getString("COLUMN_DEF"));
                column.put("ordinalPosition", rs.getInt("ORDINAL_POSITION"));
                column.put("remarks", rs.getString("REMARKS"));

                columns.add(column);
            }
        }

        return columns;
    }

    private List<String> extractPrimaryKeys(DatabaseMetaData metaData, String catalog,
                                             String schema, String tableName) throws SQLException {
        List<String> primaryKeys = new ArrayList<>();

        try (ResultSet rs = metaData.getPrimaryKeys(catalog, schema, tableName)) {
            while (rs.next()) {
                primaryKeys.add(rs.getString("COLUMN_NAME"));
            }
        }

        return primaryKeys;
    }

    private List<Map<String, Object>> extractForeignKeys(DatabaseMetaData metaData, String catalog,
                                                           String schema, String tableName) throws SQLException {
        List<Map<String, Object>> foreignKeys = new ArrayList<>();

        try (ResultSet rs = metaData.getImportedKeys(catalog, schema, tableName)) {
            while (rs.next()) {
                Map<String, Object> fk = new LinkedHashMap<>();
                fk.put("name", rs.getString("FK_NAME"));
                fk.put("column", rs.getString("FKCOLUMN_NAME"));
                fk.put("referencedTable", rs.getString("PKTABLE_NAME"));
                fk.put("referencedColumn", rs.getString("PKCOLUMN_NAME"));
                fk.put("updateRule", getUpdateDeleteRule(rs.getShort("UPDATE_RULE")));
                fk.put("deleteRule", getUpdateDeleteRule(rs.getShort("DELETE_RULE")));

                foreignKeys.add(fk);
            }
        }

        return foreignKeys;
    }

    private List<Map<String, Object>> extractIndexes(DatabaseMetaData metaData, String catalog,
                                                       String schema, String tableName) throws SQLException {
        List<Map<String, Object>> indexes = new ArrayList<>();

        try (ResultSet rs = metaData.getIndexInfo(catalog, schema, tableName, false, false)) {
            while (rs.next()) {
                // Skip table statistics
                if (rs.getShort("TYPE") == DatabaseMetaData.tableIndexStatistic) {
                    continue;
                }

                Map<String, Object> index = new LinkedHashMap<>();
                index.put("name", rs.getString("INDEX_NAME"));
                index.put("column", rs.getString("COLUMN_NAME"));
                index.put("unique", !rs.getBoolean("NON_UNIQUE"));
                index.put("ordinalPosition", rs.getShort("ORDINAL_POSITION"));

                indexes.add(index);
            }
        }

        return indexes;
    }

    private String getCatalog(DatabaseConfig config) {
        // For MySQL, the catalog is the database name
        if ("mysql".equalsIgnoreCase(config.getType())) {
            return config.getDatabaseName();
        }
        return null;
    }

    private String getSchemaPattern(DatabaseConfig config) {
        // For PostgreSQL, use schema (default is 'public')
        if ("postgresql".equalsIgnoreCase(config.getType())) {
            return config.getSchemaName() != null ? config.getSchemaName() : "public";
        }
        return null;
    }

    private String getUpdateDeleteRule(short rule) {
        switch (rule) {
            case DatabaseMetaData.importedKeyCascade:
                return "CASCADE";
            case DatabaseMetaData.importedKeySetNull:
                return "SET NULL";
            case DatabaseMetaData.importedKeySetDefault:
                return "SET DEFAULT";
            case DatabaseMetaData.importedKeyRestrict:
                return "RESTRICT";
            case DatabaseMetaData.importedKeyNoAction:
                return "NO ACTION";
            default:
                return "UNKNOWN";
        }
    }

    private String buildJdbcUrl(DatabaseConfig config) {
        String type = config.getType().toLowerCase();
        switch (type) {
            case "mysql":
                return String.format("jdbc:mysql://%s:%d/%s?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true",
                        config.getHost(), config.getPort() != null ? config.getPort() : 3306, config.getDatabaseName());

            case "postgresql":
                return String.format("jdbc:postgresql://%s:%d/%s",
                        config.getHost(), config.getPort() != null ? config.getPort() : 5432, config.getDatabaseName());

            case "sqlserver":
                return String.format("jdbc:sqlserver://%s:%d;databaseName=%s",
                        config.getHost(), config.getPort() != null ? config.getPort() : 1433, config.getDatabaseName());

            case "oracle":
                return String.format("jdbc:oracle:thin:@%s:%d:%s",
                        config.getHost(), config.getPort() != null ? config.getPort() : 1521, config.getDatabaseName());

            case "h2":
                return String.format("jdbc:h2:tcp://%s:%d/%s",
                        config.getHost(), config.getPort() != null ? config.getPort() : 9092, config.getDatabaseName());

            default:
                throw new IllegalArgumentException("Unsupported database type: " + type);
        }
    }
}

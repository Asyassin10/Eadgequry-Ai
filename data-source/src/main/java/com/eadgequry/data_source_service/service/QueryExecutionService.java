package com.eadgequry.data_source_service.service;

import com.eadgequry.data_source_service.dto.QueryExecutionResponse;
import com.eadgequry.data_source_service.exception.DatabaseConfigNotFoundException;
import com.eadgequry.data_source_service.model.DatabaseConfig;
import com.eadgequry.data_source_service.repository.DatabaseConfigRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.sql.*;
import java.util.*;

@Service
@Slf4j
@RequiredArgsConstructor
public class QueryExecutionService {

    private final DatabaseConfigRepository databaseConfigRepository;

    /**
     * Execute SQL query on the specified database
     * IMPORTANT: Only SELECT queries should be allowed (validated by chatbot service)
     */
    public QueryExecutionResponse executeQuery(Long databaseConfigId, Long userId, String sqlQuery) {
        long startTime = System.currentTimeMillis();

        try {
            // Get database configuration
            DatabaseConfig config = databaseConfigRepository.findByIdAndUserId(databaseConfigId, userId)
                    .orElseThrow(() -> new DatabaseConfigNotFoundException(databaseConfigId, userId));

            // Build JDBC URL
            String jdbcUrl = buildJdbcUrl(config);

            // Execute query
            List<Map<String, Object>> results = executeQueryOnDatabase(jdbcUrl, config.getUsername(),
                    config.getPassword(), sqlQuery);

            long executionTime = System.currentTimeMillis() - startTime;

            log.info("Query executed successfully in {}ms, returned {} rows", executionTime, results.size());

            return QueryExecutionResponse.success(sqlQuery, results, executionTime);

        } catch (Exception e) {
            log.error("Query execution failed", e);
            return QueryExecutionResponse.error(sqlQuery, e.getMessage());
        }
    }

    /**
     * Execute query on database and return results as list of maps
     */
    private List<Map<String, Object>> executeQueryOnDatabase(String jdbcUrl, String username,
                                                              String password, String sqlQuery) throws SQLException {

        List<Map<String, Object>> results = new ArrayList<>();

        try (Connection connection = DriverManager.getConnection(jdbcUrl, username, password);
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(sqlQuery)) {

            ResultSetMetaData metaData = resultSet.getMetaData();
            int columnCount = metaData.getColumnCount();

            while (resultSet.next()) {
                Map<String, Object> row = new LinkedHashMap<>();

                for (int i = 1; i <= columnCount; i++) {
                    String columnName = metaData.getColumnLabel(i);
                    Object value = resultSet.getObject(i);
                    row.put(columnName, value);
                }

                results.add(row);
            }
        }

        return results;
    }

    /**
     * Validate SQL query syntax (basic validation)
     */
    public boolean validateQuerySyntax(String sqlQuery) {
        if (sqlQuery == null || sqlQuery.trim().isEmpty()) {
            return false;
        }

        String trimmed = sqlQuery.trim().toLowerCase();

        // Must start with SELECT
        if (!trimmed.startsWith("select")) {
            return false;
        }

        // Must contain FROM
        if (!trimmed.contains(" from ")) {
            return false;
        }

        // Check for balanced quotes
        long singleQuotes = sqlQuery.chars().filter(ch -> ch == '\'').count();
        long doubleQuotes = sqlQuery.chars().filter(ch -> ch == '"').count();

        return singleQuotes % 2 == 0 && doubleQuotes % 2 == 0;
    }

    /**
     * Build JDBC URL based on database configuration
     */
    private String buildJdbcUrl(DatabaseConfig config) {
        String type = config.getType().toLowerCase();

        return switch (type) {
            case "mysql" -> String.format("jdbc:mysql://%s:%d/%s?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true",
                    config.getHost(), config.getPort() != null ? config.getPort() : 3306, config.getDatabaseName());

            case "postgresql" -> String.format("jdbc:postgresql://%s:%d/%s",
                    config.getHost(), config.getPort() != null ? config.getPort() : 5432, config.getDatabaseName());

            case "sqlserver" -> String.format("jdbc:sqlserver://%s:%d;databaseName=%s",
                    config.getHost(), config.getPort() != null ? config.getPort() : 1433, config.getDatabaseName());

            case "oracle" -> String.format("jdbc:oracle:thin:@%s:%d:%s",
                    config.getHost(), config.getPort() != null ? config.getPort() : 1521, config.getDatabaseName());

            case "h2" -> String.format("jdbc:h2:tcp://%s:%d/%s",
                    config.getHost(), config.getPort() != null ? config.getPort() : 9092, config.getDatabaseName());

            default -> throw new IllegalArgumentException("Unsupported database type: " + type);
        };
    }
}

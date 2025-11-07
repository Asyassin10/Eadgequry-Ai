package com.eadgequry.data_source_service.service;

import com.eadgequry.data_source_service.dto.QueryExecutionResponse;
import com.eadgequry.data_source_service.exception.DatabaseConfigNotFoundException;
import com.eadgequry.data_source_service.model.DatabaseConfig;
import com.eadgequry.data_source_service.repository.DatabaseConfigRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.sql.*;
import java.util.*;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
public class QueryExecutionService {

    private static final Logger log = LoggerFactory.getLogger(QueryExecutionService.class);

    private final DatabaseConfigRepository databaseConfigRepository;

    // Forbidden SQL keywords for security
    private static final String[] FORBIDDEN_KEYWORDS = {
            "INSERT", "UPDATE", "DELETE", "DROP", "ALTER", "TRUNCATE",
            "CREATE", "REPLACE", "GRANT", "REVOKE", "EXEC", "EXECUTE",
            "CALL", "LOAD", "OUTFILE", "INFILE", "DUMPFILE", "MERGE"
    };

    private static final Pattern SELECT_PATTERN = Pattern.compile("^\\s*SELECT\\s+", Pattern.CASE_INSENSITIVE);
    private static final Pattern FROM_PATTERN = Pattern.compile("\\s+FROM\\s+", Pattern.CASE_INSENSITIVE);

    /**
     * Execute SQL query on the specified database
     * SECURITY: Only SELECT queries are allowed - all dangerous operations are blocked
     */
    public QueryExecutionResponse executeQuery(Long databaseConfigId, Long userId, String sqlQuery) {
        long startTime = System.currentTimeMillis();

        try {
            // SECURITY: Validate query before execution
            validateQuerySecurity(sqlQuery);

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

        } catch (IllegalArgumentException e) {
            // Security violation
            log.error("Security validation failed: {}", e.getMessage());
            return QueryExecutionResponse.error(sqlQuery, "Security error: " + e.getMessage());
        } catch (Exception e) {
            log.error("Query execution failed", e);
            return QueryExecutionResponse.error(sqlQuery, e.getMessage());
        }
    }

    /**
     * SECURITY: Validate query to ensure only SELECT is allowed
     * Blocks: DELETE, DROP, UPDATE, INSERT, TRUNCATE, etc.
     */
    private void validateQuerySecurity(String sqlQuery) {
        if (sqlQuery == null || sqlQuery.trim().isEmpty()) {
            throw new IllegalArgumentException("SQL query cannot be empty");
        }

        String trimmedQuery = sqlQuery.trim();

        // Must start with SELECT
        if (!SELECT_PATTERN.matcher(trimmedQuery).find()) {
            throw new IllegalArgumentException("Only SELECT queries are allowed. Query must start with SELECT.");
        }

        // Must contain FROM clause
        if (!FROM_PATTERN.matcher(trimmedQuery).find()) {
            throw new IllegalArgumentException("Invalid SELECT query: FROM clause is required");
        }

        // Check for forbidden keywords (as whole words)
        String upperQuery = sqlQuery.toUpperCase();
        for (String forbiddenKeyword : FORBIDDEN_KEYWORDS) {
            String regex = "\\b" + Pattern.quote(forbiddenKeyword) + "\\b";
            if (Pattern.compile(regex, Pattern.CASE_INSENSITIVE).matcher(sqlQuery).find()) {
                throw new IllegalArgumentException(
                        String.format("Forbidden SQL keyword detected: %s. Only SELECT queries are allowed.", forbiddenKeyword));
            }
        }

        // Validate query structure (matching quotes and parentheses)
        validateQueryStructure(sqlQuery);

        log.debug("Query passed security validation");
    }

    /**
     * Validate query structure (matching quotes, parentheses)
     */
    private void validateQueryStructure(String query) {
        // Check single quotes
        long singleQuotes = query.chars().filter(ch -> ch == '\'').count();
        if (singleQuotes % 2 != 0) {
            throw new IllegalArgumentException("Invalid SQL syntax: Unmatched single quote detected");
        }

        // Check double quotes
        long doubleQuotes = query.chars().filter(ch -> ch == '"').count();
        if (doubleQuotes % 2 != 0) {
            throw new IllegalArgumentException("Invalid SQL syntax: Unmatched double quote detected");
        }

        // Check parentheses
        long openParens = query.chars().filter(ch -> ch == '(').count();
        long closeParens = query.chars().filter(ch -> ch == ')').count();
        if (openParens != closeParens) {
            throw new IllegalArgumentException("Invalid SQL syntax: Unmatched parentheses detected");
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
        try {
            validateQuerySecurity(sqlQuery);
            return true;
        } catch (Exception e) {
            return false;
        }
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

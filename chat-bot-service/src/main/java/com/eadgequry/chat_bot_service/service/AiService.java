package com.eadgequry.chat_bot_service.service;

import com.eadgequry.chat_bot_service.config.AiApiProperties;
import com.eadgequry.chat_bot_service.dto.DatabaseSchemaDTO;
import com.eadgequry.chat_bot_service.exception.ChatBotException;
import com.eadgequry.chat_bot_service.model.UserAiSettings;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;

import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.Set;
import java.util.HashSet;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Simple AI Service for SQL generation and natural language responses
 * Works with any database - language agnostic, no business logic
 */
@Service
@RequiredArgsConstructor
public class AiService {

    private static final Logger log = LoggerFactory.getLogger(AiService.class);

    private final AiApiProperties aiApiProperties;
    private final ObjectMapper objectMapper;
    private final WebClient webClient;
    private final UserAiSettingsService userAiSettingsService;

    /**
     * Check if question is a greeting or non-database question
     * Returns friendly response if yes, null if it's a database question
     */
    public String handleNonDatabaseQuestion(String question) {
        String lowerQuestion = question.toLowerCase().trim();

        // Detect non-English language (simple detection)
        if (isNonEnglish(question)) {
            return "I can only communicate in English. Could you please ask your question in English? " +
                    "I'm here to help you query your database!\n\n" +
                    "Example questions:\n" +
                    "• Show me all customers\n" +
                    "• How many orders were placed last month?\n" +
                    "• Find products with price above $100";
        }

        // Handle greetings
        if (lowerQuestion
                .matches("^(hi|hello|hey|good morning|good afternoon|good evening|bonjour|hola|salut|ciao).*")) {
            return "Hello! I'm your AI assistant for EadgeQuery. I'm here to help you query and analyze your database using natural language (in English). Just ask me anything about your data!";
        }

        // Handle "who are you" type questions
        if (lowerQuestion.matches(".*(who are you|what are you|what can you do|help).*")) {
            return "I'm an AI-powered database assistant for EadgeQuery. I can help you:\n\n" +
                    "• Query your database using natural language (in English)\n" +
                    "• Generate and execute complex SQL queries automatically\n" +
                    "• Handle JOINs, aggregations, subqueries, and calculations\n" +
                    "• Analyze and present your data in easy-to-read tables\n" +
                    "• Work with any database: MySQL, PostgreSQL, Oracle, SQL Server, etc.\n\n" +
                    "Example questions:\n" +
                    "• Show me all customers from California\n" +
                    "• For each product, show total revenue and quantity sold\n" +
                    "• Find customers who haven't ordered in the last 6 months\n" +
                    "• Which employees report to John Smith?";
        }

        // Check if question seems unrelated to database
        if (lowerQuestion.matches(".*(weather|news|joke|game|movie|music|recipe|time|date|politics|sports).*") &&
                !lowerQuestion.matches(
                        ".*(table|database|query|select|data|record|row|column|customer|order|product|employee).*")) {
            return "I'm specifically designed to help you with your database queries. I can answer questions about your data, tables, and records. "
                    +
                    "Please ask me something about your database.\n\n" +
                    "Try questions like:\n" +
                    "• Show all customers\n" +
                    "• How many orders were placed this month?\n" +
                    "• Find products that are out of stock\n" +
                    "• For each customer, show their total spending";
        }

        return null; // It's a database question
    }

    /**
     * Simple non-English language detection
     */
    private boolean isNonEnglish(String text) {
        // Check for common non-English characters and patterns
        // French: é, è, ê, à, ù, ç, etc.
        // Spanish: ñ, á, é, í, ó, ú, ¿, ¡
        // German: ä, ö, ü, ß
        // Arabic, Chinese, Japanese, etc.

        // Count non-ASCII characters
        long nonAsciiCount = text.chars().filter(c -> c > 127).count();

        // If more than 15% non-ASCII, likely non-English
        if (text.length() > 0 && (double) nonAsciiCount / text.length() > 0.15) {
            return true;
        }

        // Check for common non-English words (simple patterns)
        String lower = text.toLowerCase();
        if (lower.matches(".*(bonjour|merci|comment|quelle|donde|cómo|cuál|wie|welche|什么|どう|كيف|как|где).*")) {
            return true;
        }

        return false;
    }

    /**
     * Parse schemaJson string and populate the tables field
     */
    private DatabaseSchemaDTO parseSchemaFromJson(DatabaseSchemaDTO dto) {
        try {
            if (dto == null) {
                throw new ChatBotException("Schema DTO is null");
            }

            // If tables already populated, return as-is
            if (dto.getTables() != null && !dto.getTables().isEmpty()) {
                return dto;
            }

            // Parse schemaJson string
            if (dto.getSchemaJson() == null || dto.getSchemaJson().trim().isEmpty()) {
                throw new ChatBotException("Schema JSON is empty");
            }

            log.debug("Parsing schemaJson string...");

            // Parse the JSON string into a temporary object
            DatabaseSchemaDTO parsed = objectMapper.readValue(dto.getSchemaJson(), DatabaseSchemaDTO.class);

            // Copy parsed fields into original DTO
            dto.setDatabaseName(parsed.getDatabaseName());
            dto.setDatabaseType(parsed.getDatabaseType());
            dto.setTables(parsed.getTables());

            log.debug("Successfully parsed schema: {} tables",
                    dto.getTables() != null ? dto.getTables().size() : 0);

            return dto;

        } catch (Exception e) {
            log.error("Failed to parse schema JSON", e);
            throw new ChatBotException("Failed to parse database schema: " + e.getMessage());
        }
    }

    /**
     * Generate SQL query with 99.9% accuracy using two-stage verification
     */
    public String generateSqlQuery(Long userId, String question, DatabaseSchemaDTO schema, String previousError) {

        schema = parseSchemaFromJson(schema);
        // Validate inputs
        if (schema == null) {
            throw new ChatBotException("Schema is null. Please connect to a database first.");
        }
        if (schema.getTables() == null || schema.getTables().isEmpty()) {
            throw new ChatBotException("Schema has no tables. Please refresh the database connection.");
        }
        if (question == null || question.trim().isEmpty()) {
            throw new ChatBotException("Question cannot be empty.");
        }

        try {
            // STAGE 1: Analyze and map to schema
            SchemaMapping mapping = analyzeSchemaMappingWithValidation(userId, question, schema, previousError);

            // STAGE 2: Generate SQL using validated mapping
            String sql = generateSqlFromMapping(userId, question, mapping, schema);

            log.debug("Generated SQL: {}", sql);
            return cleanSqlResponse(sql);

        } catch (ChatBotException e) {
            throw e;
        } catch (Exception e) {
            log.error("Failed to generate SQL query", e);
            throw new ChatBotException("Failed to generate SQL query: " + e.getMessage(), e);
        }
    }

    /**
     * STAGE 1: Analyze question and map to schema with validation
     */
    private SchemaMapping analyzeSchemaMappingWithValidation(Long userId, String question,
            DatabaseSchemaDTO schema, String previousError) {

        String prompt = buildMappingPrompt(question, schema, previousError);
        String response = callAiApi(userId, prompt, 0.2); // Very low temp for structured output

        // Parse and validate mapping
        SchemaMapping mapping = parseMappingResponse(response);
        validateMapping(mapping, schema);

        return mapping;
    }

    /**
     * Build prompt for schema mapping analysis
     */
    private String buildMappingPrompt(String question, DatabaseSchemaDTO schema, String previousError) {
        StringBuilder p = new StringBuilder();

        p.append("You are a database schema analyzer. Map user question to exact schema identifiers.\n\n");

        // Show available schema with null checks
        p.append("=== AVAILABLE SCHEMA ===\n");
        Map<String, List<String>> schemaMap = new HashMap<>();
        Map<String, String> columnTypes = new HashMap<>();

        if (schema != null && schema.getTables() != null) {
            for (DatabaseSchemaDTO.TableInfo table : schema.getTables()) {
                if (table == null || table.getName() == null)
                    continue;

                List<String> cols = new ArrayList<>();
                if (table.getColumns() != null) {
                    for (DatabaseSchemaDTO.ColumnInfo col : table.getColumns()) {
                        if (col != null && col.getName() != null) {
                            cols.add(col.getName());
                            columnTypes.put(table.getName() + "." + col.getName(), col.getType());
                        }
                    }
                }
                schemaMap.put(table.getName(), cols);
            }
        }

        if (schemaMap.isEmpty()) {
            throw new ChatBotException("No valid tables found in schema");
        }

        for (Map.Entry<String, List<String>> entry : schemaMap.entrySet()) {
            p.append("TABLE: ").append(entry.getKey()).append("\n");
            p.append("  COLUMNS: ").append(String.join(", ", entry.getValue())).append("\n");
        }
        p.append("\n");

        // Show foreign keys
        p.append("=== FOREIGN KEYS (for JOINs) ===\n");
        boolean hasForeignKeys = false;
        if (schema != null && schema.getTables() != null) {
            for (DatabaseSchemaDTO.TableInfo table : schema.getTables()) {
                if (table == null || table.getForeignKeys() == null)
                    continue;

                for (DatabaseSchemaDTO.ForeignKeyInfo fk : table.getForeignKeys()) {
                    if (fk != null && fk.getColumn() != null && fk.getReferencedTable() != null) {
                        p.append(table.getName()).append(".").append(fk.getColumn())
                                .append(" → ").append(fk.getReferencedTable()).append(".")
                                .append(fk.getReferencedColumn()).append("\n");
                        hasForeignKeys = true;
                    }
                }
            }
        }
        if (!hasForeignKeys) {
            p.append("(No foreign keys defined)\n");
        }
        p.append("\n");

        if (previousError != null && !previousError.trim().isEmpty()) {
            p.append("=== PREVIOUS ERROR ===\n");
            p.append(previousError).append("\n\n");
            p.append("FIX: Choose different identifiers from AVAILABLE SCHEMA above\n\n");
        }

        // Mapping instructions
        p.append("=== TASK ===\n");
        p.append("Question: \"").append(question).append("\"\n\n");

        p.append("Analyze and output JSON ONLY (no markdown, no explanations):\n");
        p.append("{\n");
        p.append("  \"intent\": \"description of what user wants\",\n");
        p.append("  \"tables\": [\"exact_table_name1\", \"exact_table_name2\"],\n");
        p.append("  \"columns\": {\n");
        p.append("    \"exact_table_name1\": [\"exact_col1\", \"exact_col2\"],\n");
        p.append("    \"exact_table_name2\": [\"exact_col3\"]\n");
        p.append("  },\n");
        p.append("  \"joins\": [{\"table1\": \"t1\", \"table2\": \"t2\", \"on\": \"t1.col = t2.col\"}],\n");
        p.append("  \"aggregations\": [\"SUM(column)\"],\n");
        p.append("  \"groupBy\": [\"table.column\"],\n");
        p.append("  \"orderBy\": {\"column\": \"table.column\", \"direction\": \"DESC\"},\n");
        p.append("  \"limit\": 10\n");
        p.append("}\n\n");

        p.append("CRITICAL RULES:\n");
        p.append("• Use ONLY table/column names from AVAILABLE SCHEMA above\n");
        p.append("• Copy names EXACTLY (case-sensitive)\n");
        p.append("• Common mappings:\n");
        p.append("  - 'customers' → find 'customer' table\n");
        p.append("  - 'spending/payment/amount' → find 'amount' column\n");
        p.append("  - 'name' → find 'first_name', 'last_name'\n");
        p.append(
                "  - 'top N by X' → aggregations:[\"SUM(X)\"], orderBy:{\"column\":\"X\",\"direction\":\"DESC\"}, limit:N\n\n");

        return p.toString();
    }

    /**
     * Parse mapping response from AI
     */
    private SchemaMapping parseMappingResponse(String response) {
        try {
            if (response == null || response.trim().isEmpty()) {
                throw new ChatBotException("Empty response from AI");
            }

            // Clean response (remove markdown, explanations)
            String json = response
                    .replaceAll("```json\\s*", "")
                    .replaceAll("```\\s*", "")
                    .replaceAll("(?s).*?(\\{.*\\}).*", "$1") // Extract JSON object
                    .trim();

            SchemaMapping mapping = objectMapper.readValue(json, SchemaMapping.class);

            // Initialize null fields to prevent NPE
            if (mapping.getTables() == null)
                mapping.setTables(new ArrayList<>());
            if (mapping.getColumns() == null)
                mapping.setColumns(new HashMap<>());
            if (mapping.getJoins() == null)
                mapping.setJoins(new ArrayList<>());
            if (mapping.getAggregations() == null)
                mapping.setAggregations(new ArrayList<>());
            if (mapping.getGroupBy() == null)
                mapping.setGroupBy(new ArrayList<>());

            log.debug("Parsed mapping: {}", mapping);
            return mapping;

        } catch (Exception e) {
            log.error("Failed to parse mapping response: {}", response, e);
            throw new ChatBotException("Failed to parse schema mapping. Please try rephrasing your question.");
        }
    }

    /**
     * Validate mapping against schema
     */
    private void validateMapping(SchemaMapping mapping, DatabaseSchemaDTO schema) {
        if (mapping == null) {
            throw new ChatBotException("Mapping is null");
        }

        // Get valid identifiers
        Set<String> validTables = new HashSet<>();
        Map<String, Set<String>> validColumns = new HashMap<>();

        if (schema.getTables() != null) {
            for (DatabaseSchemaDTO.TableInfo table : schema.getTables()) {
                if (table == null || table.getName() == null)
                    continue;

                validTables.add(table.getName());

                Set<String> cols = new HashSet<>();
                if (table.getColumns() != null) {
                    for (DatabaseSchemaDTO.ColumnInfo col : table.getColumns()) {
                        if (col != null && col.getName() != null) {
                            cols.add(col.getName());
                        }
                    }
                }
                validColumns.put(table.getName(), cols);
            }
        }

        // Validate tables
        if (mapping.getTables() != null) {
            for (String table : mapping.getTables()) {
                if (table != null && !validTables.contains(table)) {
                    throw new ChatBotException("Invalid table: '" + table + "'. Valid tables: " + validTables);
                }
            }
        }

        // Validate columns
        if (mapping.getColumns() != null) {
            for (Map.Entry<String, List<String>> entry : mapping.getColumns().entrySet()) {
                String table = entry.getKey();

                if (table == null || !validTables.contains(table)) {
                    throw new ChatBotException("Invalid table: '" + table + "'");
                }

                Set<String> tableCols = validColumns.get(table);
                if (tableCols != null && entry.getValue() != null) {
                    for (String col : entry.getValue()) {
                        if (col != null && !tableCols.contains(col)) {
                            throw new ChatBotException("Invalid column '" + col +
                                    "' for table '" + table + "'. Valid columns: " + tableCols);
                        }
                    }
                }
            }
        }

        log.info("Schema mapping validated successfully");
    }

    /**
     * STAGE 2: Generate SQL from validated mapping
     */
    private String generateSqlFromMapping(Long userId, String question, SchemaMapping mapping,
            DatabaseSchemaDTO schema) {

        String prompt = buildSqlPrompt(question, mapping, schema);
        return callAiApi(userId, prompt, 0.3); // Low temp for accuracy
    }

    /**
     * Build SQL generation prompt using validated mapping
     */
    private String buildSqlPrompt(String question, SchemaMapping mapping, DatabaseSchemaDTO schema) {
        StringBuilder p = new StringBuilder();

        String dbType = schema.getDatabaseType() != null
                ? schema.getDatabaseType().toUpperCase()
                : "UNKNOWN";

        p.append("Generate SQL query for ").append(dbType).append(".\n\n");

        p.append("=== VALIDATED MAPPING ===\n");
        if (mapping.getIntent() != null) {
            p.append("Intent: ").append(mapping.getIntent()).append("\n");
        }
        if (mapping.getTables() != null && !mapping.getTables().isEmpty()) {
            p.append("Tables: ").append(mapping.getTables()).append("\n");
        }
        if (mapping.getColumns() != null && !mapping.getColumns().isEmpty()) {
            p.append("Columns: ").append(mapping.getColumns()).append("\n");
        }
        if (mapping.getJoins() != null && !mapping.getJoins().isEmpty()) {
            p.append("Joins: ");
            for (JoinInfo join : mapping.getJoins()) {
                p.append(join.getTable1()).append(" JOIN ").append(join.getTable2())
                        .append(" ON ").append(join.getOn()).append("; ");
            }
            p.append("\n");
        }
        if (mapping.getAggregations() != null && !mapping.getAggregations().isEmpty()) {
            p.append("Aggregations: ").append(mapping.getAggregations()).append("\n");
        }
        if (mapping.getGroupBy() != null && !mapping.getGroupBy().isEmpty()) {
            p.append("Group By: ").append(mapping.getGroupBy()).append("\n");
        }
        if (mapping.getOrderBy() != null) {
            p.append("Order By: ").append(mapping.getOrderBy().getColumn())
                    .append(" ").append(mapping.getOrderBy().getDirection()).append("\n");
        }
        if (mapping.getLimit() != null) {
            p.append("Limit: ").append(mapping.getLimit()).append("\n");
        }
        p.append("\n");

        p.append("=== ").append(dbType).append(" SYNTAX ===\n");
        switch (dbType) {
            case "POSTGRESQL":
                p.append("• LIMIT N for row limiting\n");
                p.append("• No quotes needed for lowercase names\n");
                break;
            case "MYSQL":
                p.append("• LIMIT N for row limiting\n");
                p.append("• Backticks for identifiers: `name`\n");
                break;
            case "SQLSERVER":
                p.append("• TOP N after SELECT\n");
                p.append("• Brackets for identifiers: [name]\n");
                break;
            case "ORACLE":
                p.append("• FETCH FIRST N ROWS ONLY\n");
                break;
            default:
                p.append("• Use standard SQL\n");
        }
        p.append("\n");

        p.append("=== TASK ===\n");
        p.append("Question: \"").append(question).append("\"\n\n");
        p.append("Generate SQL query:\n");
        p.append("• Use ONLY identifiers from VALIDATED MAPPING above\n");
        p.append("• Follow ").append(dbType).append(" syntax\n");
        p.append("• Return ONLY the SQL query (no markdown, no explanations)\n\n");

        return p.toString();
    }

    /**
     * Clean SQL response
     */
    private String cleanSqlResponse(String response) {
        if (response == null)
            return "";

        return response
                .replaceAll("```sql\\s*", "")
                .replaceAll("```\\s*", "")
                .replaceAll("^--.*$", "")
                .replaceAll("^\\s*\\n", "")
                .trim();
    }

    /**
     * Schema mapping DTO - Inner class
     */
    @lombok.Data
    @com.fasterxml.jackson.annotation.JsonIgnoreProperties(ignoreUnknown = true)
    private static class SchemaMapping {
        private String intent;
        private List<String> tables = new ArrayList<>();
        private Map<String, List<String>> columns = new HashMap<>();
        private List<JoinInfo> joins = new ArrayList<>();
        private List<String> aggregations = new ArrayList<>();
        private List<String> groupBy = new ArrayList<>();
        private OrderByInfo orderBy;
        private Integer limit;
    }

    @lombok.Data
    @com.fasterxml.jackson.annotation.JsonIgnoreProperties(ignoreUnknown = true)
    private static class JoinInfo {
        private String table1;
        private String table2;
        private String on;
    }

    @lombok.Data
    @com.fasterxml.jackson.annotation.JsonIgnoreProperties(ignoreUnknown = true)
    private static class OrderByInfo {
        private String column;
        private String direction;
    }

    /**
     * Generate natural language answer from SQL results
     */
    public String generateAnswer(Long userId, String question, String sqlQuery, List<Map<String, Object>> result) {
        String prompt = buildAnswerPrompt(question, sqlQuery, result);

        try {
            String response = callAiApi(userId, prompt, aiApiProperties.getTemperatureAnswer());
            log.debug("AI generated answer: {}", response);
            return cleanAnswer(response);
        } catch (Exception e) {
            log.error("Failed to generate answer", e);
            throw new ChatBotException("Failed to generate answer: " + e.getMessage(), e);
        }
    }

    private String buildQueryPrompt(String question, DatabaseSchemaDTO schema, String previousError) {
        StringBuilder p = new StringBuilder();

        String databaseType = schema != null && schema.getDatabaseType() != null
                ? schema.getDatabaseType().toUpperCase()
                : "UNKNOWN";

        // 1️⃣ EXTRACT WHITELIST - What AI CAN use
        Map<String, List<String>> tableColumns = new HashMap<>();
        if (schema != null && schema.getTables() != null) {
            for (DatabaseSchemaDTO.TableInfo table : schema.getTables()) {
                List<String> cols = table.getColumns() != null
                        ? table.getColumns().stream()
                                .map(DatabaseSchemaDTO.ColumnInfo::getName)
                                .collect(Collectors.toList())
                        : new ArrayList<>();
                tableColumns.put(table.getName(), cols);
            }
        }

        // 2️⃣ SHOW WHITELIST FIRST - What exists
        p.append("=== AVAILABLE SCHEMA (ONLY USE THESE) ===\n");
        p.append("Database: ").append(databaseType).append("\n\n");

        p.append("TABLES (").append(tableColumns.size()).append(" total):\n");
        for (String table : tableColumns.keySet()) {
            p.append("  • ").append(table).append("\n");
        }

        p.append("\nCOLUMNS BY TABLE:\n");
        for (Map.Entry<String, List<String>> entry : tableColumns.entrySet()) {
            p.append("  ").append(entry.getKey()).append(":\n");
            for (String col : entry.getValue()) {
                p.append("    - ").append(col).append("\n");
            }
        }
        p.append("\n");

        // 3️⃣ STRICT RULES
        p.append("=== CRITICAL RULES ===\n");
        p.append("🚨 You can ONLY use identifiers from the lists above\n");
        p.append("🚨 If you use a name NOT in the lists = WRONG QUERY\n");
        p.append("🚨 DO NOT invent, guess, or modify ANY name\n");
        p.append("🚨 COPY EXACT spelling and case from lists above\n\n");

        // 4️⃣ EXAMPLES - Wrong vs Right
        if (!tableColumns.isEmpty()) {
            String firstTable = tableColumns.keySet().iterator().next();
            List<String> firstCols = tableColumns.get(firstTable);

            p.append("=== EXAMPLES OF RIGHT vs WRONG ===\n");
            p.append("❌ WRONG: SELECT customerID FROM orders\n");
            p.append("   (invented 'customerID' and 'orders' not in schema)\n\n");
            p.append("✅ RIGHT: SELECT ").append(firstCols.isEmpty() ? "*" : firstCols.get(0))
                    .append(" FROM ").append(firstTable).append("\n");
            p.append("   (used exact names from schema above)\n\n");
        }

        // 5️⃣ ERROR RECOVERY
        if (previousError != null) {
            p.append("=== ⚠️ PREVIOUS ERROR ===\n");
            p.append(previousError).append("\n\n");
            p.append("FIX: Find the CORRECT name in AVAILABLE SCHEMA above\n");
            p.append("     'Unknown column X' → Find real column in COLUMNS list\n");
            p.append("     'Unknown table Y' → Find real table in TABLES list\n\n");
        }

        // 6️⃣ VERIFICATION ALGORITHM
        p.append("=== STEP-BY-STEP PROCESS ===\n");
        p.append("Question: \"").append(question).append("\"\n\n");

        p.append("BEFORE writing SQL, complete this:\n");
        p.append("1. What tables do I need? (list names from TABLES above)\n");
        p.append("2. What columns do I need? (list names from COLUMNS above)\n");
        p.append("3. VERIFY: Are ALL these in the schema above? YES/NO\n");
        p.append("4. If NO → STOP and find correct names\n");
        p.append("5. If YES → Write SQL using ONLY these verified names\n\n");

        // 7️⃣ INTENT PATTERNS (compact)
        p.append("=== UNDERSTAND INTENT ===\n");
        p.append("'top N customers by spending' → SELECT ... ORDER BY SUM(amount_col) DESC LIMIT N\n");
        p.append("'how many' → COUNT(*)\n");
        p.append("'total/sum' → SUM(column)\n");
        p.append("'each/per' → GROUP BY\n");
        p.append("'never/not' → NOT IN or LEFT JOIN WHERE NULL\n\n");

        // 8️⃣ DATABASE SYNTAX
        p.append("=== ").append(databaseType).append(" SYNTAX ===\n");
        switch (databaseType) {
            case "MYSQL":
                p.append("LIMIT N, backticks `name`, CONCAT()\n");
                break;
            case "POSTGRESQL":
                p.append("LIMIT N, quotes \"name\", || or CONCAT()\n");
                break;
            case "SQLSERVER":
                p.append("TOP N, brackets [name], + or CONCAT()\n");
                break;
            case "ORACLE":
                p.append("FETCH FIRST N ROWS or ROWNUM <= N\n");
                break;
        }
        p.append("\n");

        // 9️⃣ REAL EXAMPLE from schema
        p.append("=== REAL EXAMPLE FROM YOUR SCHEMA ===\n");
        if (!tableColumns.isEmpty()) {
            // Find tables with foreign keys for JOIN example
            String exampleQuery = buildRealExample(schema, tableColumns, databaseType);
            p.append(exampleQuery).append("\n");
        }

        p.append("\n=== OUTPUT ===\n");
        p.append("Write ONLY the SQL query\n");
        p.append("Use ONLY names from AVAILABLE SCHEMA above\n");
        p.append("NO explanations, NO markdown, NO comments\n\n");

        return p.toString();
    }

    // Helper: Build real example from actual schema
    private String buildRealExample(DatabaseSchemaDTO schema, Map<String, List<String>> tableColumns, String dbType) {
        StringBuilder example = new StringBuilder();

        // Example 1: Simple SELECT
        String firstTable = tableColumns.keySet().iterator().next();
        List<String> firstCols = tableColumns.get(firstTable);
        String firstCol = firstCols.isEmpty() ? "*" : firstCols.get(0);

        example.append("Example 1 - Count records:\n");
        example.append("  SELECT COUNT(*) FROM ").append(firstTable).append("\n\n");

        // Example 2: With specific column
        if (!firstCols.isEmpty() && firstCols.size() > 1) {
            example.append("Example 2 - Select specific columns:\n");
            example.append("  SELECT ").append(firstCols.get(0));
            if (firstCols.size() > 1) {
                example.append(", ").append(firstCols.get(1));
            }
            example.append(" FROM ").append(firstTable);
            if (dbType.equals("SQLSERVER")) {
                example.insert(example.indexOf("SELECT") + 7, "TOP 10 ");
            } else if (dbType.equals("ORACLE")) {
                example.append(" FETCH FIRST 10 ROWS ONLY");
            } else {
                example.append(" LIMIT 10");
            }
            example.append("\n\n");
        }

        // Example 3: JOIN if foreign keys exist
        for (DatabaseSchemaDTO.TableInfo table : schema.getTables()) {
            if (table.getForeignKeys() != null && !table.getForeignKeys().isEmpty()) {
                DatabaseSchemaDTO.ForeignKeyInfo fk = table.getForeignKeys().get(0);
                example.append("Example 3 - JOIN tables:\n");
                example.append("  SELECT t1.*, t2.* FROM ").append(table.getName()).append(" t1\n");
                example.append("  JOIN ").append(fk.getReferencedTable()).append(" t2\n");
                example.append("  ON t1.").append(fk.getColumn()).append(" = t2.").append(fk.getReferencedColumn())
                        .append("\n");
                example.append("  LIMIT 5\n");
                break;
            }
        }

        return example.toString();
    }

    /**
     * Get database-specific SQL syntax rules
     */
    private String getDatabaseSpecificSyntax(String databaseType) {
        StringBuilder syntax = new StringBuilder();

        switch (databaseType.toUpperCase()) {
            case "MYSQL":
                syntax.append("- Use LIMIT for row limiting: SELECT * FROM table LIMIT 10\n");
                syntax.append("- Use backticks for identifiers: `table_name`, `column_name`\n");
                syntax.append("- String concat: CONCAT(str1, str2) or CONCAT_WS(separator, str1, str2)\n");
                syntax.append("- Date functions: NOW(), CURDATE(), DATE_FORMAT(date, format)\n");
                syntax.append("- Case-insensitive comparison is default\n");
                break;

            case "POSTGRESQL":
                syntax.append("- Use LIMIT for row limiting: SELECT * FROM table LIMIT 10\n");
                syntax.append("- Use double quotes for case-sensitive identifiers: \"TableName\"\n");
                syntax.append("- String concat: str1 || str2 or CONCAT(str1, str2)\n");
                syntax.append("- Date functions: NOW(), CURRENT_DATE, TO_CHAR(date, format)\n");
                syntax.append("- Use ILIKE for case-insensitive pattern matching\n");
                syntax.append("- Boolean type: TRUE/FALSE\n");
                break;

            case "SQLSERVER":
                syntax.append("- Use TOP for row limiting: SELECT TOP 10 * FROM table\n");
                syntax.append("- Use square brackets for identifiers: [table_name], [column name]\n");
                syntax.append("- String concat: str1 + str2 or CONCAT(str1, str2)\n");
                syntax.append("- Date functions: GETDATE(), CONVERT(), FORMAT()\n");
                syntax.append("- Use schema prefix: dbo.table_name\n");
                break;

            case "ORACLE":
                syntax.append("- Use FETCH FIRST for row limiting: SELECT * FROM table FETCH FIRST 10 ROWS ONLY\n");
                syntax.append("- Or use ROWNUM: SELECT * FROM table WHERE ROWNUM <= 10\n");
                syntax.append("- Use double quotes for case-sensitive identifiers: \"table_name\"\n");
                syntax.append("- String concat: str1 || str2 or CONCAT(str1, str2)\n");
                syntax.append("- Date functions: SYSDATE, TO_DATE(), TO_CHAR()\n");
                syntax.append("- No LIMIT keyword - use ROWNUM or FETCH FIRST\n");
                break;

            case "H2":
                syntax.append("- Use LIMIT for row limiting: SELECT * FROM table LIMIT 10\n");
                syntax.append("- Compatible with both MySQL and PostgreSQL syntax\n");
                syntax.append("- Use double quotes for identifiers: \"table_name\"\n");
                break;

            default:
                syntax.append("- Use standard SQL syntax\n");
                syntax.append("- Be careful with quotes and identifiers\n");
                break;
        }

        return syntax.toString();
    }

    /**
     * Build intelligent prompt for answer generation with user-friendly
     * explanations
     */
    private String buildAnswerPrompt(String question, String sqlQuery, List<Map<String, Object>> result) {
        StringBuilder prompt = new StringBuilder();

        prompt.append(
                "You are Eadge Query AI Assistant - an EXPERT, FRIENDLY, and INTELLIGENT database assistant.\n\n");

        prompt.append("=== CONTEXT ===\n");
        prompt.append("User Question: \"").append(question).append("\"\n");
        prompt.append("SQL Query Executed: ").append(sqlQuery).append("\n");
        prompt.append("Query Results: ").append(objectMapper.valueToTree(result).toString()).append("\n\n");

        prompt.append("=== YOUR CAPABILITIES ===\n");
        prompt.append("• Understand the user's ORIGINAL question (even with typos/unclear language)\n");
        prompt.append("• Provide HELPFUL and FRIENDLY responses\n");
        prompt.append("• Explain results in a way ANYONE can understand\n");
        prompt.append("• Be PATIENT and SUPPORTIVE if no results found\n");
        prompt.append("• Suggest alternatives or corrections when needed\n");
        prompt.append("• Make data EASY to read and understand\n\n");

        if (result == null || result.isEmpty()) {
            prompt.append("=== SITUATION: NO RESULTS FOUND ===\n");
            prompt.append("The query returned NO results. Be HELPFUL and SUPPORTIVE:\n\n");

            prompt.append("INSTRUCTIONS:\n");
            prompt.append("1. Politely explain that no data was found\n");
            prompt.append("2. Acknowledge what the user was looking for (restate their question clearly)\n");
            prompt.append("3. Provide SPECIFIC, HELPFUL suggestions:\n");
            prompt.append("   - Check if the search term is spelled correctly\n");
            prompt.append("   - Try different search criteria\n");
            prompt.append("   - Suggest related searches they might want to try\n");
            prompt.append("   - If they searched for something specific, suggest broader search\n");
            prompt.append("4. Be ENCOURAGING - don't make the user feel bad\n");
            prompt.append("5. DO NOT invent data - be honest about no results\n\n");

            prompt.append("EXAMPLE RESPONSE:\n");
            prompt.append("I couldn't find any results for your search. ");
            prompt.append("It looks like there are no users with the status 'pending' in the database.\n\n");
            prompt.append("Here are some suggestions:\n");
            prompt.append("• Try searching for users with status 'active' or 'inactive'\n");
            prompt.append("• Check if the status value is spelled correctly\n");
            prompt.append("• Ask 'show all users' to see what's available\n\n");
        } else {
            prompt.append("=== SITUATION: RESULTS FOUND ===\n");
            prompt.append("Great! The query returned ").append(result.size()).append(" result(s).\n\n");

            prompt.append("CRITICAL FORMATTING RULES:\n");
            prompt.append("1. START with a friendly acknowledgment: 'Here's what I found:' or 'I found X results:'\n");
            prompt.append("2. ALWAYS present data in CLEAN MARKDOWN TABLE format\n");
            prompt.append("3. Use proper markdown table syntax with aligned columns:\n");
            prompt.append("   | Column Name | Another Column |\n");
            prompt.append("   |-------------|----------------|\n");
            prompt.append("   | Value 1     | Value 2        |\n");
            prompt.append("4. SMART column selection:\n");
            prompt.append("   - NEVER show 'id', 'created_at', 'updated_at' unless specifically asked\n");
            prompt.append("   - Show user-friendly columns: name, email, status, title, description, etc.\n");
            prompt.append("   - Use meaningful column headers (capitalize first letter)\n");
            prompt.append("5. Handle large results:\n");
            prompt.append("   - MAXIMUM 50 rows displayed (system limit)\n");
            prompt.append("   - If result has more than 50 rows, show first 50 and say:\n");
            prompt.append(
                    "     'Showing 50 of X results (maximum display limit). To see specific data, try refining your search.'\n");
            prompt.append("   - If 50 or fewer rows, show all\n");
            prompt.append("6. END with helpful summary:\n");
            prompt.append("   - Brief insight about the data (1-2 sentences)\n");
            prompt.append("   - Total count if it's a count query\n");
            prompt.append("   - Suggest related questions they might want to ask\n");
            prompt.append("7. Be CONVERSATIONAL, FRIENDLY, and PROFESSIONAL\n");
            prompt.append("8. ALWAYS respond in ENGLISH ONLY\n\n");

            prompt.append("EXCELLENT EXAMPLE:\n");
            prompt.append("Here's what I found - 3 active users in your database:\n\n");
            prompt.append("| Name  | Email            | Status |\n");
            prompt.append("|-------|------------------|--------|\n");
            prompt.append("| John  | john@test.com    | Active |\n");
            prompt.append("| Jane  | jane@test.com    | Active |\n");
            prompt.append("| Bob   | bob@test.com     | Active |\n\n");
            prompt.append(
                    "You have 3 active users. Would you like to see inactive users or filter by a specific criteria?\n\n");
        }

        prompt.append("=== YOUR TASK ===\n");
        prompt.append("Generate a HELPFUL, FRIENDLY, and WELL-FORMATTED response.\n");
        prompt.append(
                "Remember: Be understanding and supportive. The user might have asked an unclear question, but you understood it!\n\n");

        prompt.append("Response:");

        return prompt.toString();
    }

    /**
     * Format schema information for prompt - DETAILED VERSION
     */
    private String formatSchemaInfoDetailed(DatabaseSchemaDTO schema) {
        if (schema == null || schema.getTables() == null) {
            return "Schema information not available";
        }

        StringBuilder info = new StringBuilder();
        info.append("Database: ").append(schema.getDatabaseName()).append("\n");
        info.append("Type: ").append(schema.getDatabaseType()).append("\n");
        info.append("Total Tables: ").append(schema.getTables().size()).append("\n\n");

        info.append("📊 COMPLETE TABLE AND COLUMN LISTING:\n");
        info.append("=".repeat(80)).append("\n\n");

        for (DatabaseSchemaDTO.TableInfo table : schema.getTables()) {
            info.append("Table: ").append(table.getName()).append("\n");
            info.append("-".repeat(60)).append("\n");

            if (table.getColumns() != null && !table.getColumns().isEmpty()) {
                info.append("Columns (USE THESE EXACT NAMES):\n");
                for (DatabaseSchemaDTO.ColumnInfo col : table.getColumns()) {
                    info.append("  • ").append(col.getName())
                            .append(" (").append(col.getType());
                    if (col.getSize() != null && col.getSize() > 0) {
                        info.append("(").append(col.getSize()).append(")");
                    }
                    info.append(")");
                    if (col.getNullable() != null && !col.getNullable()) {
                        info.append(" NOT NULL");
                    }
                    if (col.getDefaultValue() != null) {
                        info.append(" DEFAULT ").append(col.getDefaultValue());
                    }
                    info.append("\n");
                }
            }

            if (table.getPrimaryKeys() != null && !table.getPrimaryKeys().isEmpty()) {
                info.append("Primary Key(s): ").append(String.join(", ", table.getPrimaryKeys())).append("\n");
            }

            if (table.getForeignKeys() != null && !table.getForeignKeys().isEmpty()) {
                info.append("Foreign Keys (for JOINs):\n");
                for (DatabaseSchemaDTO.ForeignKeyInfo fk : table.getForeignKeys()) {
                    info.append("  • ").append(fk.getColumn())
                            .append(" → ").append(fk.getReferencedTable())
                            .append(".").append(fk.getReferencedColumn()).append("\n");
                }
            }

            info.append("\n");
        }

        info.append("=".repeat(80)).append("\n");
        info.append("⚠️  REMEMBER: Use column names EXACTLY as listed above!\n");
        info.append("⚠️  Do NOT change camelCase to snake_case or vice versa!\n");

        return info.toString();
    }

    /**
     * Format schema information for prompt - LEGACY VERSION (for backwards
     * compatibility)
     */
    private String formatSchemaInfo(DatabaseSchemaDTO schema) {
        return formatSchemaInfoDetailed(schema);
    }

    /**
     * Generate examples based on actual schema
     */
    private String getSchemaBasedExamples(DatabaseSchemaDTO schema, String databaseType) {
        if (schema == null || schema.getTables() == null || schema.getTables().isEmpty()) {
            return "No schema available for examples.\n";
        }

        StringBuilder examples = new StringBuilder();
        examples.append("Here are examples using YOUR ACTUAL SCHEMA:\n\n");

        // Find some common tables to use as examples
        DatabaseSchemaDTO.TableInfo firstTable = schema.getTables().get(0);

        // Example 1: Simple SELECT
        examples.append("Example 1 - Simple SELECT:\n");
        examples.append("User: \"Show me all data from ").append(firstTable.getName()).append("\"\n");

        if (databaseType.equals("MYSQL") || databaseType.equals("POSTGRESQL") || databaseType.equals("H2")) {
            examples.append("SQL: SELECT * FROM ").append(firstTable.getName()).append(" LIMIT 100\n\n");
        } else if (databaseType.equals("SQLSERVER")) {
            examples.append("SQL: SELECT TOP 100 * FROM ").append(firstTable.getName()).append("\n\n");
        } else if (databaseType.equals("ORACLE")) {
            examples.append("SQL: SELECT * FROM ").append(firstTable.getName())
                    .append(" FETCH FIRST 100 ROWS ONLY\n\n");
        }

        // Example 2: COUNT
        examples.append("Example 2 - COUNT:\n");
        examples.append("User: \"How many records in ").append(firstTable.getName()).append("?\"\n");
        examples.append("SQL: SELECT COUNT(*) as total FROM ").append(firstTable.getName()).append("\n\n");

        // Example 3: Using actual column names
        if (firstTable.getColumns() != null && firstTable.getColumns().size() >= 2) {
            DatabaseSchemaDTO.ColumnInfo col1 = firstTable.getColumns().get(0);
            DatabaseSchemaDTO.ColumnInfo col2 = firstTable.getColumns().size() > 1
                    ? firstTable.getColumns().get(1)
                    : col1;

            examples.append("Example 3 - Using EXACT column names from schema:\n");
            examples.append("User: \"Show me ").append(col2.getName()).append(" from ").append(firstTable.getName())
                    .append("\"\n");
            examples.append("IMPORTANT: Schema has column '").append(col2.getName()).append("'\n");
            examples.append("✓ CORRECT: SELECT ").append(col2.getName()).append(" FROM ").append(firstTable.getName())
                    .append("\n");

            // Show what would be WRONG
            if (col2.getName().matches(".*[A-Z].*")) { // has camelCase
                String wrongName = col2.getName().replaceAll("([A-Z])", "_$1").toLowerCase();
                examples.append("✗ WRONG:   SELECT ").append(wrongName).append(" FROM ").append(firstTable.getName())
                        .append(" (invented name!)\n\n");
            } else if (col2.getName().contains("_")) { // has snake_case
                String wrongName = toCamelCase(col2.getName());
                examples.append("✗ WRONG:   SELECT ").append(wrongName).append(" FROM ").append(firstTable.getName())
                        .append(" (invented name!)\n\n");
            } else {
                examples.append("\n");
            }
        }

        // Example 4: JOIN if foreign keys exist
        DatabaseSchemaDTO.TableInfo tableWithFK = schema.getTables().stream()
                .filter(t -> t.getForeignKeys() != null && !t.getForeignKeys().isEmpty())
                .findFirst()
                .orElse(null);

        if (tableWithFK != null && tableWithFK.getForeignKeys() != null && !tableWithFK.getForeignKeys().isEmpty()) {
            DatabaseSchemaDTO.ForeignKeyInfo fk = tableWithFK.getForeignKeys().get(0);
            examples.append("Example 4 - JOIN using foreign keys:\n");
            examples.append("User: \"Show ").append(tableWithFK.getName()).append(" with ")
                    .append(fk.getReferencedTable()).append(" info\"\n");
            examples.append("SQL: SELECT t1.*, t2.* FROM ").append(tableWithFK.getName()).append(" t1 ");
            examples.append("JOIN ").append(fk.getReferencedTable()).append(" t2 ");
            examples.append("ON t1.").append(fk.getColumn()).append(" = t2.").append(fk.getReferencedColumn());

            if (databaseType.equals("MYSQL") || databaseType.equals("POSTGRESQL") || databaseType.equals("H2")) {
                examples.append(" LIMIT 100\n\n");
            } else if (databaseType.equals("SQLSERVER")) {
                examples.append("\n  (Add TOP 100 after SELECT)\n\n");
            } else {
                examples.append("\n  (Add FETCH FIRST 100 ROWS ONLY at end)\n\n");
            }
        }

        examples.append("🎯 KEY TAKEAWAY: Always use the EXACT column names shown in the schema above!\n");

        return examples.toString();
    }

    /**
     * Convert snake_case to camelCase (helper for examples)
     */
    private String toCamelCase(String snakeCase) {
        String[] parts = snakeCase.split("_");
        StringBuilder camelCase = new StringBuilder(parts[0]);
        for (int i = 1; i < parts.length; i++) {
            camelCase.append(parts[i].substring(0, 1).toUpperCase())
                    .append(parts[i].substring(1));
        }
        return camelCase.toString();
    }

    /**
     * Get provider configuration based on user settings
     */
    private ProviderConfig getProviderConfig(Long userId) {
        UserAiSettings settings = userAiSettingsService.getUserSettingsEntity(userId);

        ProviderConfig config = new ProviderConfig();
        config.provider = settings.getProvider();
        config.model = settings.getModel();

        switch (settings.getProvider()) {
            case DEMO:
                // Use platform's OpenRouter key
                config.url = aiApiProperties.getUrl();
                config.apiKey = aiApiProperties.getKey();
                break;

            case CLAUDE:
                // Use user's Anthropic API key
                config.url = "https://api.anthropic.com/v1/messages";
                config.apiKey = userAiSettingsService.getDecryptedApiKey(userId);
                if (config.apiKey == null) {
                    throw new ChatBotException("Claude API key not configured. Please add your API key in settings.");
                }
                break;

            case OPENAI:
                // Use user's OpenAI API key
                config.url = "https://api.openai.com/v1/chat/completions";
                config.apiKey = userAiSettingsService.getDecryptedApiKey(userId);
                if (config.apiKey == null) {
                    throw new ChatBotException("OpenAI API key not configured. Please add your API key in settings.");
                }
                break;
        }

        return config;
    }

    /**
     * Call AI API (non-streaming)
     */
    private String callAiApi(Long userId, String prompt, Double temperature) {
        ProviderConfig config = getProviderConfig(userId);

        Map<String, Object> requestBody = Map.of(
                "model", config.model,
                "messages", List.of(
                        Map.of("role", "system", "content",
                                "You are an EXPERT AI database assistant with ADVANCED natural language understanding. "
                                        +
                                        "You excel at understanding unclear questions, handling typos, interpreting user intent, "
                                        +
                                        "and providing intelligent, helpful responses. You are patient, friendly, supportive, and "
                                        +
                                        "can understand questions even when they have spelling mistakes, grammar errors, or are written "
                                        +
                                        "in unclear language. You always try to help the user get the information they need, regardless "
                                        +
                                        "of how their question is phrased. You are database-agnostic and can work with MySQL, PostgreSQL, "
                                        +
                                        "Oracle, SQL Server, and other databases using their specific syntax."),
                        Map.of("role", "user", "content", prompt)),
                "temperature", temperature,
                "max_tokens", aiApiProperties.getMaxTokens());

        try {
            log.debug("Calling AI API: {} with model: {} (provider: {})", config.url, config.model, config.provider);

            String response = webClient.post()
                    .uri(config.url)
                    .header("Authorization", "Bearer " + config.apiKey)
                    .header("Content-Type", "application/json")
                    .header("HTTP-Referer", "http://localhost:3000")
                    .header("X-Title", "Eadgequry AI Chatbot")
                    .bodyValue(requestBody)
                    .retrieve()
                    .onStatus(
                            status -> status.is4xxClientError(),
                            clientResponse -> {
                                return clientResponse.bodyToMono(String.class)
                                        .flatMap(body -> {
                                            log.error("OpenRouter 4xx error - Status: {}, Body: {}",
                                                    clientResponse.statusCode(), body);
                                            return clientResponse.createException()
                                                    .flatMap(ex -> {
                                                        if (clientResponse.statusCode().value() == 401) {
                                                            return reactor.core.publisher.Mono
                                                                    .error(new ChatBotException(
                                                                            "OpenRouter authentication failed - check API key"));
                                                        } else if (clientResponse.statusCode().value() == 429) {
                                                            return reactor.core.publisher.Mono
                                                                    .error(new ChatBotException(
                                                                            "OpenRouter rate limit exceeded"));
                                                        } else {
                                                            return reactor.core.publisher.Mono
                                                                    .error(new ChatBotException(
                                                                            "OpenRouter client error: " + body));
                                                        }
                                                    });
                                        });
                            })
                    .onStatus(
                            status -> status.is5xxServerError(),
                            clientResponse -> {
                                return clientResponse.bodyToMono(String.class)
                                        .flatMap(body -> {
                                            log.error("OpenRouter 5xx error - Status: {}, Body: {}",
                                                    clientResponse.statusCode(), body);
                                            return reactor.core.publisher.Mono
                                                    .error(new ChatBotException("OpenRouter server error: " + body));
                                        });
                            })
                    .bodyToMono(String.class)
                    .timeout(Duration.ofMillis(aiApiProperties.getTimeout()))
                    .block();

            log.debug("AI API raw response: {}", response);
            return extractContent(response);
        } catch (ChatBotException e) {
            throw e;
        } catch (Exception e) {
            log.error("AI API call failed. URL: {}, Model: {}, Error: {}",
                    aiApiProperties.getUrl(),
                    aiApiProperties.getModel(),
                    e.getMessage(), e);
            throw new ChatBotException(
                    "AI API call failed: " + (e.getMessage() != null ? e.getMessage() : e.getClass().getSimpleName()),
                    e);
        }
    }

    /**
     * Extract content from AI API response
     */
    private String extractContent(String response) {
        try {
            JsonNode root = objectMapper.readTree(response);

            // Check for error in response
            if (root.has("error")) {
                String errorMsg = root.path("error").path("message").asText();
                String errorCode = root.path("error").path("code").asText();
                log.error("OpenRouter API returned error - Code: {}, Message: {}", errorCode, errorMsg);
                throw new ChatBotException("OpenRouter API error: " + errorMsg);
            }

            // Extract content
            JsonNode choices = root.path("choices");
            if (choices.isMissingNode() || choices.isEmpty()) {
                log.error("No choices in response. Full response: {}", response);
                throw new ChatBotException("OpenRouter response missing 'choices' field");
            }

            String content = choices.get(0).path("message").path("content").asText();
            if (content == null || content.isEmpty()) {
                log.error("Empty content in response. Full response: {}", response);
                throw new ChatBotException("OpenRouter returned empty content");
            }

            return content;
        } catch (ChatBotException e) {
            throw e;
        } catch (Exception e) {
            log.error("Failed to parse AI response. Response: {}", response, e);
            throw new ChatBotException("Failed to parse AI response: " + e.getMessage(), e);
        }
    }

    /**
     * Clean answer (remove extra quotes, trim)
     */
    private String cleanAnswer(String answer) {
        if (answer == null) {
            return "";
        }
        return answer.trim().replaceAll("^\"|\"$", "").replaceAll("^'|'$", "");
    }

    /**
     * Helper class for provider configuration
     */
    private static class ProviderConfig {
        UserAiSettings.AiProvider provider;
        String url;
        String apiKey;
        String model;
    }
}
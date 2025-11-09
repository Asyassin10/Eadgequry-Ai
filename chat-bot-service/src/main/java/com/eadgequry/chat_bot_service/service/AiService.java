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
import java.util.List;
import java.util.Map;

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
        if (lowerQuestion.matches("^(hi|hello|hey|good morning|good afternoon|good evening|bonjour|hola|salut|ciao).*")) {
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
            !lowerQuestion.matches(".*(table|database|query|select|data|record|row|column|customer|order|product|employee).*")) {
            return "I'm specifically designed to help you with your database queries. I can answer questions about your data, tables, and records. " +
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
     * Generate SQL query from natural language question
     */
    public String generateSqlQuery(Long userId, String question, DatabaseSchemaDTO schema, String previousError) {
        String prompt = buildQueryPrompt(question, schema, previousError);

        try {
            String response = callAiApi(userId, prompt, aiApiProperties.getTemperatureQuery());
            log.debug("AI generated SQL query: {}", response);
            return response;
        } catch (Exception e) {
            log.error("Failed to generate SQL query", e);
            throw new ChatBotException("Failed to generate SQL query: " + e.getMessage(), e);
        }
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

    /**
     * Generate streaming answer from SQL results
     */
    public Flux<String> generateStreamingAnswer(Long userId, String question, String sqlQuery, List<Map<String, Object>> result) {
        String prompt = buildAnswerPrompt(question, sqlQuery, result);

        return callAiApiStreaming(userId, prompt, aiApiProperties.getTemperatureAnswer())
                .map(this::cleanAnswer)
                .onErrorResume(e -> {
                    log.error("Failed to generate streaming answer", e);
                    return Flux.just("Error: " + e.getMessage());
                });
    }

    /**
     * Build advanced intelligent prompt for SQL query generation
     */
    private String buildQueryPrompt(String question, DatabaseSchemaDTO schema, String previousError) {
        StringBuilder prompt = new StringBuilder();

        // Get database type
        String databaseType = schema != null && schema.getDatabaseType() != null
            ? schema.getDatabaseType().toUpperCase()
            : "UNKNOWN";

        prompt.append("You are an EXPERT SQL query generator with WORLD-CLASS natural language understanding and PERFECT schema analysis skills.\n\n");

        prompt.append("=== CRITICAL RULE #1: EXACT TABLE AND COLUMN NAMES ===\n");
        prompt.append("🚨 NEVER INVENT, GUESS, OR MODIFY TABLE/COLUMN NAMES! 🚨\n");
        prompt.append("• You MUST use the EXACT names from the schema below (EXACT case, EXACT spelling)\n");
        prompt.append("• Table names: If schema has 'orderdetails', use 'orderdetails' NOT 'orderDetails' or 'order_details'\n");
        prompt.append("• Column names: If schema has 'buyPrice', use 'buyPrice' NOT 'buy_price' or 'BuyPrice'\n");
        prompt.append("• Names may be: camelCase, snake_case, PascalCase, lowercase, UPPERCASE, or mixed\n");
        prompt.append("• DO NOT convert between formats - copy EXACTLY from schema\n");
        prompt.append("• WRONG: FROM orderDetails  ✗ (if schema has 'orderdetails')\n");
        prompt.append("• RIGHT:  FROM orderdetails  ✓\n");
        prompt.append("• WRONG: WHERE buy_price > 100  ✗ (if schema has 'buyPrice')\n");
        prompt.append("• RIGHT:  WHERE buyPrice > 100  ✓\n\n");

        prompt.append("=== TARGET DATABASE ===\n");
        prompt.append("Database Type: ").append(databaseType).append("\n");
        prompt.append("Database Name: ").append(schema != null ? schema.getDatabaseName() : "unknown").append("\n");
        prompt.append("CRITICAL: Generate SQL using ").append(databaseType).append(" specific syntax!\n\n");

        prompt.append("=== YOUR CAPABILITIES ===\n");
        prompt.append("✓ Understand questions with typos, grammar errors, and unclear language\n");
        prompt.append("✓ Interpret user intent even from messy or incomplete questions\n");
        prompt.append("✓ Handle questions in different languages or mixed languages\n");
        prompt.append("✓ Infer missing information from context\n");
        prompt.append("✓ Be tolerant of spelling mistakes in user questions\n");
        prompt.append("✓ Match fuzzy table/column names to actual schema\n");
        prompt.append("✓ Understand abbreviations and SQL slang\n");
        prompt.append("✗ BUT NEVER invent column names - always use EXACT names from schema\n\n");

        prompt.append("=== QUERY GENERATION PROCESS ===\n");
        prompt.append("STEP 1: ANALYZE the user question and understand intent (be tolerant of typos)\n");
        prompt.append("STEP 2: IDENTIFY which tables are needed by fuzzy matching user's words to table names\n");
        prompt.append("STEP 3: READ the schema and find the EXACT column names in those tables\n");
        prompt.append("STEP 4: MAP user's intent to exact schema columns (e.g., 'price' → find 'buyPrice' or 'MSRP')\n");
        prompt.append("STEP 5: CONSTRUCT SQL using database-specific syntax and EXACT column names\n");
        prompt.append("STEP 6: VALIDATE that all column names in your query exist in the schema\n\n");

        prompt.append("=== UNDERSTANDING USER INTENT ===\n");
        prompt.append("Common Phrases (understand these even with typos):\n");
        prompt.append("• 'give me' / 'show me' / 'get me' / 'list' → SELECT\n");
        prompt.append("• 'how many' / 'count' / 'total number' → COUNT(*)\n");
        prompt.append("• 'latest' / 'newest' / 'recent' → ORDER BY date_column DESC\n");
        prompt.append("• 'oldest' / 'first' → ORDER BY date_column ASC\n");
        prompt.append("• 'top N' / 'first N' → LIMIT N (or TOP N for SQL Server)\n");
        prompt.append("• 'all' → SELECT * LIMIT 100\n");
        prompt.append("• 'find' / 'search' / 'lookup' → WHERE with LIKE or =\n");
        prompt.append("• 'total' / 'sum' / 'revenue' → SUM(column)\n");
        prompt.append("• 'average' / 'avg' / 'mean' → AVG(column)\n");
        prompt.append("• 'above average' → WHERE column > (SELECT AVG(column) ...)\n");
        prompt.append("• 'never ordered' / 'not in' / 'haven't' → NOT IN (subquery) or LEFT JOIN WHERE NULL\n");
        prompt.append("• 'who reports to X' → self-join or WHERE reportsTo = (subquery)\n");
        prompt.append("• 'for each X' / 'per X' / 'by X' → GROUP BY with aggregations\n");
        prompt.append("• 'total quantity ordered' → SUM(quantityOrdered)\n");
        prompt.append("• 'total revenue' / 'total value' → SUM(quantity * price)\n");
        prompt.append("• 'most recent' / 'last order' → MAX(date_column)\n");
        prompt.append("• 'lifetime value' → SUM across all related records\n");
        prompt.append("• 'in the last N months/days' → WHERE date > DATE_SUB(NOW(), INTERVAL N MONTH/DAY)\n\n");

        prompt.append("=== ADVANCED QUERY PATTERNS ===\n");
        prompt.append("You are EXPERT at complex SQL queries:\n");
        prompt.append("• JOINS: Use INNER JOIN, LEFT JOIN, RIGHT JOIN when combining tables\n");
        prompt.append("• GROUP BY: When user says 'for each', 'per', 'by' → use GROUP BY\n");
        prompt.append("• AGGREGATIONS: COUNT(), SUM(), AVG(), MAX(), MIN(), GROUP_CONCAT()\n");
        prompt.append("• SUBQUERIES: Use for 'above average', 'never ordered', complex filtering\n");
        prompt.append("• CALCULATIONS: Can multiply columns (quantity * price for revenue)\n");
        prompt.append("• ALIASES: Always use meaningful aliases (AS total_revenue, AS order_count)\n");
        prompt.append("• HAVING: Filter aggregated results with HAVING (not WHERE)\n\n");

        prompt.append("COMPLEX QUERY EXAMPLES:\n");
        prompt.append("1. \"For each product, total quantity and revenue\"\n");
        prompt.append("   → SELECT p.productName, SUM(od.quantityOrdered) as total_qty,\n");
        prompt.append("      SUM(od.quantityOrdered * od.priceEach) as revenue\n");
        prompt.append("      FROM products p JOIN orderdetails od ON p.productCode = od.productCode\n");
        prompt.append("      GROUP BY p.productCode, p.productName\n\n");

        prompt.append("2. \"Customers with total orders and lifetime value\"\n");
        prompt.append("   → SELECT c.customerName, COUNT(o.orderNumber) as order_count,\n");
        prompt.append("      MAX(o.orderDate) as last_order, SUM(p.amount) as lifetime_value\n");
        prompt.append("      FROM customers c LEFT JOIN orders o ON c.customerNumber = o.customerNumber\n");
        prompt.append("      LEFT JOIN payments p ON c.customerNumber = p.customerNumber\n");
        prompt.append("      GROUP BY c.customerNumber, c.customerName\n\n");

        prompt.append("3. \"Products never ordered\"\n");
        prompt.append("   → SELECT productName FROM products\n");
        prompt.append("      WHERE productCode NOT IN (SELECT DISTINCT productCode FROM orderdetails)\n\n");

        prompt.append("=== FUZZY TABLE/COLUMN MATCHING ===\n");
        prompt.append("User says fuzzy name → Find closest match in schema:\n");
        prompt.append("• 'usr' / 'users' / 'user' → match to 'users' table\n");
        prompt.append("• 'price' → find 'buyPrice', 'MSRP', 'priceEach', etc.\n");
        prompt.append("• 'name' → find 'productName', 'customerName', 'firstName', 'lastName', etc.\n");
        prompt.append("• 'employee' → find 'employees' table with 'employeeNumber'\n");
        prompt.append("• 'customer' → find 'customers' table with 'customerNumber' (NOT 'customer_id'!)\n");
        prompt.append("• 'payment' → find 'payments' table with 'amount', 'customerNumber'\n\n");

        prompt.append("=== STRICT OUTPUT RULES ===\n");
        prompt.append("1. Return ONLY the SQL query - NO explanations, NO comments, NO markdown\n");
        prompt.append("2. Query MUST use ").append(databaseType).append(" specific syntax\n");
        prompt.append("3. Query MUST use EXACT column names from schema (never invent names)\n");
        prompt.append("4. ONLY SELECT queries - NO INSERT, UPDATE, DELETE, DROP, CREATE, ALTER, TRUNCATE\n");
        prompt.append("5. Use proper quotes for identifiers based on database type\n");
        prompt.append("6. All parentheses and quotes must be properly closed\n\n");

        // Add database-specific syntax rules
        prompt.append("=== ").append(databaseType).append(" SPECIFIC SYNTAX ===\n");
        prompt.append(getDatabaseSpecificSyntax(databaseType));
        prompt.append("\n");

        if (previousError != null) {
            prompt.append("=== ⚠️ PREVIOUS ERROR - YOU MUST FIX THIS ===\n");
            prompt.append("Error: ").append(previousError).append("\n\n");
            prompt.append("ANALYSIS REQUIRED:\n");
            prompt.append("• If error says 'Unknown column': You used wrong column name - check schema for EXACT name\n");
            prompt.append("• If error says 'Unknown table': You used wrong table name - check schema for EXACT name\n");
            prompt.append("• If error says syntax error: Check ").append(databaseType).append(" syntax rules\n");
            prompt.append("• Review the schema below and use EXACT names, not invented ones\n\n");
        }

        // Add schema information with emphasis
        prompt.append("=== 📋 DATABASE SCHEMA (USE EXACT NAMES FROM HERE) ===\n");
        prompt.append(formatSchemaInfoDetailed(schema));
        prompt.append("\n");

        prompt.append("=== REAL-WORLD EXAMPLES ===\n");
        prompt.append(getSchemaBasedExamples(schema, databaseType));
        prompt.append("\n");

        prompt.append("=== 👤 USER QUESTION ===\n");
        prompt.append("\"").append(question).append("\"\n\n");

        prompt.append("=== YOUR TASK ===\n");
        prompt.append("1. Understand what the user wants (be tolerant of typos in their question)\n");
        prompt.append("2. Find the relevant tables and columns from the EXACT schema above\n");
        prompt.append("3. Generate a syntactically perfect ").append(databaseType).append(" query\n");
        prompt.append("4. Use ONLY column names that exist in the schema (EXACT spelling, EXACT case)\n");
        prompt.append("5. Double-check: every column in your query MUST be in the schema above\n\n");

        prompt.append("Generate the SQL query now (ONLY the query, nothing else):\n\n");

        return prompt.toString();
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
     * Build intelligent prompt for answer generation with user-friendly explanations
     */
    private String buildAnswerPrompt(String question, String sqlQuery, List<Map<String, Object>> result) {
        StringBuilder prompt = new StringBuilder();

        prompt.append("You are Eadge Query AI Assistant - an EXPERT, FRIENDLY, and INTELLIGENT database assistant.\n\n");

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
            prompt.append("     'Showing 50 of X results (maximum display limit). To see specific data, try refining your search.'\n");
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
            prompt.append("You have 3 active users. Would you like to see inactive users or filter by a specific criteria?\n\n");
        }

        prompt.append("=== YOUR TASK ===\n");
        prompt.append("Generate a HELPFUL, FRIENDLY, and WELL-FORMATTED response.\n");
        prompt.append("Remember: Be understanding and supportive. The user might have asked an unclear question, but you understood it!\n\n");

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
        info.append("=" .repeat(80)).append("\n\n");

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

        info.append("=" .repeat(80)).append("\n");
        info.append("⚠️  REMEMBER: Use column names EXACTLY as listed above!\n");
        info.append("⚠️  Do NOT change camelCase to snake_case or vice versa!\n");

        return info.toString();
    }

    /**
     * Format schema information for prompt - LEGACY VERSION (for backwards compatibility)
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
            examples.append("SQL: SELECT * FROM ").append(firstTable.getName()).append(" FETCH FIRST 100 ROWS ONLY\n\n");
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
            examples.append("User: \"Show me ").append(col2.getName()).append(" from ").append(firstTable.getName()).append("\"\n");
            examples.append("IMPORTANT: Schema has column '").append(col2.getName()).append("'\n");
            examples.append("✓ CORRECT: SELECT ").append(col2.getName()).append(" FROM ").append(firstTable.getName()).append("\n");

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
            examples.append("User: \"Show ").append(tableWithFK.getName()).append(" with ").append(fk.getReferencedTable()).append(" info\"\n");
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
                            "You are an EXPERT AI database assistant with ADVANCED natural language understanding. " +
                            "You excel at understanding unclear questions, handling typos, interpreting user intent, " +
                            "and providing intelligent, helpful responses. You are patient, friendly, supportive, and " +
                            "can understand questions even when they have spelling mistakes, grammar errors, or are written " +
                            "in unclear language. You always try to help the user get the information they need, regardless " +
                            "of how their question is phrased. You are database-agnostic and can work with MySQL, PostgreSQL, " +
                            "Oracle, SQL Server, and other databases using their specific syntax."),
                        Map.of("role", "user", "content", prompt)
                ),
                "temperature", temperature,
                "max_tokens", aiApiProperties.getMaxTokens()
        );

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
                                            log.error("OpenRouter 4xx error - Status: {}, Body: {}", clientResponse.statusCode(), body);
                                            return clientResponse.createException()
                                                    .flatMap(ex -> {
                                                        if (clientResponse.statusCode().value() == 401) {
                                                            return reactor.core.publisher.Mono.error(new ChatBotException("OpenRouter authentication failed - check API key"));
                                                        } else if (clientResponse.statusCode().value() == 429) {
                                                            return reactor.core.publisher.Mono.error(new ChatBotException("OpenRouter rate limit exceeded"));
                                                        } else {
                                                            return reactor.core.publisher.Mono.error(new ChatBotException("OpenRouter client error: " + body));
                                                        }
                                                    });
                                        });
                            }
                    )
                    .onStatus(
                            status -> status.is5xxServerError(),
                            clientResponse -> {
                                return clientResponse.bodyToMono(String.class)
                                        .flatMap(body -> {
                                            log.error("OpenRouter 5xx error - Status: {}, Body: {}", clientResponse.statusCode(), body);
                                            return reactor.core.publisher.Mono.error(new ChatBotException("OpenRouter server error: " + body));
                                        });
                            }
                    )
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
            throw new ChatBotException("AI API call failed: " + (e.getMessage() != null ? e.getMessage() : e.getClass().getSimpleName()), e);
        }
    }

    /**
     * Call AI API with streaming
     */
    private Flux<String> callAiApiStreaming(Long userId, String prompt, Double temperature) {
        ProviderConfig config = getProviderConfig(userId);

        Map<String, Object> requestBody = Map.of(
                "model", config.model,
                "messages", List.of(
                        Map.of("role", "system", "content",
                            "You are an EXPERT AI database assistant with ADVANCED natural language understanding. " +
                            "You excel at understanding unclear questions, handling typos, interpreting user intent, " +
                            "and providing intelligent, helpful responses. You are patient, friendly, supportive, and " +
                            "can understand questions even when they have spelling mistakes, grammar errors, or are written " +
                            "in unclear language. You always try to help the user get the information they need, regardless " +
                            "of how their question is phrased. You are database-agnostic and can work with MySQL, PostgreSQL, " +
                            "Oracle, SQL Server, and other databases using their specific syntax."),
                        Map.of("role", "user", "content", prompt)
                ),
                "temperature", temperature,
                "max_tokens", aiApiProperties.getMaxTokens(),
                "stream", true
        );

        return webClient.post()
                .uri(config.url)
                .header("Authorization", "Bearer " + config.apiKey)
                .header("Content-Type", "application/json")
                .header("HTTP-Referer", "http://localhost:3000")  // OpenRouter recommended header
                .header("X-Title", "Eadgequry AI Chatbot")        // OpenRouter recommended header
                .bodyValue(requestBody)
                .retrieve()
                .bodyToFlux(String.class)
                .timeout(Duration.ofMillis(aiApiProperties.getTimeout()))
                .map(this::extractStreamingContent)
                .filter(content -> content != null && !content.isEmpty());
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
     * Extract content from streaming response
     */
    private String extractStreamingContent(String chunk) {
        try {
            if (chunk.startsWith("data: ")) {
                chunk = chunk.substring(6).trim();
            }
            if (chunk.equals("[DONE]")) {
                return "";
            }
            JsonNode root = objectMapper.readTree(chunk);
            JsonNode delta = root.path("choices").get(0).path("delta");
            return delta.path("content").asText("");
        } catch (Exception e) {
            log.warn("Failed to parse streaming chunk: {}", chunk, e);
            return "";
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

package com.eadgequry.chat_bot_service.service;

import com.eadgequry.chat_bot_service.config.AiApiProperties;
import com.eadgequry.chat_bot_service.dto.DatabaseSchemaDTO;
import com.eadgequry.chat_bot_service.exception.ChatBotException;
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

    /**
     * Check if question is a greeting or non-database question
     * Returns friendly response if yes, null if it's a database question
     */
    public String handleNonDatabaseQuestion(String question) {
        String lowerQuestion = question.toLowerCase().trim();

        // Handle greetings
        if (lowerQuestion.matches("^(hi|hello|hey|good morning|good afternoon|good evening).*")) {
            return "Hello! I'm your AI assistant for Eadge Query. I'm here to help you query and analyze your database using natural language. Just ask me anything about your data!";
        }

        // Handle "who are you" type questions
        if (lowerQuestion.matches(".*(who are you|what are you|what can you do|help).*")) {
            return "I'm an AI-powered database assistant for Eadge Query. I can help you:\n\n" +
                   "• Query your database using natural language\n" +
                   "• Generate and execute SQL queries automatically\n" +
                   "• Analyze and present your data in easy-to-read tables\n\n" +
                   "Just ask me questions like 'Show all users' or 'Count total orders' and I'll handle the rest!";
        }

        // Check if question seems unrelated to database
        if (lowerQuestion.matches(".*(weather|news|joke|game|movie|music|recipe|time|date).*") &&
            !lowerQuestion.matches(".*(table|database|query|select|data|record|row|column).*")) {
            return "I'm specifically designed to help you with your database queries. I can answer questions about your data, tables, and records. " +
                   "Please ask me something about your database, like 'Show all users' or 'Count records in orders table'.";
        }

        return null; // It's a database question
    }

    /**
     * Generate SQL query from natural language question
     */
    public String generateSqlQuery(String question, DatabaseSchemaDTO schema, String previousError) {
        String prompt = buildQueryPrompt(question, schema, previousError);

        try {
            String response = callAiApi(prompt, aiApiProperties.getTemperatureQuery());
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
    public String generateAnswer(String question, String sqlQuery, List<Map<String, Object>> result) {
        String prompt = buildAnswerPrompt(question, sqlQuery, result);

        try {
            String response = callAiApi(prompt, aiApiProperties.getTemperatureAnswer());
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
    public Flux<String> generateStreamingAnswer(String question, String sqlQuery, List<Map<String, Object>> result) {
        String prompt = buildAnswerPrompt(question, sqlQuery, result);

        return callAiApiStreaming(prompt, aiApiProperties.getTemperatureAnswer())
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

        prompt.append("You are an EXPERT database query assistant with ADVANCED natural language understanding.\n\n");

        prompt.append("=== YOUR CAPABILITIES ===\n");
        prompt.append("• Understand questions with typos, grammar errors, and unclear language\n");
        prompt.append("• Interpret user intent even from messy or incomplete questions\n");
        prompt.append("• Handle questions in different languages or mixed languages\n");
        prompt.append("• Infer missing information from context and database schema\n");
        prompt.append("• Be tolerant of spelling mistakes in table/column names\n");
        prompt.append("• Understand abbreviations and common SQL slang\n\n");

        prompt.append("=== TARGET DATABASE ===\n");
        prompt.append("Database Type: ").append(databaseType).append("\n");
        prompt.append("CRITICAL: Generate SQL using ").append(databaseType).append(" specific syntax!\n\n");

        prompt.append("=== INTELLIGENCE GUIDELINES ===\n");
        prompt.append("1. UNDERSTAND INTENT: If user asks 'shwo all usr' → interpret as 'show all users'\n");
        prompt.append("2. HANDLE TYPOS: Match table/column names even with spelling mistakes\n");
        prompt.append("3. BE SMART: 'how many' = COUNT, 'latest' = ORDER BY date DESC LIMIT, 'top 10' = LIMIT 10\n");
        prompt.append("4. INFER CONTEXT: If user says 'active ones', look for status/active/enabled columns\n");
        prompt.append("5. FUZZY MATCHING: If exact table doesn't exist, find closest match (users vs user vs usr)\n");
        prompt.append("6. COMMON PHRASES:\n");
        prompt.append("   - 'give me' / 'show me' / 'get me' → SELECT\n");
        prompt.append("   - 'how many' / 'count' → COUNT(*)\n");
        prompt.append("   - 'latest' / 'newest' / 'recent' → ORDER BY date/created_at DESC\n");
        prompt.append("   - 'oldest' / 'first' → ORDER BY date/created_at ASC\n");
        prompt.append("   - 'top N' / 'first N' → LIMIT N\n");
        prompt.append("   - 'all' → SELECT * (but exclude ID columns)\n");
        prompt.append("   - 'find' / 'search' / 'lookup' → WHERE with LIKE or =\n");
        prompt.append("   - 'total' / 'sum' → SUM() or COUNT(*)\n");
        prompt.append("   - 'average' / 'avg' → AVG()\n\n");

        prompt.append("=== STRICT OUTPUT RULES ===\n");
        prompt.append("1. Return ONLY the SQL query - absolutely NO explanations, comments, or text\n");
        prompt.append("2. Query MUST be syntactically perfect for ").append(databaseType).append("\n");
        prompt.append("3. NEVER include ID columns in SELECT (unless specifically requested)\n");
        prompt.append("4. ONLY SELECT queries - NO INSERT, UPDATE, DELETE, DROP, CREATE, ALTER\n");
        prompt.append("5. All quotes must be properly closed\n");
        prompt.append("6. Use correct ").append(databaseType).append(" syntax (see below)\n\n");

        // Add database-specific syntax rules
        prompt.append("=== ").append(databaseType).append(" SPECIFIC SYNTAX ===\n");
        prompt.append(getDatabaseSpecificSyntax(databaseType));
        prompt.append("\n");

        if (previousError != null) {
            prompt.append("=== PREVIOUS ERROR - FIX THIS ===\n");
            prompt.append("Error: ").append(previousError).append("\n");
            prompt.append("Analyze the error and generate a corrected query.\n\n");
        }

        // Add schema information
        prompt.append("=== DATABASE SCHEMA ===\n");
        prompt.append(formatSchemaInfo(schema));
        prompt.append("\n");

        prompt.append("=== SMART EXAMPLES FOR ").append(databaseType).append(" ===\n");
        prompt.append(getIntelligentExamples(databaseType));
        prompt.append("\n");

        prompt.append("=== USER QUESTION ===\n");
        prompt.append("Question: \"").append(question).append("\"\n\n");

        prompt.append("ANALYZE the question, UNDERSTAND the intent (even with typos/errors), MATCH with schema, and generate the PERFECT SQL query.\n");
        prompt.append("Remember: Be INTELLIGENT and UNDERSTANDING. The user might have typos or unclear language.\n\n");

        prompt.append("SQL Query:");

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
     * Get intelligent examples showing understanding of unclear/messy questions
     */
    private String getIntelligentExamples(String databaseType) {
        StringBuilder examples = new StringBuilder();

        examples.append("Example 1 - Handling Typos:\n");
        examples.append("User: \"shwo all usrs\" (has typos)\n");
        examples.append("Understanding: User wants to 'show all users' from users table\n");

        switch (databaseType.toUpperCase()) {
            case "MYSQL":
                examples.append("SQL: SELECT * FROM users LIMIT 100\n\n");

                examples.append("Example 2 - Understanding Intent:\n");
                examples.append("User: \"gimme top 10 latest ordrs\"\n");
                examples.append("Understanding: 'gimme' = SELECT, 'top 10' = LIMIT 10, 'latest' = ORDER BY DESC\n");
                examples.append("SQL: SELECT * FROM orders ORDER BY created_at DESC LIMIT 10\n\n");

                examples.append("Example 3 - Fuzzy Matching:\n");
                examples.append("User: \"how many activ usr we have?\"\n");
                examples.append("Understanding: 'how many' = COUNT, 'activ' = active, 'usr' = users\n");
                examples.append("SQL: SELECT COUNT(*) as total FROM users WHERE status = 'active'\n\n");
                break;

            case "POSTGRESQL":
                examples.append("SQL: SELECT * FROM users LIMIT 100\n\n");

                examples.append("Example 2 - Case Insensitive Search:\n");
                examples.append("User: \"find usr with nam like john\"\n");
                examples.append("Understanding: Use ILIKE for case-insensitive search in PostgreSQL\n");
                examples.append("SQL: SELECT * FROM users WHERE name ILIKE '%john%'\n\n");

                examples.append("Example 3 - Understanding 'recent':\n");
                examples.append("User: \"giv me recent 5 orders\"\n");
                examples.append("Understanding: 'recent' = ORDER BY date DESC\n");
                examples.append("SQL: SELECT * FROM orders ORDER BY created_at DESC LIMIT 5\n\n");
                break;

            case "SQLSERVER":
                examples.append("SQL: SELECT TOP 100 * FROM users\n\n");

                examples.append("Example 2 - TOP instead of LIMIT:\n");
                examples.append("User: \"show me frist 20 products\"\n");
                examples.append("Understanding: 'frist' = first, use TOP for SQL Server\n");
                examples.append("SQL: SELECT TOP 20 * FROM products\n\n");

                examples.append("Example 3 - Today's records:\n");
                examples.append("User: \"hw many order 2day?\"\n");
                examples.append("Understanding: 'hw many' = COUNT, '2day' = today\n");
                examples.append("SQL: SELECT COUNT(*) as total FROM orders WHERE CAST(created_at AS DATE) = CAST(GETDATE() AS DATE)\n\n");
                break;

            case "ORACLE":
                examples.append("SQL: SELECT * FROM users FETCH FIRST 100 ROWS ONLY\n\n");

                examples.append("Example 2 - FETCH FIRST syntax:\n");
                examples.append("User: \"gime top 15 custmers\"\n");
                examples.append("Understanding: Use FETCH FIRST in Oracle (no LIMIT keyword)\n");
                examples.append("SQL: SELECT * FROM customers FETCH FIRST 15 ROWS ONLY\n\n");

                examples.append("Example 3 - Using ROWNUM:\n");
                examples.append("User: \"show 10 newest employes\"\n");
                examples.append("Understanding: 'newest' = ORDER BY DESC, can use ROWNUM\n");
                examples.append("SQL: SELECT * FROM employees WHERE ROWNUM <= 10 ORDER BY hire_date DESC\n\n");
                break;

            default:
                examples.append("SQL: SELECT * FROM users\n\n");

                examples.append("Example 2 - Understanding Common Phrases:\n");
                examples.append("User: \"cunt all producs\"\n");
                examples.append("Understanding: 'cunt' = count (typo)\n");
                examples.append("SQL: SELECT COUNT(*) as total FROM products\n\n");
                break;
        }

        examples.append("Example - Very Messy Question:\n");
        examples.append("User: \"hw mny usr witg staus actv and emial contaning @gmil?\"\n");
        examples.append("Understanding: Multiple typos but intent is clear\n");
        examples.append("Translation: 'how many users with status active and email containing @gmail'\n");

        switch (databaseType.toUpperCase()) {
            case "POSTGRESQL":
                examples.append("SQL: SELECT COUNT(*) as total FROM users WHERE status = 'active' AND email ILIKE '%@gmail%'\n\n");
                break;
            case "SQLSERVER":
                examples.append("SQL: SELECT COUNT(*) as total FROM users WHERE status = 'active' AND email LIKE '%@gmail%'\n\n");
                break;
            default:
                examples.append("SQL: SELECT COUNT(*) as total FROM users WHERE status = 'active' AND email LIKE '%@gmail%'\n\n");
                break;
        }

        return examples.toString();
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
            prompt.append("   - Show ALL rows if 20 or fewer\n");
            prompt.append("   - If more than 20, show first 15 and say 'Showing 15 of X results. Ask to see more if needed.'\n");
            prompt.append("6. END with helpful summary:\n");
            prompt.append("   - Brief insight about the data (1-2 sentences)\n");
            prompt.append("   - Total count if it's a count query\n");
            prompt.append("   - Suggest related questions they might want to ask\n");
            prompt.append("7. Be CONVERSATIONAL and FRIENDLY (but professional)\n\n");

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
     * Format schema information for prompt
     */
    private String formatSchemaInfo(DatabaseSchemaDTO schema) {
        if (schema == null || schema.getTables() == null) {
            return "Schema information not available";
        }

        StringBuilder info = new StringBuilder();
        info.append("Database: ").append(schema.getDatabaseName()).append("\n");
        info.append("Type: ").append(schema.getDatabaseType()).append("\n\n");

        for (DatabaseSchemaDTO.TableInfo table : schema.getTables()) {
            info.append("Table '").append(table.getName()).append("':\n");

            if (table.getColumns() != null && !table.getColumns().isEmpty()) {
                info.append("  Columns: ");
                info.append(table.getColumns().stream()
                        .map(col -> col.getName() + " (" + col.getType() + ")")
                        .reduce((a, b) -> a + ", " + b)
                        .orElse("none"));
                info.append("\n");
            }

            if (table.getPrimaryKeys() != null && !table.getPrimaryKeys().isEmpty()) {
                info.append("  Primary Keys: ").append(String.join(", ", table.getPrimaryKeys())).append("\n");
            }

            if (table.getForeignKeys() != null && !table.getForeignKeys().isEmpty()) {
                info.append("  Foreign Keys: ");
                for (DatabaseSchemaDTO.ForeignKeyInfo fk : table.getForeignKeys()) {
                    info.append(fk.getColumn()).append(" -> ")
                        .append(fk.getReferencedTable()).append(".")
                        .append(fk.getReferencedColumn()).append("; ");
                }
                info.append("\n");
            }

            info.append("\n");
        }

        return info.toString();
    }

    /**
     * Call AI API (non-streaming)
     */
    private String callAiApi(String prompt, Double temperature) {
        Map<String, Object> requestBody = Map.of(
                "model", aiApiProperties.getModel(),
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
            log.debug("Calling AI API: {} with model: {}", aiApiProperties.getUrl(), aiApiProperties.getModel());

            String response = webClient.post()
                    .uri(aiApiProperties.getUrl())
                    .header("Authorization", "Bearer " + aiApiProperties.getKey())
                    .header("Content-Type", "application/json")
                    .header("HTTP-Referer", "http://localhost:3000")  // OpenRouter recommended header
                    .header("X-Title", "Eadgequry AI Chatbot")        // OpenRouter recommended header
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
    private Flux<String> callAiApiStreaming(String prompt, Double temperature) {
        Map<String, Object> requestBody = Map.of(
                "model", aiApiProperties.getModel(),
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
                .uri(aiApiProperties.getUrl())
                .header("Authorization", "Bearer " + aiApiProperties.getKey())
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
}

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
     * Build simple global prompt for SQL query generation
     */
    private String buildQueryPrompt(String question, DatabaseSchemaDTO schema, String previousError) {
        StringBuilder prompt = new StringBuilder();

        // Get database type
        String databaseType = schema != null && schema.getDatabaseType() != null
            ? schema.getDatabaseType().toUpperCase()
            : "UNKNOWN";

        prompt.append("Generate a SELECT SQL query to answer this question.\n\n");

        prompt.append("TARGET DATABASE TYPE: ").append(databaseType).append("\n");
        prompt.append("IMPORTANT: Use the correct SQL syntax for ").append(databaseType).append(" database!\n\n");

        prompt.append("CRITICAL RULES:\n");
        prompt.append("1. Generate ONLY the SQL query, nothing else\n");
        prompt.append("2. Query MUST be syntactically correct for ").append(databaseType).append("\n");
        prompt.append("3. ALL quotes must be properly closed\n");
        prompt.append("4. Use LIKE with correct quotes: LIKE '%keyword%'\n");
        prompt.append("5. NO explanations, just the SQL query\n");
        prompt.append("6. NEVER return 'id' columns in SELECT\n");
        prompt.append("7. ONLY SELECT queries - NO INSERT, UPDATE, DELETE, DROP, etc.\n\n");

        // Add database-specific syntax rules
        prompt.append("DATABASE-SPECIFIC SYNTAX FOR ").append(databaseType).append(":\n");
        prompt.append(getDatabaseSpecificSyntax(databaseType));
        prompt.append("\n");

        if (previousError != null) {
            prompt.append("PREVIOUS ERROR: ").append(previousError).append("\n");
            prompt.append("Fix this error in the new query.\n\n");
        }

        // Add schema information
        prompt.append("DATABASE SCHEMA:\n");
        prompt.append(formatSchemaInfo(schema));
        prompt.append("\n\n");

        prompt.append("EXAMPLES FOR ").append(databaseType).append(":\n");
        prompt.append(getDatabaseSpecificExamples(databaseType));
        prompt.append("\n");

        prompt.append("Now generate SQL for:\n");
        prompt.append("Question: \"").append(question).append("\"\n");
        prompt.append("SQL:");

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
     * Get database-specific SQL examples
     */
    private String getDatabaseSpecificExamples(String databaseType) {
        StringBuilder examples = new StringBuilder();

        switch (databaseType.toUpperCase()) {
            case "MYSQL":
                examples.append("Question: \"Show first 10 users\"\n");
                examples.append("SQL: SELECT * FROM users LIMIT 10\n\n");
                examples.append("Question: \"Find users created today\"\n");
                examples.append("SQL: SELECT * FROM users WHERE DATE(created_at) = CURDATE()\n\n");
                break;

            case "POSTGRESQL":
                examples.append("Question: \"Show first 10 users\"\n");
                examples.append("SQL: SELECT * FROM users LIMIT 10\n\n");
                examples.append("Question: \"Find users with name containing 'john' (case-insensitive)\"\n");
                examples.append("SQL: SELECT * FROM users WHERE name ILIKE '%john%'\n\n");
                break;

            case "SQLSERVER":
                examples.append("Question: \"Show first 10 users\"\n");
                examples.append("SQL: SELECT TOP 10 * FROM users\n\n");
                examples.append("Question: \"Find users created today\"\n");
                examples.append("SQL: SELECT * FROM users WHERE CAST(created_at AS DATE) = CAST(GETDATE() AS DATE)\n\n");
                break;

            case "ORACLE":
                examples.append("Question: \"Show first 10 users\"\n");
                examples.append("SQL: SELECT * FROM users FETCH FIRST 10 ROWS ONLY\n\n");
                examples.append("Question: \"Find users created today\"\n");
                examples.append("SQL: SELECT * FROM users WHERE TRUNC(created_at) = TRUNC(SYSDATE)\n\n");
                break;

            default:
                examples.append("Question: \"Show all users\"\n");
                examples.append("SQL: SELECT * FROM users\n\n");
                examples.append("Question: \"Count total orders\"\n");
                examples.append("SQL: SELECT COUNT(*) as total FROM orders\n\n");
                break;
        }

        return examples.toString();
    }

    /**
     * Build prompt for answer generation - always returns structured table format
     */
    private String buildAnswerPrompt(String question, String sqlQuery, List<Map<String, Object>> result) {
        StringBuilder prompt = new StringBuilder();

        prompt.append("You are Eadge Query AI Assistant - a professional database assistant.\n\n");
        prompt.append("User Question: \"").append(question).append("\"\n");
        prompt.append("SQL Query: ").append(sqlQuery).append("\n");
        prompt.append("Query Result: ").append(objectMapper.valueToTree(result).toString()).append("\n\n");

        if (result == null || result.isEmpty()) {
            prompt.append("The query returned NO results.\n");
            prompt.append("INSTRUCTIONS:\n");
            prompt.append("- Explain clearly that no data was found\n");
            prompt.append("- Suggest checking the search criteria or table name\n");
            prompt.append("- DO NOT invent data\n\n");
        } else {
            prompt.append("CRITICAL FORMATTING RULES:\n");
            prompt.append("1. ALWAYS present data in MARKDOWN TABLE format\n");
            prompt.append("2. Use proper markdown table syntax: | Column | Column |\n");
            prompt.append("3. Include table headers with column names\n");
            prompt.append("4. NEVER show ID columns or technical fields\n");
            prompt.append("5. Show ALL rows if under 20, otherwise show first 15 and say 'and X more...'\n");
            prompt.append("6. After the table, add a brief summary (1-2 sentences)\n");
            prompt.append("7. Use clear, professional English\n\n");

            prompt.append("EXAMPLE FORMAT:\n");
            prompt.append("Here are the results:\n\n");
            prompt.append("| Name | Email | Status |\n");
            prompt.append("|------|-------|--------|\n");
            prompt.append("| John | john@test.com | Active |\n");
            prompt.append("| Jane | jane@test.com | Active |\n\n");
            prompt.append("Found 2 active users in the database.\n\n");
        }

        prompt.append("Generate the response following the rules above:\n");
        prompt.append("Answer:");

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
                        Map.of("role", "system", "content", "You are a helpful database query assistant."),
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
                        Map.of("role", "system", "content", "You are a helpful database query assistant."),
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

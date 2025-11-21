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
    public Flux<String> generateStreamingAnswer(Long userId, String question, String sqlQuery,
            List<Map<String, Object>> result) {
        String prompt = buildAnswerPrompt(question, sqlQuery, result);

        return callAiApiStreaming(userId, prompt, aiApiProperties.getTemperatureAnswer())
                .map(this::cleanAnswer)
                .onErrorResume(e -> {
                    log.error("Failed to generate streaming answer", e);
                    return Flux.just("Error: " + e.getMessage());
                });
    }

    /**
     * Build concise, optimized prompt for SQL query generation
     * Optimized to use ~60% fewer tokens while maintaining intelligence
     */
    private String buildQueryPrompt(String question, DatabaseSchemaDTO schema, String previousError) {
        StringBuilder prompt = new StringBuilder();

        // Get database type
        String databaseType = schema != null && schema.getDatabaseType() != null
                ? schema.getDatabaseType().toUpperCase()
                : "MYSQL";

        prompt.append("You are an expert SQL generator for ").append(databaseType).append(".\n\n");

        prompt.append("CRITICAL RULES:\n");
        prompt.append("1. Use EXACT table/column names from schema (case-sensitive)\n");
        prompt.append("2. ONLY SELECT queries (no INSERT/UPDATE/DELETE/DROP/ALTER/CREATE/TRUNCATE)\n");
        prompt.append("3. Return ONLY the SQL query (no explanations, no markdown, no code blocks)\n");
        prompt.append("4. Add LIMIT 100 to all queries (or TOP 100 for SQL Server)\n\n");

        // Database-specific syntax (concise)
        prompt.append(databaseType).append(" SYNTAX:\n");
        if ("MYSQL".equals(databaseType)) {
            prompt.append("• Limit: SELECT * FROM table LIMIT 100\n");
            prompt.append("• Backticks: `table`, `column`\n");
        } else if ("POSTGRESQL".equals(databaseType)) {
            prompt.append("• Limit: SELECT * FROM table LIMIT 100\n");
            prompt.append("• Quotes: \"table\", \"column\"\n");
        } else if ("SQLSERVER".equals(databaseType)) {
            prompt.append("• Limit: SELECT TOP 100 * FROM table\n");
            prompt.append("• Brackets: [table], [column]\n");
        } else if ("ORACLE".equals(databaseType)) {
            prompt.append("• Limit: SELECT * FROM table FETCH FIRST 100 ROWS ONLY\n");
            prompt.append("• Quotes: \"table\", \"column\"\n");
        }
        prompt.append("\n");

        // Common patterns (concise)
        prompt.append("COMMON PATTERNS:\n");
        prompt.append("'give me all'/'show all' → SELECT * FROM [first_table] LIMIT 100\n");
        prompt.append("'how many'/'count' → SELECT COUNT(*) as total FROM table\n");
        prompt.append("'for each'/'per' → GROUP BY\n");
        prompt.append("'total'/'sum' → SUM(column)\n");
        prompt.append("'average' → AVG(column)\n");
        prompt.append("'latest' → ORDER BY date DESC LIMIT 10\n\n");

        // Schema (compact format)
        prompt.append("DATABASE SCHEMA:\n");
        prompt.append("DB: ").append(schema != null ? schema.getDatabaseName() : "unknown").append(" (")
                .append(databaseType).append(")\n");
        if (schema != null && schema.getTables() != null && !schema.getTables().isEmpty()) {
            for (DatabaseSchemaDTO.TableInfo table : schema.getTables()) {
                prompt.append("\nTable: ").append(table.getName()).append("\n");
                if (table.getColumns() != null && !table.getColumns().isEmpty()) {
                    prompt.append("  Columns: ");
                    for (int i = 0; i < Math.min(table.getColumns().size(), 20); i++) { // Limit to first 20 columns
                        if (i > 0)
                            prompt.append(", ");
                        DatabaseSchemaDTO.ColumnInfo col = table.getColumns().get(i);
                        prompt.append(col.getName()).append(" (").append(col.getType()).append(")");
                    }
                    if (table.getColumns().size() > 20) {
                        prompt.append(", ... (").append(table.getColumns().size() - 20).append(" more)");
                    }
                    prompt.append("\n");
                }
                if (table.getForeignKeys() != null && !table.getForeignKeys().isEmpty()) {
                    prompt.append("  FK: ");
                    for (int i = 0; i < table.getForeignKeys().size(); i++) {
                        if (i > 0)
                            prompt.append(", ");
                        DatabaseSchemaDTO.ForeignKeyInfo fk = table.getForeignKeys().get(i);
                        prompt.append(fk.getColumn()).append("→").append(fk.getReferencedTable());
                    }
                    prompt.append("\n");
                }
            }
        } else {
            prompt.append("ERROR: No schema provided!\n");
        }
        prompt.append("\n");

        // Previous error (if any)
        if (previousError != null) {
            prompt.append("FIX ERROR:\n");
            prompt.append(previousError).append("\n");
            prompt.append("→ Check: use EXACT names from schema above\n\n");
        }

        // User question
        prompt.append("QUESTION: ").append(question).append("\n\n");

        prompt.append("Generate SQL query using EXACT names from schema (query only, no explanations):\n");

        return prompt.toString();
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
     * Call AI API with streaming
     */
    private Flux<String> callAiApiStreaming(Long userId, String prompt, Double temperature) {
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
                "max_tokens", aiApiProperties.getMaxTokens(),
                "stream", true);

        return webClient.post()
                .uri(config.url)
                .header("Authorization", "Bearer " + config.apiKey)
                .header("Content-Type", "application/json")
                .header("HTTP-Referer", "http://localhost:3000") // OpenRouter recommended header
                .header("X-Title", "Eadgequry AI Chatbot") // OpenRouter recommended header
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
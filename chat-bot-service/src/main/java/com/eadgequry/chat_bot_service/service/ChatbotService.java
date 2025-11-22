package com.eadgequry.chat_bot_service.service;

import com.eadgequry.chat_bot_service.client.DataSourceClient;
import com.eadgequry.chat_bot_service.dto.*;
import com.eadgequry.chat_bot_service.exception.ChatBotException;
import com.eadgequry.chat_bot_service.model.Conversation;
import com.eadgequry.chat_bot_service.model.ConversationSession;
import com.eadgequry.chat_bot_service.repository.ConversationRepository;
import com.eadgequry.chat_bot_service.repository.ConversationSessionRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Simple Chatbot Service - just handles database queries
 * Flow: Question → SQL → Execute → Answer
 */
@Service
@RequiredArgsConstructor
public class ChatbotService {

    private static final Logger log = LoggerFactory.getLogger(ChatbotService.class);

    private final AiService aiService;
    private final SqlValidatorService sqlValidatorService;
    private final DataSourceClient dataSourceClient;
    private final ConversationRepository conversationRepository;
    private final ConversationSessionRepository conversationSessionRepository;
    private final UserAiSettingsService userAiSettingsService;
    private final DemoQueryUsageService demoQueryUsageService;

    @Value("${chatbot.max-retries:2}")
    private int maxRetries;

    /**
     * Simple flow: Question → Generate SQL → Validate → Execute → Generate Answer
     */
    @Transactional
    public ChatResponse ask(ChatRequest request) {
        String sqlQuery = null;
        try {
            String question = request.getQuestion().trim();
            Long userId = request.getUserId();
            Long databaseConfigId = request.getDatabaseConfigId();

            // Check if it's a greeting or non-database question first
            String nonDbResponse = aiService.handleNonDatabaseQuestion(question);
            if (nonDbResponse != null) {
                log.info("Handling non-database question: {}", question);
                return ChatResponse.success(question, null, null, nonDbResponse);
            }

            // Check if user is on DEMO mode and has exceeded daily query limit
            if (userAiSettingsService.isUsingDemoMode(userId)) {
                if (demoQueryUsageService.hasExceededDailyLimit(userId)) {
                    String limitMessage = buildDailyLimitExceededMessage(userId);
                    log.warn("User {} exceeded daily DEMO query limit", userId);
                    return ChatResponse.builder()
                            .success(false)
                            .question(question)
                            .sqlQuery(null)
                            .sqlResult(null)
                            .answer(limitMessage)
                            .error("Daily query limit exceeded")
                            .build();
                }
            }

            // Get database schema
            DatabaseSchemaDTO schema = dataSourceClient.getSchemaByConfigId(databaseConfigId, userId);

            // Generate SQL query with retries
            sqlQuery = generateQueryWithRetries(userId, question, schema);

            // Clean SQL
            sqlQuery = sqlValidatorService.cleanQuery(sqlQuery);

            // Execute query (datasource will validate for security)
            QueryExecutionResponse queryResult = dataSourceClient.executeQuery(databaseConfigId, userId, sqlQuery);

            if (!queryResult.isSuccess()) {
                String errorMsg = queryResult.getError();

                // Check if it's a table/column not found error
                if (isTableOrColumnNotFoundError(errorMsg)) {
                    log.warn("Table or column not found: {}", errorMsg);
                    String friendlyError = buildTableNotFoundResponse(errorMsg, question, sqlQuery, schema);

                    // Save conversation with error
                    String sessionId = getOrCreateSession(userId, databaseConfigId);
                    saveConversation(userId, databaseConfigId, sessionId, question, sqlQuery, null, friendlyError,
                            errorMsg);

                    // Return error response
                    return ChatResponse.builder()
                            .success(false)
                            .question(question)
                            .sqlQuery(sqlQuery)
                            .sqlResult(null)
                            .answer(friendlyError)
                            .error(friendlyError)
                            .build();
                }

                // Check if it's a forbidden keyword error
                if (isForbiddenKeywordError(errorMsg)) {
                    log.warn("Forbidden SQL operation attempted: {}", sqlQuery);
                    String friendlyError = buildForbiddenOperationResponse(errorMsg, schema.getDatabaseType());

                    // Save conversation with error
                    String sessionId = getOrCreateSession(userId, databaseConfigId);
                    saveConversation(userId, databaseConfigId, sessionId, question, sqlQuery, null, friendlyError,
                            errorMsg);

                    // Return error response but include the SQL query so user can see what was
                    // attempted
                    return ChatResponse.builder()
                            .success(false)
                            .question(question)
                            .sqlQuery(sqlQuery) // Show the generated query
                            .sqlResult(null)
                            .answer(friendlyError)
                            .error(friendlyError)
                            .build();
                }

                throw new ChatBotException("Query execution failed: " + errorMsg);
            }

            // Limit results to 50 rows maximum (for display purposes)
            List<Map<String, Object>> limitedResult = limitResults(queryResult.getResult(), 50);

            // Generate answer (AI will mention if results were limited)
            String answer = aiService.generateAnswer(userId, question, sqlQuery, limitedResult);

            // Increment query count for DEMO users
            if (userAiSettingsService.isUsingDemoMode(userId)) {
                demoQueryUsageService.incrementQueryCount(userId);
            }

            // Save conversation
            String sessionId = getOrCreateSession(userId, databaseConfigId);
            saveConversation(userId, databaseConfigId, sessionId, question, sqlQuery, limitedResult, answer, null);

            return ChatResponse.success(question, sqlQuery, limitedResult, answer);

        } catch (Exception e) {
            log.error("Error processing question", e);

            // Handle timeout errors
            if (isTimeoutError(e)) {
                String timeoutMessage = "⏱️ **The AI took too long to process your question.**\n\n" +
                        "This usually happens with very complex questions or when the AI service is slow.\n\n" +
                        "**What you can do:**\n" +
                        "• Try asking a simpler question\n" +
                        "• Break your question into smaller parts\n" +
                        "• Try again in a moment\n" +
                        "• If this keeps happening, please contact support\n\n" +
                        "**Your question:** \"" + request.getQuestion() + "\"";

                return ChatResponse.builder()
                        .success(false)
                        .question(request.getQuestion())
                        .sqlQuery(sqlQuery)
                        .sqlResult(null)
                        .answer(timeoutMessage)
                        .error("Request timeout")
                        .build();
            }

            // If we have a SQL query and it's a forbidden keyword error, handle it
            // gracefully
            if (sqlQuery != null && isForbiddenKeywordError(e.getMessage())) {
                String friendlyError = buildForbiddenOperationResponse(e.getMessage(), "");
                return ChatResponse.builder()
                        .success(false)
                        .question(request.getQuestion())
                        .sqlQuery(sqlQuery)
                        .sqlResult(null)
                        .answer(friendlyError)
                        .error(friendlyError)
                        .build();
            }

            return ChatResponse.error("Error: " + e.getMessage());
        }
    }

    /**
     * Check if exception is a timeout error
     */
    private boolean isTimeoutError(Exception e) {
        if (e == null) {
            return false;
        }

        String message = e.getMessage();
        if (message == null) {
            message = e.getClass().getName();
        }

        String lowerMessage = message.toLowerCase();
        return lowerMessage.contains("timeout") ||
                lowerMessage.contains("timeoutexception") ||
                e.getClass().getSimpleName().toLowerCase().contains("timeout") ||
                e.getCause() != null && isTimeoutError((Exception) e.getCause());
    }

    /**
     * Check if error message indicates table or column doesn't exist
     */
    private boolean isTableOrColumnNotFoundError(String errorMessage) {
        if (errorMessage == null) {
            return false;
        }

        String lowerError = errorMessage.toLowerCase();
        return lowerError.contains("table") && lowerError.contains("doesn't exist") ||
                lowerError.contains("table") && lowerError.contains("not found") ||
                lowerError.contains("unknown table") ||
                lowerError.contains("unknown column") ||
                lowerError.contains("column") && lowerError.contains("doesn't exist") ||
                lowerError.contains("no such table") ||
                lowerError.contains("no such column");
    }

    /**
     * Check if error message indicates a forbidden SQL keyword was used
     */
    private boolean isForbiddenKeywordError(String errorMessage) {
        if (errorMessage == null) {
            return false;
        }

        String lowerError = errorMessage.toLowerCase();
        return lowerError.contains("forbidden") ||
                lowerError.contains("only select queries are allowed") ||
                lowerError.contains("insert") && lowerError.contains("not allowed") ||
                lowerError.contains("update") && lowerError.contains("not allowed") ||
                lowerError.contains("delete") && lowerError.contains("not allowed") ||
                lowerError.contains("drop") && lowerError.contains("not allowed") ||
                lowerError.contains("alter") && lowerError.contains("not allowed") ||
                lowerError.contains("create") && lowerError.contains("not allowed");
    }

    /**
     * Build a friendly response for table/column not found errors
     */
    private String buildTableNotFoundResponse(String errorMessage, String question, String sqlQuery,
            DatabaseSchemaDTO schema) {
        StringBuilder response = new StringBuilder();

        // Extract table or column name from error
        String missingItem = extractMissingTableOrColumn(errorMessage);

        response.append("❌ **Sorry, I couldn't find that table or column in your database.**\n\n");

        if (missingItem != null) {
            response.append("**What I was looking for:** `").append(missingItem).append("`\n\n");
        }

        response.append("**Your question:** \"").append(question).append("\"\n\n");

        response.append("**Generated SQL:**\n```sql\n").append(sqlQuery).append("\n```\n\n");

        response.append("**Available tables in your database:**\n");
        if (schema != null && schema.getTables() != null && !schema.getTables().isEmpty()) {
            for (DatabaseSchemaDTO.TableInfo table : schema.getTables()) {
                // Skip system tables
                if (!table.getName().toLowerCase().startsWith("spring_") &&
                        !table.getName().toLowerCase().equals("flyway_schema_history")) {
                    response.append("• `").append(table.getName()).append("`\n");
                }
            }
        } else {
            response.append("• (No tables available)\n");
        }

        response.append("\n**Suggestions:**\n");
        response.append("• Check the spelling of table/column names\n");
        response.append("• Table names are case-sensitive in some databases\n");
        response.append("• Try asking about one of the available tables listed above\n");
        response.append("• Example: \"Show me all data from [table_name]\"\n");

        return response.toString();
    }

    /**
     * Extract table or column name from error message
     */
    private String extractMissingTableOrColumn(String errorMessage) {
        if (errorMessage == null) {
            return null;
        }

        // Try to extract table name: "Table 'database.tablename' doesn't exist"
        if (errorMessage.contains("Table '") && errorMessage.contains("' doesn't exist")) {
            int start = errorMessage.indexOf("Table '") + 7;
            int end = errorMessage.indexOf("' doesn't exist");
            if (start > 0 && end > start) {
                String fullName = errorMessage.substring(start, end);
                // Extract just the table name (after the dot if database.table format)
                if (fullName.contains(".")) {
                    return fullName.substring(fullName.lastIndexOf(".") + 1);
                }
                return fullName;
            }
        }

        // Try to extract column name: "Unknown column 'columnname'"
        if (errorMessage.contains("Unknown column '")) {
            int start = errorMessage.indexOf("Unknown column '") + 16;
            int end = errorMessage.indexOf("'", start);
            if (start > 0 && end > start) {
                return errorMessage.substring(start, end);
            }
        }

        return null;
    }

    /**
     * Build a friendly response for forbidden operations
     */
    private String buildForbiddenOperationResponse(String errorMessage, String databaseType) {
        StringBuilder response = new StringBuilder();

        response.append("⚠️ **I understand what you want to do, but I can't execute this operation.**\n\n");

        // Extract which keyword was detected
        String forbiddenKeyword = extractForbiddenKeyword(errorMessage);

        if (forbiddenKeyword != null) {
            response.append("**Reason:** The query contains a `").append(forbiddenKeyword)
                    .append("` operation, which is not allowed for safety reasons.\n\n");
        } else {
            response.append(
                    "**Reason:** This query contains operations that modify or delete data, which is not allowed for safety reasons.\n\n");
        }

        response.append("**Security Policy:**\n");
        response.append("• ✅ **Allowed:** SELECT queries (read data only)\n");
        response.append("• ❌ **Not Allowed:** INSERT, UPDATE, DELETE, DROP, ALTER, CREATE, TRUNCATE, etc.\n\n");

        response.append("**What I can help you with:**\n");
        response.append("• View and analyze your data\n");
        response.append("• Count records and calculate statistics\n");
        response.append("• Search and filter information\n");
        response.append("• Generate reports and summaries\n\n");

        response.append("**Alternative suggestions:**\n");
        response.append("• Ask me to \"show\" or \"find\" data instead\n");
        response.append("• Use your database management tool for data modifications\n");
        response.append("• Contact your database administrator for write access\n\n");

        response.append("I generated the SQL query above so you can see what would be executed, ");
        response.append("but for your database's safety, I cannot run it.");

        return response.toString();
    }

    /**
     * Extract the forbidden keyword from error message
     */
    private String extractForbiddenKeyword(String errorMessage) {
        if (errorMessage == null) {
            return null;
        }

        String[] forbiddenKeywords = { "INSERT", "UPDATE", "DELETE", "DROP", "ALTER",
                "CREATE", "TRUNCATE", "REPLACE", "MERGE" };

        String upperError = errorMessage.toUpperCase();
        for (String keyword : forbiddenKeywords) {
            if (upperError.contains(keyword)) {
                return keyword;
            }
        }

        return null;
    }

    /**
     * Generate SQL query with retries
     */
    private String generateQueryWithRetries(Long userId, String question, DatabaseSchemaDTO schema) {
        String lastError = null;

        for (int attempt = 0; attempt <= maxRetries; attempt++) {
            try {
                String query = aiService.generateSqlQuery(userId, question, schema, lastError);
                log.info("this is query come form ai :" + "  " + query);
                String cleaned = sqlValidatorService.cleanQuery(query);
                return cleaned;
            } catch (Exception e) {
                lastError = e.getMessage();
                log.warn("Query generation attempt {} failed: {}", attempt + 1, lastError);

                if (attempt == maxRetries) {
                    throw new ChatBotException(
                            "Failed to generate valid SQL after " + maxRetries + " attempts: " + lastError);
                }
            }
        }

        throw new ChatBotException("Failed to generate SQL query");
    }

    /**
     * Get or create conversation session
     */
    private String getOrCreateSession(Long userId, Long databaseConfigId) {
        List<ConversationSession> activeSessions = conversationSessionRepository
                .findByUserIdAndIsActiveTrueOrderByLastActivityAtDesc(userId);

        if (!activeSessions.isEmpty()) {
            ConversationSession session = activeSessions.get(0);
            if (session.getDatabaseConfigId().equals(databaseConfigId)) {
                return session.getSessionId();
            }
        }

        // Create new session
        String sessionId = UUID.randomUUID().toString();
        ConversationSession session = ConversationSession.builder()
                .sessionId(sessionId)
                .userId(userId)
                .databaseConfigId(databaseConfigId)
                .isActive(true)
                .build();

        conversationSessionRepository.save(session);
        return sessionId;
    }

    /**
     * Save conversation to database
     */
    private void saveConversation(Long userId, Long databaseConfigId, String sessionId,
            String question, String sqlQuery,
            List<Map<String, Object>> sqlResult,
            String answer, String errorMessage) {
        try {
            Conversation conversation = Conversation.builder()
                    .userId(userId)
                    .databaseConfigId(databaseConfigId)
                    .sessionId(sessionId)
                    .question(question)
                    .sqlQuery(sqlQuery)
                    .sqlResult(sqlResult)
                    .answer(answer)
                    .errorMessage(errorMessage)
                    .isGreeting(false)
                    .build();

            conversationRepository.save(conversation);
            log.debug("Conversation saved: {}", conversation.getId());
        } catch (Exception e) {
            log.error("Failed to save conversation", e);
            // Don't throw - saving conversation failure shouldn't break the flow
        }
    }

    /**
     * Get conversation history for a user
     */
    public List<Conversation> getConversationHistory(Long userId) {
        return conversationRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }

    /**
     * Get conversation history for a session
     */
    public List<Conversation> getSessionHistory(String sessionId) {
        return conversationRepository.findBySessionIdOrderByCreatedAtAsc(sessionId);
    }

    /**
     * Limit query results to maximum number of rows for display
     */
    private List<Map<String, Object>> limitResults(List<Map<String, Object>> results, int maxRows) {
        if (results == null || results.isEmpty()) {
            return results;
        }

        if (results.size() <= maxRows) {
            return results;
        }

        log.info("Limiting results from {} rows to {} rows for display", results.size(), maxRows);
        return results.subList(0, maxRows);
    }

    /**
     * Build friendly message when user exceeds daily query limit in DEMO mode
     */
    private String buildDailyLimitExceededMessage(Long userId) {
        int currentCount = demoQueryUsageService.getCurrentQueryCount(userId);
        int limit = demoQueryUsageService.getDailyLimit();

        StringBuilder message = new StringBuilder();
        message.append("🚫 **Daily Query Limit Reached**\n\n");
        message.append("You're currently using the **DEMO mode** with our platform's free AI service.\n\n");
        message.append("**Your usage today:** ").append(currentCount).append(" / ").append(limit)
                .append(" queries\n\n");
        message.append("**Why this limit exists:**\n");
        message.append("• DEMO mode uses our shared AI resources\n");
        message.append("• Helps us provide free service to all users\n");
        message.append("• Prevents abuse and ensures fair access\n\n");
        message.append("**Want unlimited queries? Upgrade to your own AI:**\n");
        message.append("1. Go to **Settings → AI Settings**\n");
        message.append("2. Choose one of these options:\n");
        message.append("   • **Claude AI** - Use your Anthropic API key (powerful and intelligent)\n");
        message.append("   • **OpenAI** - Use your OpenAI API key (GPT-4 or GPT-3.5)\n");
        message.append("3. Add your API key and enjoy unlimited queries!\n\n");
        message.append("**API key benefits:**\n");
        message.append("• ✅ No daily limits\n");
        message.append("• ✅ Faster response times\n");
        message.append("• ✅ Your choice of AI model\n");
        message.append("• ✅ You only pay for what you use\n\n");
        message.append("**Your limit resets:** Tomorrow at midnight\n");
        message.append("**Queries remaining today:** 0\n");
        return message.toString();
    }
}
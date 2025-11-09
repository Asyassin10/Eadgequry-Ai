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

    @Value("${chatbot.max-retries:2}")
    private int maxRetries;

    /**
     * Process a chat question (non-streaming)
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

                // Check if it's a forbidden keyword error
                if (isForbiddenKeywordError(errorMsg)) {
                    log.warn("Forbidden SQL operation attempted: {}", sqlQuery);
                    String friendlyError = buildForbiddenOperationResponse(errorMsg, schema.getDatabaseType());

                    // Save conversation with error
                    String sessionId = getOrCreateSession(userId, databaseConfigId);
                    saveConversation(userId, databaseConfigId, sessionId, question, sqlQuery, null, friendlyError, errorMsg);

                    // Return error response but include the SQL query so user can see what was attempted
                    return ChatResponse.builder()
                            .success(false)
                            .question(question)
                            .sqlQuery(sqlQuery)  // Show the generated query
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

            // Save conversation
            String sessionId = getOrCreateSession(userId, databaseConfigId);
            saveConversation(userId, databaseConfigId, sessionId, question, sqlQuery, limitedResult, answer, null);

            return ChatResponse.success(question, sqlQuery, limitedResult, answer);

        } catch (Exception e) {
            log.error("Error processing question", e);

            // If we have a SQL query and it's a forbidden keyword error, handle it gracefully
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
            response.append("**Reason:** This query contains operations that modify or delete data, which is not allowed for safety reasons.\n\n");
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

        String[] forbiddenKeywords = {"INSERT", "UPDATE", "DELETE", "DROP", "ALTER",
                                       "CREATE", "TRUNCATE", "REPLACE", "MERGE"};

        String upperError = errorMessage.toUpperCase();
        for (String keyword : forbiddenKeywords) {
            if (upperError.contains(keyword)) {
                return keyword;
            }
        }

        return null;
    }

    /**
     * Process a chat question with streaming response
     */
    @Transactional
    public Flux<String> askStreaming(ChatRequest request) {
        return Flux.defer(() -> {
            String sqlQuery = null;
            try {
                String question = request.getQuestion().trim();
                Long userId = request.getUserId();
                Long databaseConfigId = request.getDatabaseConfigId();

                // Get database schema
                DatabaseSchemaDTO schema = dataSourceClient.getSchemaByConfigId(databaseConfigId, userId);

                // Generate SQL query
                sqlQuery = generateQueryWithRetries(userId, question, schema);

                // Clean SQL
                String cleanedQuery = sqlValidatorService.cleanQuery(sqlQuery);
                final String finalSqlQuery = cleanedQuery;

                // Execute query (datasource will validate for security)
                QueryExecutionResponse queryResult = dataSourceClient.executeQuery(databaseConfigId, userId, cleanedQuery);

                if (!queryResult.isSuccess()) {
                    String errorMsg = queryResult.getError();

                    // Check if it's a forbidden keyword error
                    if (isForbiddenKeywordError(errorMsg)) {
                        log.warn("Forbidden SQL operation attempted in streaming: {}", finalSqlQuery);
                        String friendlyError = buildForbiddenOperationResponse(errorMsg, schema.getDatabaseType());

                        // Save conversation with error
                        String sessionId = getOrCreateSession(userId, databaseConfigId);
                        saveConversation(userId, databaseConfigId, sessionId, question, finalSqlQuery, null, friendlyError, errorMsg);

                        // Return error as flux
                        return Flux.just(friendlyError);
                    }

                    return Flux.error(new ChatBotException("Query execution failed: " + errorMsg));
                }

                // Limit results to 50 rows maximum (for display purposes)
                List<Map<String, Object>> limitedResult = limitResults(queryResult.getResult(), 50);

                // Generate streaming answer
                String sessionId = getOrCreateSession(userId, databaseConfigId);

                return aiService.generateStreamingAnswer(userId, question, cleanedQuery, limitedResult)
                        .doOnComplete(() -> {
                            // Save conversation after streaming completes
                            saveConversation(userId, databaseConfigId, sessionId, question, cleanedQuery,
                                    limitedResult, "[Streaming response]", null);
                        });

            } catch (Exception e) {
                log.error("Error processing streaming question", e);

                // If we have a SQL query and it's a forbidden keyword error, handle it gracefully
                if (sqlQuery != null && isForbiddenKeywordError(e.getMessage())) {
                    String friendlyError = buildForbiddenOperationResponse(e.getMessage(), "");
                    return Flux.just(friendlyError);
                }

                return Flux.error(new ChatBotException("Error: " + e.getMessage(), e));
            }
        });
    }

    /**
     * Generate SQL query with retries
     */
    private String generateQueryWithRetries(Long userId, String question, DatabaseSchemaDTO schema) {
        String lastError = null;

        for (int attempt = 0; attempt <= maxRetries; attempt++) {
            try {
                String query = aiService.generateSqlQuery(userId, question, schema, lastError);
                String cleaned = sqlValidatorService.cleanQuery(query);
                return cleaned;
            } catch (Exception e) {
                lastError = e.getMessage();
                log.warn("Query generation attempt {} failed: {}", attempt + 1, lastError);

                if (attempt == maxRetries) {
                    throw new ChatBotException("Failed to generate valid SQL after " + maxRetries + " attempts: " + lastError);
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
}

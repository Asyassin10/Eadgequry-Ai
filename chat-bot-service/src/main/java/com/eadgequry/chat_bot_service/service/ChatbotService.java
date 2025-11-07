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
        try {
            String question = request.getQuestion().trim();
            Long userId = request.getUserId();
            Long databaseConfigId = request.getDatabaseConfigId();

            // Get database schema
            DatabaseSchemaDTO schema = dataSourceClient.getSchemaByConfigId(databaseConfigId, userId);

            // Generate SQL query with retries
            String sqlQuery = generateQueryWithRetries(question, schema);

            // Clean SQL
            sqlQuery = sqlValidatorService.cleanQuery(sqlQuery);

            // Execute query (datasource will validate for security)
            QueryExecutionResponse queryResult = dataSourceClient.executeQuery(databaseConfigId, userId, sqlQuery);

            if (!queryResult.isSuccess()) {
                throw new ChatBotException("Query execution failed: " + queryResult.getError());
            }

            // Generate answer
            String answer = aiService.generateAnswer(question, sqlQuery, queryResult.getResult());

            // Save conversation
            String sessionId = getOrCreateSession(userId, databaseConfigId);
            saveConversation(userId, databaseConfigId, sessionId, question, sqlQuery, queryResult.getResult(), answer, null);

            return ChatResponse.success(question, sqlQuery, queryResult.getResult(), answer);

        } catch (Exception e) {
            log.error("Error processing question", e);
            return ChatResponse.error("Error: " + e.getMessage());
        }
    }

    /**
     * Process a chat question with streaming response
     */
    @Transactional
    public Flux<String> askStreaming(ChatRequest request) {
        return Flux.defer(() -> {
            try {
                String question = request.getQuestion().trim();
                Long userId = request.getUserId();
                Long databaseConfigId = request.getDatabaseConfigId();

                // Get database schema
                DatabaseSchemaDTO schema = dataSourceClient.getSchemaByConfigId(databaseConfigId, userId);

                // Generate SQL query
                String sqlQuery = generateQueryWithRetries(question, schema);

                // Clean SQL
                String cleanedQuery = sqlValidatorService.cleanQuery(sqlQuery);

                // Execute query (datasource will validate for security)
                QueryExecutionResponse queryResult = dataSourceClient.executeQuery(databaseConfigId, userId, cleanedQuery);

                if (!queryResult.isSuccess()) {
                    return Flux.error(new ChatBotException("Query execution failed: " + queryResult.getError()));
                }

                // Generate streaming answer
                String sessionId = getOrCreateSession(userId, databaseConfigId);
                final String finalQuery = cleanedQuery;

                return aiService.generateStreamingAnswer(question, finalQuery, queryResult.getResult())
                        .doOnComplete(() -> {
                            // Save conversation after streaming completes
                            saveConversation(userId, databaseConfigId, sessionId, question, finalQuery,
                                    queryResult.getResult(), "[Streaming response]", null);
                        });

            } catch (Exception e) {
                log.error("Error processing streaming question", e);
                return Flux.error(new ChatBotException("Error: " + e.getMessage(), e));
            }
        });
    }

    /**
     * Generate SQL query with retries
     */
    private String generateQueryWithRetries(String question, DatabaseSchemaDTO schema) {
        String lastError = null;

        for (int attempt = 0; attempt <= maxRetries; attempt++) {
            try {
                String query = aiService.generateSqlQuery(question, schema, lastError);
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
}

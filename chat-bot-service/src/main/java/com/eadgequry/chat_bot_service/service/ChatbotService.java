package com.eadgequry.chat_bot_service.service;

import com.eadgequry.chat_bot_service.client.DataSourceClient;
import com.eadgequry.chat_bot_service.dto.*;
import com.eadgequry.chat_bot_service.exception.ChatBotException;
import com.eadgequry.chat_bot_service.model.Conversation;
import com.eadgequry.chat_bot_service.model.ConversationSession;
import com.eadgequry.chat_bot_service.repository.ConversationRepository;
import com.eadgequry.chat_bot_service.repository.ConversationSessionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;

import java.util.List;
import java.util.UUID;
import java.util.regex.Pattern;

@Service
@Slf4j
@RequiredArgsConstructor
public class ChatbotService {

    private final AiService aiService;
    private final SqlValidatorService sqlValidatorService;
    private final DataSourceClient dataSourceClient;
    private final ConversationRepository conversationRepository;
    private final ConversationSessionRepository conversationSessionRepository;

    @Value("${chatbot.max-retries:2}")
    private int maxRetries;

    @Value("${chatbot.stream-enabled:true}")
    private boolean streamEnabled;

    /**
     * Main method to process a chat question (non-streaming)
     */
    @Transactional
    public ChatResponse ask(ChatRequest request) {
        try {
            String question = request.getQuestion().trim();
            Long userId = request.getUserId();
            Long databaseConfigId = request.getDatabaseConfigId();

            // Check if it's a greeting or simple chat
            if (isGreetingOrChat(question)) {
                String answer = getGreetingResponse(question);
                saveConversation(userId, databaseConfigId, null, question, null, null, answer, true);
                return ChatResponse.greetingResponse(question, answer);
            }

            // Check if completely out of context
            if (isCompletelyOutOfContext(question)) {
                String answer = "I specialize in database queries. I can help you find data, check attributes, or compare up to 2 records. How can I assist?";
                saveConversation(userId, databaseConfigId, null, question, null, null, answer, false);
                return ChatResponse.greetingResponse(question, answer);
            }

            // Check if trying to compare more than 2
            if (isComparingMoreThanTwo(question)) {
                String answer = "I can compare maximum 2 items at a time. Please select 2 items you want to compare.";
                saveConversation(userId, databaseConfigId, null, question, null, null, answer, false);
                return ChatResponse.greetingResponse(question, answer);
            }

            // Determine request type
            boolean isComparison = isComparisonRequest(question);
            boolean isDetailsRequest = isDetailsRequest(question);

            // Get database schema
            DatabaseSchemaDTO schema = dataSourceClient.getSchemaByConfigId(databaseConfigId, userId);

            // Generate SQL query with retries
            String sqlQuery = generateQueryWithRetries(question, schema, isComparison, isDetailsRequest);

            // Validate and clean SQL
            sqlQuery = sqlValidatorService.cleanQuery(sqlQuery);
            sqlValidatorService.validateQuery(sqlQuery);

            // Execute query
            QueryExecutionResponse queryResult = dataSourceClient.executeQuery(databaseConfigId, userId, sqlQuery);

            if (!queryResult.isSuccess()) {
                throw new ChatBotException("Query execution failed: " + queryResult.getError());
            }

            // Generate answer
            String answer = aiService.generateAnswer(question, sqlQuery, queryResult.getResult(), isComparison, isDetailsRequest);

            // Save conversation
            String sessionId = getOrCreateSession(userId, databaseConfigId);
            saveConversation(userId, databaseConfigId, sessionId, question, sqlQuery, queryResult.getResult(), answer, false);

            return ChatResponse.success(question, sqlQuery, queryResult.getResult(), answer);

        } catch (Exception e) {
            log.error("Error processing question", e);
            return ChatResponse.error("Error: " + e.getMessage());
        }
    }

    /**
     * Main method to process a chat question with streaming response
     */
    @Transactional
    public Flux<String> askStreaming(ChatRequest request) {
        return Flux.defer(() -> {
            try {
                String question = request.getQuestion().trim();
                Long userId = request.getUserId();
                Long databaseConfigId = request.getDatabaseConfigId();

                // Check if it's a greeting or simple chat
                if (isGreetingOrChat(question)) {
                    String answer = getGreetingResponse(question);
                    saveConversation(userId, databaseConfigId, null, question, null, null, answer, true);
                    return Flux.just(answer);
                }

                // Check if completely out of context
                if (isCompletelyOutOfContext(question)) {
                    String answer = "I specialize in database queries. I can help you find data, check attributes, or compare up to 2 records. How can I assist?";
                    saveConversation(userId, databaseConfigId, null, question, null, null, answer, false);
                    return Flux.just(answer);
                }

                // Check if trying to compare more than 2
                if (isComparingMoreThanTwo(question)) {
                    String answer = "I can compare maximum 2 items at a time. Please select 2 items you want to compare.";
                    saveConversation(userId, databaseConfigId, null, question, null, null, answer, false);
                    return Flux.just(answer);
                }

                // Determine request type
                boolean isComparison = isComparisonRequest(question);
                boolean isDetailsRequest = isDetailsRequest(question);

                // Get database schema
                DatabaseSchemaDTO schema = dataSourceClient.getSchemaByConfigId(databaseConfigId, userId);

                // Generate SQL query
                String sqlQuery = generateQueryWithRetries(question, schema, isComparison, isDetailsRequest);

                // Validate and clean SQL
                String cleanedQuery = sqlValidatorService.cleanQuery(sqlQuery);
                sqlValidatorService.validateQuery(cleanedQuery);

                // Execute query
                QueryExecutionResponse queryResult = dataSourceClient.executeQuery(databaseConfigId, userId, cleanedQuery);

                if (!queryResult.isSuccess()) {
                    return Flux.error(new ChatBotException("Query execution failed: " + queryResult.getError()));
                }

                // Generate streaming answer
                String sessionId = getOrCreateSession(userId, databaseConfigId);
                final String finalQuery = cleanedQuery;

                return aiService.generateStreamingAnswer(question, finalQuery, queryResult.getResult(), isComparison, isDetailsRequest)
                        .doOnComplete(() -> {
                            // Save conversation after streaming completes
                            // Note: In production, you'd want to accumulate the streamed answer
                            saveConversation(userId, databaseConfigId, sessionId, question, finalQuery,
                                    queryResult.getResult(), "[Streaming response]", false);
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
    private String generateQueryWithRetries(String question, DatabaseSchemaDTO schema,
                                           boolean isComparison, boolean isDetailsRequest) {
        String lastError = null;

        for (int attempt = 0; attempt <= maxRetries; attempt++) {
            try {
                String query = aiService.generateSqlQuery(question, schema, isComparison, isDetailsRequest, lastError);
                String cleaned = sqlValidatorService.cleanQuery(query);
                sqlValidatorService.validateQuery(cleaned);
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
     * Check if this is a greeting or simple chat
     */
    private boolean isGreetingOrChat(String question) {
        String lower = question.toLowerCase().trim();

        String[] greetings = {
                "bonjour", "bonsoir", "salut", "hello", "hi", "hey",
                "comment ça va", "comment vas-tu", "ça va",
                "qui es-tu", "qui es tu", "c'est quoi", "c est quoi",
                "tu es qui", "what are you", "who are you",
                "présente-toi", "presente toi", "introduce yourself",
                "merci", "thank you", "thanks", "au revoir", "bye"
        };

        for (String greeting : greetings) {
            if (lower.equals(greeting) || lower.startsWith(greeting + " ") || lower.endsWith(" " + greeting)) {
                return true;
            }
        }

        return false;
    }

    /**
     * Get appropriate greeting response
     */
    private String getGreetingResponse(String question) {
        String lower = question.toLowerCase().trim();

        if (lower.contains("bonjour") || lower.contains("bonsoir") || lower.contains("salut") ||
                lower.contains("hello") || lower.contains("hi")) {
            return "Hello! I'm the AI assistant for the database query system. I can help you find data, check attributes, or compare up to 2 records. How can I assist you today?";
        }

        if (lower.contains("qui es-tu") || lower.contains("qui es tu") || lower.contains("tu es qui") ||
                lower.contains("who are you") || lower.contains("présente") || lower.contains("introduce")) {
            return "I'm the AI assistant for the database query system. I can help you:\n\n" +
                    "- Find data by criteria\n" +
                    "- Check attributes and functionalities\n" +
                    "- Compare 2 records with complete details\n" +
                    "- Analyze database information\n\n" +
                    "What would you like to know?";
        }

        if (lower.contains("comment ça va") || lower.contains("comment vas") || lower.contains("ça va")) {
            return "I'm doing great, thanks! I'm ready to help you find data. What are you looking for?";
        }

        if (lower.contains("merci") || lower.contains("thank")) {
            return "You're welcome! Feel free to ask if you have more questions.";
        }

        if (lower.contains("au revoir") || lower.contains("bye")) {
            return "Goodbye! Come back anytime you need database assistance!";
        }

        return "Hello! How can I assist you with your database queries today?";
    }

    /**
     * Check if question is completely out of context
     */
    private boolean isCompletelyOutOfContext(String question) {
        String lower = question.toLowerCase();

        String[] outOfContextKeywords = {
                "météo", "weather", "température", "recette", "recipe", "cuisine",
                "sport", "football", "basket", "politique", "actualité", "news",
                "film", "movie", "musique", "music", "chanson", "blague", "joke",
                "restaurant", "voyage", "travel", "hotel", "voiture", "car"
        };

        for (String keyword : outOfContextKeywords) {
            if (lower.contains(keyword)) {
                return true;
            }
        }

        return false;
    }

    /**
     * Check if this is a comparison request
     */
    private boolean isComparisonRequest(String question) {
        String lower = question.toLowerCase();
        return lower.contains("compar") || lower.contains("versus") ||
                lower.contains("vs") || lower.contains("différence") ||
                lower.contains("diff");
    }

    /**
     * Check if this is a details request
     */
    private boolean isDetailsRequest(String question) {
        String lower = question.toLowerCase();

        String[] detailsKeywords = {
                "c'est quoi", "c est quoi", "qu'est-ce que", "qu est-ce que",
                "parle moi de", "dis moi sur", "information sur", "détails sur",
                "présente", "describe", "tell me about"
        };

        for (String keyword : detailsKeywords) {
            if (lower.contains(keyword)) {
                return true;
            }
        }

        // Also check for specific pattern like "logiciel X" or "solution X"
        if (Pattern.matches(".*\\b(logiciel|solution)\\s+[\\w\\-]+.*", lower)) {
            // But NOT if asking for specific fields
            String[] specificRequests = {"nom", "name", "catégorie", "category", "critère", "attribute"};
            for (String specific : specificRequests) {
                if (lower.contains(specific)) {
                    return false;
                }
            }
            return true;
        }

        return false;
    }

    /**
     * Check if trying to compare more than 2 items
     */
    private boolean isComparingMoreThanTwo(String question) {
        String lower = question.toLowerCase();

        if (!lower.contains("compar") && !lower.contains("versus") &&
                !lower.contains("vs") && !lower.contains("différence")) {
            return false;
        }

        // Count "et" or "and" or commas
        long separators = Pattern.compile("\\bet\\b|\\band\\b|,").matcher(lower).results().count();

        if (separators > 1) {
            return true;
        }

        // Check for explicit numbers like "3 logiciels", "4 solutions"
        return Pattern.matches(".*\\b([3-9]|[1-9]\\d+)\\s*(logiciels?|solutions?).*", lower);
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
                                  String answer, boolean isGreeting) {
        try {
            Conversation conversation = Conversation.builder()
                    .userId(userId)
                    .databaseConfigId(databaseConfigId)
                    .sessionId(sessionId)
                    .question(question)
                    .sqlQuery(sqlQuery)
                    .sqlResult(sqlResult)
                    .answer(answer)
                    .isGreeting(isGreeting)
                    .build();

            conversationRepository.save(conversation);
            log.debug("Conversation saved: {}", conversation.getId());
        } catch (Exception e) {
            log.error("Failed to save conversation", e);
            // Don't throw exception - saving conversation failure shouldn't break the flow
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

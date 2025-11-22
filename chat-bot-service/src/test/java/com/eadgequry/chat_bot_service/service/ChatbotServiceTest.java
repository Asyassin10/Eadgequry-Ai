package com.eadgequry.chat_bot_service.service;

import com.eadgequry.chat_bot_service.client.DataSourceClient;
import com.eadgequry.chat_bot_service.dto.*;
import com.eadgequry.chat_bot_service.model.Conversation;
import com.eadgequry.chat_bot_service.model.ConversationSession;
import com.eadgequry.chat_bot_service.repository.ConversationRepository;
import com.eadgequry.chat_bot_service.repository.ConversationSessionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ChatbotServiceTest {

    @Mock
    private AiService aiService;

    @Mock
    private SqlValidatorService sqlValidatorService;

    @Mock
    private DataSourceClient dataSourceClient;

    @Mock
    private ConversationRepository conversationRepository;

    @Mock
    private ConversationSessionRepository conversationSessionRepository;

    @Mock
    private UserAiSettingsService userAiSettingsService;

    @Mock
    private DemoQueryUsageService demoQueryUsageService;

    @InjectMocks
    private ChatbotService chatbotService;

    private ChatRequest chatRequest;
    private DatabaseSchemaDTO schemaDTO;
    private QueryExecutionResponse queryResult;

    @BeforeEach
    void setUp() {
        chatRequest = new ChatRequest();
        chatRequest.setUserId(1L);
        chatRequest.setDatabaseConfigId(100L);
        chatRequest.setQuestion("SELECT * FROM users");

        schemaDTO = new DatabaseSchemaDTO();
        schemaDTO.setId(1L);
        schemaDTO.setDatabaseConfigId(100L);
        schemaDTO.setSchemaJson("{\"tables\": []}");

        queryResult = QueryExecutionResponse.builder()
                .success(true)
                .result(List.of(Map.of("id", 1, "name", "Test")))
                .build();
    }

    @Test
    void ask_WhenGreetingQuestion_ShouldReturnGreetingResponse() {
        // Arrange
        chatRequest.setQuestion("Hello");
        when(aiService.handleNonDatabaseQuestion("Hello"))
                .thenReturn("Hi! How can I help you?");

        // Act
        ChatResponse response = chatbotService.ask(chatRequest);

        // Assert
        assertTrue(response.isSuccess());
        assertEquals("Hi! How can I help you?", response.getAnswer());
        assertNull(response.getSqlQuery());
        verify(aiService).handleNonDatabaseQuestion("Hello");
    }

    @Test
    void ask_WhenValidDatabaseQuestion_ShouldExecuteAndReturnResult() {
        // Arrange
        when(aiService.handleNonDatabaseQuestion(anyString())).thenReturn(null);
        when(userAiSettingsService.isUsingDemoMode(1L)).thenReturn(false);
        when(dataSourceClient.getSchemaByConfigId(100L, 1L)).thenReturn(schemaDTO);
        when(aiService.generateSqlQuery(eq(1L), anyString(), any(DatabaseSchemaDTO.class), isNull()))
                .thenReturn("SELECT * FROM users");
        when(sqlValidatorService.cleanQuery(anyString())).thenReturn("SELECT * FROM users");
        when(dataSourceClient.executeQuery(eq(100L), eq(1L), anyString())).thenReturn(queryResult);
        when(aiService.generateAnswer(eq(1L), anyString(), anyString(), anyList()))
                .thenReturn("Found 1 user");

        List<ConversationSession> sessions = List.of(
                ConversationSession.builder()
                        .sessionId(UUID.randomUUID().toString())
                        .userId(1L)
                        .databaseConfigId(100L)
                        .isActive(true)
                        .build()
        );
        when(conversationSessionRepository.findByUserIdAndIsActiveTrueOrderByLastActivityAtDesc(1L))
                .thenReturn(sessions);

        // Act
        ChatResponse response = chatbotService.ask(chatRequest);

        // Assert
        assertTrue(response.isSuccess());
        assertNotNull(response.getAnswer());
        assertNotNull(response.getSqlQuery());
        verify(dataSourceClient).executeQuery(eq(100L), eq(1L), anyString());
    }

    @Test
    void ask_WhenDemoModeExceeded_ShouldReturnLimitMessage() {
        // Arrange
        when(aiService.handleNonDatabaseQuestion(anyString())).thenReturn(null);
        when(userAiSettingsService.isUsingDemoMode(1L)).thenReturn(true);
        when(demoQueryUsageService.hasExceededDailyLimit(1L)).thenReturn(true);

        // Act
        ChatResponse response = chatbotService.ask(chatRequest);

        // Assert
        assertFalse(response.isSuccess());
        assertTrue(response.getAnswer().contains("exceeded"));
        assertEquals("Daily query limit exceeded", response.getError());
    }
}

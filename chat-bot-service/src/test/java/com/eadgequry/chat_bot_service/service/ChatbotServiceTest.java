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

        queryResult = new QueryExecutionResponse();
        queryResult.setSuccess(true);
        queryResult.setData(List.of(Map.of("id", 1, "name", "Test")));
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
        when(aiService.generateQuery(anyLong(), anyString(), any())).thenReturn("SELECT * FROM users");
        when(sqlValidatorService.cleanQuery(anyString())).thenReturn("SELECT * FROM users");
        when(dataSourceClient.executeQuery(100L, 1L, "SELECT * FROM users")).thenReturn(queryResult);
        when(aiService.generateAnswer(anyString(), anyString(), anyList(), any())).thenReturn("Found 1 user");

        ConversationSession session = new ConversationSession();
        session.setSessionId(UUID.randomUUID().toString());
        when(conversationSessionRepository.findByUserIdAndDatabaseConfigIdAndActiveTrue(1L, 100L))
                .thenReturn(Optional.of(session));

        // Act
        ChatResponse response = chatbotService.ask(chatRequest);

        // Assert
        assertTrue(response.isSuccess());
        assertNotNull(response.getAnswer());
        assertNotNull(response.getSqlQuery());
        verify(dataSourceClient).executeQuery(100L, 1L, "SELECT * FROM users");
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

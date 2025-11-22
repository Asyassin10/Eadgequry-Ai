package com.eadgequry.chat_bot_service.service;

import com.eadgequry.chat_bot_service.dto.UpdateAiSettingsRequest;
import com.eadgequry.chat_bot_service.dto.UserAiSettingsDTO;
import com.eadgequry.chat_bot_service.model.UserAiSettings;
import com.eadgequry.chat_bot_service.repository.UserAiSettingsRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserAiSettingsServiceTest {

    @Mock
    private UserAiSettingsRepository userAiSettingsRepository;

    @Mock
    private EncryptionService encryptionService;

    @InjectMocks
    private UserAiSettingsService userAiSettingsService;

    private UserAiSettings settings;

    @BeforeEach
    void setUp() {
        settings = UserAiSettings.builder()
                .id(1L)
                .userId(1L)
                .apiKeyEncrypted("encrypted_key")
                .provider(UserAiSettings.AiProvider.OPENAI)
                .model("gpt-4")
                .build();
    }

    @Test
    void getUserSettings_WhenExists_ShouldReturnSettings() {
        // Arrange
        when(userAiSettingsRepository.findByUserId(1L))
                .thenReturn(Optional.of(settings));

        // Act
        UserAiSettingsDTO result = userAiSettingsService.getUserSettings(1L);

        // Assert
        assertNotNull(result);
        assertEquals(1L, result.getUserId());
        assertEquals("OPENAI", result.getProvider());
        assertEquals("gpt-4", result.getModel());
        verify(userAiSettingsRepository).findByUserId(1L);
    }

    @Test
    void isUsingDemoMode_WhenUserNotExists_ShouldReturnTrue() {
        // Arrange
        when(userAiSettingsRepository.findByUserId(1L))
                .thenReturn(Optional.empty());

        // Act
        boolean result = userAiSettingsService.isUsingDemoMode(1L);

        // Assert
        assertTrue(result);
    }

    @Test
    void isUsingDemoMode_WhenUserExistsWithDemoMode_ShouldReturnTrue() {
        // Arrange
        settings = UserAiSettings.builder()
                .id(1L)
                .userId(1L)
                .provider(UserAiSettings.AiProvider.DEMO)
                .build();

        when(userAiSettingsRepository.findByUserId(1L))
                .thenReturn(Optional.of(settings));

        // Act
        boolean result = userAiSettingsService.isUsingDemoMode(1L);

        // Assert
        assertTrue(result);
    }

    @Test
    void isUsingDemoMode_WhenUserExistsWithoutDemoMode_ShouldReturnFalse() {
        // Arrange
        when(userAiSettingsRepository.findByUserId(1L))
                .thenReturn(Optional.of(settings));

        // Act
        boolean result = userAiSettingsService.isUsingDemoMode(1L);

        // Assert
        assertFalse(result);
    }

    @Test
    void updateUserSettings_ShouldSaveAndReturnDTO() {
        // Arrange
        UpdateAiSettingsRequest request = new UpdateAiSettingsRequest();
        request.setProvider("CLAUDE");
        request.setModel("claude-3");
        request.setApiKey("test-key");

        when(userAiSettingsRepository.findByUserId(1L))
                .thenReturn(Optional.of(settings));
        when(encryptionService.encrypt(anyString())).thenReturn("encrypted");
        when(userAiSettingsRepository.save(any(UserAiSettings.class)))
                .thenReturn(settings);

        // Act
        UserAiSettingsDTO result = userAiSettingsService.updateUserSettings(1L, request);

        // Assert
        assertNotNull(result);
        verify(userAiSettingsRepository).save(any(UserAiSettings.class));
    }
}

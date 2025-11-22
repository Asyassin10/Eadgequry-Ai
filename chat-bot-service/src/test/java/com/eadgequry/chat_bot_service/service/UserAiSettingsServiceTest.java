package com.eadgequry.chat_bot_service.service;

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

    @InjectMocks
    private UserAiSettingsService userAiSettingsService;

    private UserAiSettings settings;

    @BeforeEach
    void setUp() {
        settings = new UserAiSettings();
        settings.setId(1L);
        settings.setUserId(1L);
        settings.setApiKeyEncrypted("encrypted_key");
        settings.setApiProvider("OPENAI");
        settings.setAiModel("gpt-4");
        settings.setUsingDemoMode(false);
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
        assertEquals("OPENAI", result.getApiProvider());
        assertEquals("gpt-4", result.getAiModel());
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
        settings.setUsingDemoMode(true);
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
        settings.setUsingDemoMode(false);
        when(userAiSettingsRepository.findByUserId(1L))
                .thenReturn(Optional.of(settings));

        // Act
        boolean result = userAiSettingsService.isUsingDemoMode(1L);

        // Assert
        assertFalse(result);
    }

    @Test
    void updateSettings_ShouldSaveAndReturnDTO() {
        // Arrange
        UserAiSettingsDTO dto = new UserAiSettingsDTO();
        dto.setUserId(1L);
        dto.setApiProvider("ANTHROPIC");
        dto.setAiModel("claude-3");

        when(userAiSettingsRepository.findByUserId(1L))
                .thenReturn(Optional.of(settings));
        when(userAiSettingsRepository.save(any(UserAiSettings.class)))
                .thenReturn(settings);

        // Act
        UserAiSettingsDTO result = userAiSettingsService.updateSettings(dto);

        // Assert
        assertNotNull(result);
        verify(userAiSettingsRepository).save(any(UserAiSettings.class));
    }
}

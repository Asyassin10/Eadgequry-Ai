package com.eadgequry.chat_bot_service.service;

import com.eadgequry.chat_bot_service.dto.UpdateAiSettingsRequest;
import com.eadgequry.chat_bot_service.dto.UserAiSettingsDTO;
import com.eadgequry.chat_bot_service.model.UserAiSettings;
import com.eadgequry.chat_bot_service.repository.UserAiSettingsRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserAiSettingsService {

    private static final Logger log = LoggerFactory.getLogger(UserAiSettingsService.class);

    private final UserAiSettingsRepository repository;
    private final EncryptionService encryptionService;

    /**
     * Get user's AI settings (or create default DEMO settings)
     */
    public UserAiSettingsDTO getUserSettings(Long userId) {
        UserAiSettings settings = repository.findByUserId(userId)
                .orElseGet(() -> createDefaultSettings(userId));

        return UserAiSettingsDTO.fromEntity(settings);
    }

    /**
     * Update user's AI settings
     */
    @Transactional
    public UserAiSettingsDTO updateUserSettings(Long userId, UpdateAiSettingsRequest request) {
        UserAiSettings settings = repository.findByUserId(userId)
                .orElseGet(() -> createDefaultSettings(userId));

        // Update provider
        UserAiSettings.AiProvider provider = UserAiSettings.AiProvider.valueOf(request.getProvider().toUpperCase());
        settings.setProvider(provider);

        // Update model
        settings.setModel(request.getModel());

        // Update API key (encrypt if provided)
        if (request.getApiKey() != null && !request.getApiKey().trim().isEmpty()) {
            String encryptedKey = encryptionService.encrypt(request.getApiKey());
            settings.setApiKeyEncrypted(encryptedKey);
        } else if (provider == UserAiSettings.AiProvider.DEMO) {
            // DEMO mode doesn't need API key
            settings.setApiKeyEncrypted(null);
        }

        settings = repository.save(settings);
        log.info("Updated AI settings for user {}: provider={}, model={}", userId, provider, request.getModel());

        return UserAiSettingsDTO.fromEntity(settings);
    }

    /**
     * Get decrypted API key for internal use
     */
    public String getDecryptedApiKey(Long userId) {
        return repository.findByUserId(userId)
                .map(settings -> {
                    if (settings.getApiKeyEncrypted() != null && !settings.getApiKeyEncrypted().isEmpty()) {
                        return encryptionService.decrypt(settings.getApiKeyEncrypted());
                    }
                    return null;
                })
                .orElse(null);
    }

    /**
     * Get user's AI settings entity (for internal use)
     */
    public UserAiSettings getUserSettingsEntity(Long userId) {
        return repository.findByUserId(userId)
                .orElseGet(() -> createDefaultSettings(userId));
    }

    /**
     * Create default DEMO settings for new user
     */
    private UserAiSettings createDefaultSettings(Long userId) {
        UserAiSettings settings = UserAiSettings.builder()
                .userId(userId)
                .provider(UserAiSettings.AiProvider.DEMO)
                .model("anthropic/claude-3.5-sonnet")  // Default OpenRouter model
                .apiKeyEncrypted(null)
                .build();

        return repository.save(settings);
    }

    /**
     * Delete user's AI settings
     */
    @Transactional
    public void deleteUserSettings(Long userId) {
        repository.findByUserId(userId).ifPresent(repository::delete);
        log.info("Deleted AI settings for user {}", userId);
    }
}

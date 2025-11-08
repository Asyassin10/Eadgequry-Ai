package com.eadgequry.chat_bot_service.dto;

import com.eadgequry.chat_bot_service.model.UserAiSettings;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserAiSettingsDTO {

    private Long id;
    private Long userId;
    private String provider;  // DEMO, CLAUDE, OPENAI
    private String model;
    private String apiKey;    // Not encrypted when sending to frontend (masked)
    private boolean hasApiKey;  // Whether user has set an API key

    public static UserAiSettingsDTO fromEntity(UserAiSettings entity) {
        if (entity == null) {
            return null;
        }

        return UserAiSettingsDTO.builder()
                .id(entity.getId())
                .userId(entity.getUserId())
                .provider(entity.getProvider().name())
                .model(entity.getModel())
                .apiKey(null)  // Never send actual API key to frontend
                .hasApiKey(entity.getApiKeyEncrypted() != null && !entity.getApiKeyEncrypted().isEmpty())
                .build();
    }
}

package com.eadgequry.chat_bot_service.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateAiSettingsRequest {

    private String provider;  // DEMO, CLAUDE, OPENAI
    private String model;
    private String apiKey;    // Only required for CLAUDE and OPENAI providers
}

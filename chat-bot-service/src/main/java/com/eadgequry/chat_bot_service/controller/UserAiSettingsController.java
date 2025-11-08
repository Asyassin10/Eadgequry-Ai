package com.eadgequry.chat_bot_service.controller;

import com.eadgequry.chat_bot_service.dto.UpdateAiSettingsRequest;
import com.eadgequry.chat_bot_service.dto.UserAiSettingsDTO;
import com.eadgequry.chat_bot_service.service.UserAiSettingsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/ai-settings")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class UserAiSettingsController {

    private final UserAiSettingsService service;

    /**
     * Get user's AI settings
     */
    @GetMapping("/user/{userId}")
    public ResponseEntity<UserAiSettingsDTO> getUserSettings(@PathVariable Long userId) {
        UserAiSettingsDTO settings = service.getUserSettings(userId);
        return ResponseEntity.ok(settings);
    }

    /**
     * Update user's AI settings
     */
    @PutMapping("/user/{userId}")
    public ResponseEntity<UserAiSettingsDTO> updateUserSettings(
            @PathVariable Long userId,
            @RequestBody UpdateAiSettingsRequest request) {
        UserAiSettingsDTO settings = service.updateUserSettings(userId, request);
        return ResponseEntity.ok(settings);
    }

    /**
     * Delete user's AI settings (reset to default)
     */
    @DeleteMapping("/user/{userId}")
    public ResponseEntity<Void> deleteUserSettings(@PathVariable Long userId) {
        service.deleteUserSettings(userId);
        return ResponseEntity.noContent().build();
    }

    /**
     * Get available AI providers and models
     */
    @GetMapping("/providers")
    public ResponseEntity<ProvidersResponse> getAvailableProviders() {
        ProvidersResponse response = new ProvidersResponse();

        // DEMO provider
        response.addProvider("DEMO", "Platform Demo (Free)", new String[]{
                "anthropic/claude-3.5-sonnet",
                "anthropic/claude-3-opus",
                "openai/gpt-4-turbo"
        });

        // Claude provider
        response.addProvider("CLAUDE", "Anthropic Claude (Your API Key)", new String[]{
                "claude-3-5-sonnet-20241022",
                "claude-3-opus-20240229",
                "claude-3-sonnet-20240229"
        });

        // OpenAI provider
        response.addProvider("OPENAI", "OpenAI (Your API Key)", new String[]{
                "gpt-4-turbo",
                "gpt-4",
                "gpt-3.5-turbo"
        });

        return ResponseEntity.ok(response);
    }

    // Helper class for providers response
    public static class ProvidersResponse {
        public java.util.List<Provider> providers = new java.util.ArrayList<>();

        public void addProvider(String code, String name, String[] models) {
            Provider provider = new Provider();
            provider.code = code;
            provider.name = name;
            provider.models = models;
            providers.add(provider);
        }

        public static class Provider {
            public String code;
            public String name;
            public String[] models;
        }
    }
}

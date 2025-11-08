package com.eadgequry.chat_bot_service.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "user_ai_settings")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserAiSettings {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private Long userId;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private AiProvider provider; // DEMO, CLAUDE, OPENAI

    @Column(length = 100)
    private String model;

    @Column(length = 500)
    private String apiKeyEncrypted; // Encrypted API key for Claude/OpenAI

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    public enum AiProvider {
        DEMO,      // Uses platform's OpenRouter key
        CLAUDE,    // User's Anthropic Claude API key
        OPENAI     // User's OpenAI API key
    }
}

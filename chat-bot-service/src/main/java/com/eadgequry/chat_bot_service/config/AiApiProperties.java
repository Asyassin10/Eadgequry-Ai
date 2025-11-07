package com.eadgequry.chat_bot_service.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "ai.api")
@Data
public class AiApiProperties {
    private String url;
    private String key;
    private String model;
    private Double temperatureQuery;
    private Double temperatureAnswer;
    private Integer maxTokens;
    private Integer timeout;
}

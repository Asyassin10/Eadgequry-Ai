package com.eadgequry.api_gatway.config;

import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.cloud.gateway.route.RouteDefinition;
import org.springframework.cloud.gateway.route.RouteDefinitionLocator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.List;

/**
 * OpenAPI configuration for API Gateway
 * Aggregates OpenAPI documentation from all microservices
 */
@Configuration
public class OpenApiConfig {

    /**
     * Grouped OpenAPI for Auth Service
     */
    @Bean
    public GroupedOpenApi authServiceApi() {
        return GroupedOpenApi.builder()
                .group("auth-service")
                .pathsToMatch("/auth/**")
                .build();
    }

    /**
     * Grouped OpenAPI for Profile Service
     */
    @Bean
    public GroupedOpenApi profileServiceApi() {
        return GroupedOpenApi.builder()
                .group("profile-service")
                .pathsToMatch("/profiles/**")
                .build();
    }
}

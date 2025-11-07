package com.eadgequry.api_gatway.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;

@Configuration
@EnableWebFluxSecurity
public class SecurityConfig {

    @Bean
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
        http
            .csrf(csrf -> csrf.disable())
            .cors(cors -> cors.disable())  // Disable Spring Security CORS, use CorsWebFilter instead
            .authorizeExchange(exchange -> exchange
                // Allow OPTIONS requests (CORS preflight) without authentication
                .pathMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                // Public OpenAPI/Swagger endpoints
                .pathMatchers(
                    "/swagger-ui.html",
                    "/swagger-ui/**",
                    "/v3/api-docs/**",
                    "/webjars/**",
                    "/auth/v3/api-docs/**",
                    "/auth/swagger-ui/**",
                    "/profiles/v3/api-docs/**",
                    "/profiles/swagger-ui/**",
                    "/datasource/v3/api-docs/**",
                    "/datasource/swagger-ui/**",
                    "/chatbot/v3/api-docs/**",
                    "/chatbot/swagger-ui/**"
                ).permitAll()
                // Public Auth endpoints
                .pathMatchers(
                    "/auth/login",
                    "/auth/register",
                    "/auth/forgot-password",
                    "/auth/verify-email",
                    "/auth/reset-password",
                    "/auth/health",
                    "/auth/test",
                    "/auth/test-password-encoder",
                    "/auth/.well-known/jwks.json",
                    "/auth/actuator/**"
                ).permitAll()
                // Public Profile endpoints (health check)
                .pathMatchers(
                    "/profiles/health"
                ).permitAll()
                // Public Chatbot endpoints (health check)
                .pathMatchers(
                    "/chatbot/health"
                ).permitAll()
                // Protected Profile endpoints (require JWT)
                .pathMatchers(
                    "/profiles/**"
                ).authenticated()
                // Protected Chatbot endpoints (require JWT)
                .pathMatchers(
                    "/chatbot/**"
                ).authenticated()
                // Protected Datasource endpoints (require JWT)
                .pathMatchers(
                    "/datasource/**"
                ).authenticated()
                // Protected Auth endpoints (require JWT)
                .pathMatchers(
                    "/auth/users/**"
                ).authenticated()
                // All other requests require authentication
                .anyExchange().authenticated()
            )
            .oauth2ResourceServer(oauth2 -> oauth2
                .jwt(jwt -> {})
            );

        return http.build();
    }
}

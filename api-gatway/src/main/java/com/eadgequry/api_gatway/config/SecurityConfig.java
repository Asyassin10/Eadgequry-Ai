package com.eadgequry.api_gatway.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
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
            .authorizeExchange(exchange -> exchange
                // Public OpenAPI/Swagger endpoints
                .pathMatchers(
                    "/swagger-ui.html",
                    "/swagger-ui/**",
                    "/v3/api-docs/**",
                    "/webjars/**",
                    "/auth/v3/api-docs/**",
                    "/auth/swagger-ui/**",
                    "/profiles/v3/api-docs/**",
                    "/profiles/swagger-ui/**"
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
                // Protected Profile endpoints (require JWT)
                .pathMatchers(
                    "/profiles/**"
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

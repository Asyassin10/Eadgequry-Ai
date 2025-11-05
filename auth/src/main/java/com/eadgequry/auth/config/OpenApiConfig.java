package com.eadgequry.auth.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * OpenAPI configuration for Auth Service
 * Configures Swagger UI and API documentation
 */
@Configuration
public class OpenApiConfig {

    @Value("${server.port:8081}")
    private String serverPort;

    @Bean
    public OpenAPI authServiceOpenAPI() {
        // Define JWT Bearer security scheme
        SecurityScheme securityScheme = new SecurityScheme()
                .type(SecurityScheme.Type.HTTP)
                .scheme("bearer")
                .bearerFormat("JWT")
                .in(SecurityScheme.In.HEADER)
                .name("Authorization");

        // Define security requirement
        SecurityRequirement securityRequirement = new SecurityRequirement()
                .addList("bearerAuth");

        return new OpenAPI()
                .info(new Info()
                        .title("Auth Service API")
                        .description("Authentication and user management service API. Handles user registration, login, email verification, and password management.")
                        .version("v1.0")
                        .contact(new Contact()
                                .name("Eadgequry Team")
                                .email("support@eadgequry.com"))
                        .license(new License()
                                .name("Apache 2.0")
                                .url("https://www.apache.org/licenses/LICENSE-2.0.html")))
                .servers(List.of(
                        new Server()
                                .url("http://localhost:8765/auth")
                                .description("API Gateway"),
                        new Server()
                                .url("http://localhost:" + serverPort)
                                .description("Auth Service Direct")
                ))
                .addSecurityItem(securityRequirement)
                .schemaRequirement("bearerAuth", securityScheme);
    }
}

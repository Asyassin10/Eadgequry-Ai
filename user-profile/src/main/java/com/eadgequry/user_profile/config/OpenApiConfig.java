package com.eadgequry.user_profile.config;

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
 * OpenAPI configuration for Profile Service
 * Configures Swagger UI and API documentation
 */
@Configuration
public class OpenApiConfig {

    @Value("${server.port:8082}")
    private String serverPort;

    @Bean
    public OpenAPI profileServiceOpenAPI() {
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
                        .title("Profile Service API")
                        .description("User profile management service API. Handles user profile CRUD operations including name, avatar, bio, and preferences.")
                        .version("v1.0")
                        .contact(new Contact()
                                .name("Eadgequry Team")
                                .email("support@eadgequry.com"))
                        .license(new License()
                                .name("Apache 2.0")
                                .url("https://www.apache.org/licenses/LICENSE-2.0.html")))
                .servers(List.of(
                        new Server()
                                .url("http://localhost:8765/profiles")
                                .description("API Gateway"),
                        new Server()
                                .url("http://localhost:" + serverPort + "/profiles")
                                .description("Profile Service Direct")
                ))
                .addSecurityItem(securityRequirement)
                .schemaRequirement("bearerAuth", securityScheme);
    }
}

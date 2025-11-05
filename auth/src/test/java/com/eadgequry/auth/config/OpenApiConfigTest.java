package com.eadgequry.auth.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.servers.Server;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for OpenApiConfig
 */
class OpenApiConfigTest {

    private OpenApiConfig openApiConfig;

    @BeforeEach
    void setUp() {
        openApiConfig = new OpenApiConfig();
        ReflectionTestUtils.setField(openApiConfig, "serverPort", "8081");
    }

    @Test
    void authServiceOpenAPI_ShouldConfigureOpenAPIBean() {
        // When
        OpenAPI openAPI = openApiConfig.authServiceOpenAPI();

        // Then
        assertThat(openAPI).isNotNull();
    }

    @Test
    void authServiceOpenAPI_ShouldConfigureInfoDetails() {
        // When
        OpenAPI openAPI = openApiConfig.authServiceOpenAPI();
        Info info = openAPI.getInfo();

        // Then
        assertThat(info).isNotNull();
        assertThat(info.getTitle()).isEqualTo("Auth Service API");
        assertThat(info.getDescription()).contains("Authentication and user management service");
        assertThat(info.getVersion()).isEqualTo("v1.0");
    }

    @Test
    void authServiceOpenAPI_ShouldConfigureContactInfo() {
        // When
        OpenAPI openAPI = openApiConfig.authServiceOpenAPI();

        // Then
        assertThat(openAPI.getInfo().getContact()).isNotNull();
        assertThat(openAPI.getInfo().getContact().getName()).isEqualTo("Eadgequry Team");
        assertThat(openAPI.getInfo().getContact().getEmail()).isEqualTo("support@eadgequry.com");
    }

    @Test
    void authServiceOpenAPI_ShouldConfigureLicense() {
        // When
        OpenAPI openAPI = openApiConfig.authServiceOpenAPI();

        // Then
        assertThat(openAPI.getInfo().getLicense()).isNotNull();
        assertThat(openAPI.getInfo().getLicense().getName()).isEqualTo("Apache 2.0");
        assertThat(openAPI.getInfo().getLicense().getUrl())
                .isEqualTo("https://www.apache.org/licenses/LICENSE-2.0.html");
    }

    @Test
    void authServiceOpenAPI_ShouldConfigureServers() {
        // When
        OpenAPI openAPI = openApiConfig.authServiceOpenAPI();

        // Then
        assertThat(openAPI.getServers()).isNotNull();
        assertThat(openAPI.getServers()).hasSize(2);

        Server gatewayServer = openAPI.getServers().get(0);
        assertThat(gatewayServer.getUrl()).isEqualTo("http://localhost:8765/auth");
        assertThat(gatewayServer.getDescription()).isEqualTo("API Gateway");

        Server directServer = openAPI.getServers().get(1);
        assertThat(directServer.getUrl()).isEqualTo("http://localhost:8081");
        assertThat(directServer.getDescription()).isEqualTo("Auth Service Direct");
    }

    @Test
    void authServiceOpenAPI_ShouldConfigureSecurityScheme() {
        // When
        OpenAPI openAPI = openApiConfig.authServiceOpenAPI();

        // Then
        assertThat(openAPI.getComponents()).isNotNull();
        assertThat(openAPI.getComponents().getSecuritySchemes()).isNotNull();
        assertThat(openAPI.getComponents().getSecuritySchemes()).containsKey("bearerAuth");

        var securityScheme = openAPI.getComponents().getSecuritySchemes().get("bearerAuth");
        assertThat(securityScheme.getType()).isEqualTo(io.swagger.v3.oas.models.security.SecurityScheme.Type.HTTP);
        assertThat(securityScheme.getScheme()).isEqualTo("bearer");
        assertThat(securityScheme.getBearerFormat()).isEqualTo("JWT");
    }

    @Test
    void authServiceOpenAPI_ShouldConfigureSecurityRequirement() {
        // When
        OpenAPI openAPI = openApiConfig.authServiceOpenAPI();

        // Then
        assertThat(openAPI.getSecurity()).isNotNull();
        assertThat(openAPI.getSecurity()).hasSize(1);

        SecurityRequirement securityRequirement = openAPI.getSecurity().get(0);
        assertThat(securityRequirement.containsKey("bearerAuth")).isTrue();
    }

    @Test
    void authServiceOpenAPI_ShouldUseConfiguredPort() {
        // Given
        ReflectionTestUtils.setField(openApiConfig, "serverPort", "9999");

        // When
        OpenAPI openAPI = openApiConfig.authServiceOpenAPI();

        // Then
        Server directServer = openAPI.getServers().get(1);
        assertThat(directServer.getUrl()).isEqualTo("http://localhost:9999");
    }
}

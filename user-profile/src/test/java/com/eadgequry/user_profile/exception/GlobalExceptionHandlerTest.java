package com.eadgequry.user_profile.exception;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.context.request.WebRequest;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

/**
 * Tests for GlobalExceptionHandler
 */
@ExtendWith(MockitoExtension.class)
class GlobalExceptionHandlerTest {

    @Mock
    private WebRequest webRequest;

    @InjectMocks
    private GlobalExceptionHandler globalExceptionHandler;

    @BeforeEach
    void setUp() {
        when(webRequest.getDescription(false)).thenReturn("uri=/api/profiles/123");
    }

    @Test
    void handleProfileNotFound_ShouldReturnNotFoundResponse() {
        // Given
        ProfileNotFoundException exception = new ProfileNotFoundException("Profile with ID 123 not found");

        // When
        ResponseEntity<Map<String, Object>> response = globalExceptionHandler.handleProfileNotFound(exception, webRequest);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().get("status")).isEqualTo(404);
        assertThat(response.getBody().get("error")).isEqualTo("Not Found");
        assertThat(response.getBody().get("message")).isEqualTo("Profile with ID 123 not found");
        assertThat(response.getBody().get("path")).isEqualTo("/api/profiles/123");
        assertThat(response.getBody().get("timestamp")).isNotNull();
    }

    @Test
    void handleIllegalArgument_ShouldReturnBadRequestResponse() {
        // Given
        IllegalArgumentException exception = new IllegalArgumentException("Invalid profile data");

        // When
        ResponseEntity<Map<String, Object>> response = globalExceptionHandler.handleIllegalArgument(exception, webRequest);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().get("status")).isEqualTo(400);
        assertThat(response.getBody().get("error")).isEqualTo("Bad Request");
        assertThat(response.getBody().get("message")).isEqualTo("Invalid profile data");
        assertThat(response.getBody().get("path")).isEqualTo("/api/profiles/123");
        assertThat(response.getBody().get("timestamp")).isNotNull();
    }

    @Test
    void handleGlobalException_ShouldReturnInternalServerErrorResponse() {
        // Given
        Exception exception = new RuntimeException("Unexpected database error");

        // When
        ResponseEntity<Map<String, Object>> response = globalExceptionHandler.handleGlobalException(exception, webRequest);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().get("status")).isEqualTo(500);
        assertThat(response.getBody().get("error")).isEqualTo("Internal Server Error");
        assertThat(response.getBody().get("message")).isEqualTo("An unexpected error occurred");
        assertThat(response.getBody().get("path")).isEqualTo("/api/profiles/123");
        assertThat(response.getBody().get("timestamp")).isNotNull();
    }

    @Test
    void handleProfileNotFound_ShouldLogError() {
        // Given
        ProfileNotFoundException exception = new ProfileNotFoundException("Profile not found");

        // When
        globalExceptionHandler.handleProfileNotFound(exception, webRequest);

        // Then - no exception thrown, logging should occur
        // This test verifies the method executes without errors
    }

    @Test
    void handleIllegalArgument_ShouldLogError() {
        // Given
        IllegalArgumentException exception = new IllegalArgumentException("Validation failed");

        // When
        globalExceptionHandler.handleIllegalArgument(exception, webRequest);

        // Then - no exception thrown, logging should occur
        // This test verifies the method executes without errors
    }

    @Test
    void handleGlobalException_ShouldLogError() {
        // Given
        Exception exception = new NullPointerException("Null pointer");

        // When
        globalExceptionHandler.handleGlobalException(exception, webRequest);

        // Then - no exception thrown, logging should occur
        // This test verifies the method executes without errors
    }
}

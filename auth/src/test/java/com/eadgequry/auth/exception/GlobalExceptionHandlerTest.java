package com.eadgequry.auth.exception;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.context.request.WebRequest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GlobalExceptionHandlerTest {

    @InjectMocks
    private GlobalExceptionHandler globalExceptionHandler;

    @Mock
    private WebRequest webRequest;

    @Test
    void handleBadCredentials() {
        BadCredentialsException exception = new BadCredentialsException("Bad credentials");
        when(webRequest.getDescription(false)).thenReturn("uri=/login");

        ResponseEntity<ErrorResponse> response = globalExceptionHandler.handleBadCredentials(exception, webRequest);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().status()).isEqualTo(401);
        assertThat(response.getBody().error()).isEqualTo("Unauthorized");
        assertThat(response.getBody().message()).contains("Invalid email or password");
        assertThat(response.getBody().path()).isEqualTo("/login");
        assertThat(response.getBody().timestamp()).isNotNull();
    }

    @Test
    void handleUsernameNotFound() {
        UsernameNotFoundException exception = new UsernameNotFoundException("User not found");
        when(webRequest.getDescription(false)).thenReturn("uri=/login");

        ResponseEntity<ErrorResponse> response = globalExceptionHandler.handleUsernameNotFound(exception, webRequest);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().status()).isEqualTo(401);
        assertThat(response.getBody().error()).isEqualTo("Unauthorized");
        assertThat(response.getBody().message()).contains("Invalid email or password");
        assertThat(response.getBody().path()).isEqualTo("/login");
        assertThat(response.getBody().timestamp()).isNotNull();
    }

    @Test
    void handleIllegalArgument() {
        IllegalArgumentException exception = new IllegalArgumentException("Email already registered");
        when(webRequest.getDescription(false)).thenReturn("uri=/register");

        ResponseEntity<ErrorResponse> response = globalExceptionHandler.handleIllegalArgument(exception, webRequest);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().status()).isEqualTo(400);
        assertThat(response.getBody().error()).isEqualTo("Bad Request");
        assertThat(response.getBody().message()).isEqualTo("Email already registered");
        assertThat(response.getBody().path()).isEqualTo("/register");
        assertThat(response.getBody().timestamp()).isNotNull();
    }

    @Test
    void handleGenericException() {
        Exception exception = new RuntimeException("Unexpected error");
        when(webRequest.getDescription(false)).thenReturn("uri=/test");

        ResponseEntity<ErrorResponse> response = globalExceptionHandler.handleGenericException(exception, webRequest);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().status()).isEqualTo(500);
        assertThat(response.getBody().error()).isEqualTo("Internal Server Error");
        assertThat(response.getBody().message()).contains("An unexpected error occurred");
        assertThat(response.getBody().path()).isEqualTo("/test");
        assertThat(response.getBody().timestamp()).isNotNull();
    }

    @Test
    void handleGenericException_NullPointerException() {
        Exception exception = new NullPointerException("Null value");
        when(webRequest.getDescription(false)).thenReturn("uri=/api/endpoint");

        ResponseEntity<ErrorResponse> response = globalExceptionHandler.handleGenericException(exception, webRequest);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().status()).isEqualTo(500);
        assertThat(response.getBody().message()).contains("An unexpected error occurred");
    }
}

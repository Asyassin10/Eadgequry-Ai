package com.eadgequry.auth.controller;

import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.eadgequry.auth.dto.ForgotPasswordRequest;
import com.eadgequry.auth.dto.RegisterRequest;
import com.eadgequry.auth.dto.UpdateEmailRequest;
import com.eadgequry.auth.dto.UpdatePasswordRequest;
import com.eadgequry.auth.dto.UserResponse;
import com.eadgequry.auth.dto.VerifyEmailRequest;
import com.eadgequry.auth.services.AuthService;

@RestController
@RequestMapping
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest request) {
        try {
            UserResponse user = authService.register(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(user);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Registration failed: " + e.getMessage()));
        }
    }
 
        @GetMapping("/test")
    public ResponseEntity<?> test() {
        return ResponseEntity.ok(Map.of(
            "status", "UP",
            "service", "auth"
        ));
    }

    @GetMapping("/health")
    public ResponseEntity<?> health() {
        return ResponseEntity.ok(Map.of(
            "status", "UP",
            "service", "auth"
        ));
    }

    /**
     * Forgot password endpoint (public)
     * POST /forgot-password
     * Request: { "email": "user@example.com" }
     */
    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(@RequestBody ForgotPasswordRequest request) {
        try {
            String message = authService.forgotPassword(request);
            return ResponseEntity.ok(Map.of("message", message));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to process request: " + e.getMessage()));
        }
    }

    /**
     * Verify email endpoint (public)
     * POST /verify-email
     * Request: { "token": "verification-token" }
     */
    @PostMapping("/verify-email")
    public ResponseEntity<?> verifyEmail(@RequestBody VerifyEmailRequest request) {
        try {
            String message = authService.verifyEmail(request);
            return ResponseEntity.ok(Map.of("message", message));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to verify email: " + e.getMessage()));
        }
    }

    /**
     * Update password endpoint (protected - requires JWT)
     * PUT /users/password
     * Request: { "currentPassword": "old", "newPassword": "new" }
     */
    @PutMapping("/users/password")
    public ResponseEntity<?> updatePassword(
            @AuthenticationPrincipal Jwt jwt,
            @RequestBody UpdatePasswordRequest request) {
        try {
            Long userId = jwt.getClaim("sub");
            String message = authService.updatePassword(userId, request);
            return ResponseEntity.ok(Map.of("message", message));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to update password: " + e.getMessage()));
        }
    }

    /**
     * Update email endpoint (protected - requires JWT)
     * PUT /users/email
     * Request: { "newEmail": "new@example.com", "password": "current-password" }
     */
    @PutMapping("/users/email")
    public ResponseEntity<?> updateEmail(
            @AuthenticationPrincipal Jwt jwt,
            @RequestBody UpdateEmailRequest request) {
        try {
            Long userId = jwt.getClaim("sub");
            String message = authService.updateEmail(userId, request);
            return ResponseEntity.ok(Map.of("message", message));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to update email: " + e.getMessage()));
        }
    }
}

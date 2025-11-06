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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.eadgequry.auth.dto.ForgotPasswordRequest;
import com.eadgequry.auth.dto.RegisterRequest;
import com.eadgequry.auth.dto.ResetPasswordRequest;
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
     * GET /verify-email?token=verification-token
     * Called when user clicks verification link in email
     */
    @GetMapping("/verify-email")
    public ResponseEntity<?> verifyEmail(@RequestParam("token") String token) {
        try {
            // Create VerifyEmailRequest from token parameter
            VerifyEmailRequest request = new VerifyEmailRequest(token);
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
     * Password reset GET endpoint (public)
     * GET /reset-password?token=reset-token
     * Called when user clicks password reset link in email
     * This validates the token and can return frontend URL or token validity status
     */
    @GetMapping("/reset-password")
    public ResponseEntity<?> getResetPassword(@RequestParam("token") String token) {
        try {
            if (token == null || token.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "Reset token is required"));
            }
            // Return token validity - frontend can use this to show password reset form
            return ResponseEntity.ok(Map.of(
                "message", "Token is valid. Please submit new password.",
                "token", token
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to validate reset token: " + e.getMessage()));
        }
    }

    /**
     * Password reset POST endpoint (public)
     * POST /reset-password
     * Request: { "token": "reset-token", "newPassword": "new-password" }
     * Actually resets the password
     */
    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@RequestBody ResetPasswordRequest request) {
        try {
            String message = authService.resetPassword(request);
            return ResponseEntity.ok(Map.of("message", message));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to reset password: " + e.getMessage()));
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
            // JWT subject is stored as String, parse to Long
            Long userId = Long.parseLong(jwt.getSubject());
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
            // JWT subject is stored as String, parse to Long
            Long userId = Long.parseLong(jwt.getSubject());
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

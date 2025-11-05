package com.eadgequry.auth.controller;

import com.eadgequry.auth.dto.*;
import com.eadgequry.auth.services.AuthService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AuthService authService;

    @Test
    void register_Success() throws Exception {
        RegisterRequest request = new RegisterRequest("John Doe", "john@example.com", "password123");
        UserResponse response = new UserResponse(1L, "john@example.com", "local", null);

        when(authService.register(any(RegisterRequest.class))).thenReturn(response);

        mockMvc.perform(post("/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.email").value("john@example.com"))
                .andExpect(jsonPath("$.provider").value("local"));
    }

    @Test
    void register_EmailAlreadyExists() throws Exception {
        RegisterRequest request = new RegisterRequest("John Doe", "existing@example.com", "password123");

        when(authService.register(any(RegisterRequest.class)))
                .thenThrow(new IllegalArgumentException("Email already registered"));

        mockMvc.perform(post("/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Email already registered"));
    }

    @Test
    void register_InvalidRequest() throws Exception {
        RegisterRequest request = new RegisterRequest("", "", "");

        when(authService.register(any(RegisterRequest.class)))
                .thenThrow(new IllegalArgumentException("Name is required"));

        mockMvc.perform(post("/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Name is required"));
    }

    @Test
    void register_InternalError() throws Exception {
        RegisterRequest request = new RegisterRequest("John Doe", "john@example.com", "password123");

        when(authService.register(any(RegisterRequest.class)))
                .thenThrow(new RuntimeException("Database error"));

        mockMvc.perform(post("/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.error").exists());
    }

    @Test
    void health_Success() throws Exception {
        mockMvc.perform(get("/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"))
                .andExpect(jsonPath("$.service").value("auth"));
    }

    @Test
    void test_Success() throws Exception {
        mockMvc.perform(get("/test"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"))
                .andExpect(jsonPath("$.service").value("auth"));
    }

    // ==================== Forgot Password Tests ====================

    @Test
    void forgotPassword_Success() throws Exception {
        ForgotPasswordRequest request = new ForgotPasswordRequest("john@example.com");

        when(authService.forgotPassword(any(ForgotPasswordRequest.class)))
                .thenReturn("Password reset link sent to email");

        mockMvc.perform(post("/forgot-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Password reset link sent to email"));
    }

    @Test
    void forgotPassword_UserNotFound() throws Exception {
        ForgotPasswordRequest request = new ForgotPasswordRequest("notfound@example.com");

        when(authService.forgotPassword(any(ForgotPasswordRequest.class)))
                .thenThrow(new IllegalArgumentException("User not found"));

        mockMvc.perform(post("/forgot-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("User not found"));
    }

    @Test
    void forgotPassword_InternalError() throws Exception {
        ForgotPasswordRequest request = new ForgotPasswordRequest("john@example.com");

        when(authService.forgotPassword(any(ForgotPasswordRequest.class)))
                .thenThrow(new RuntimeException("Service unavailable"));

        mockMvc.perform(post("/forgot-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.error").exists());
    }

    // ==================== Verify Email Tests ====================

    @Test
    void verifyEmail_Success() throws Exception {
        when(authService.verifyEmail(any(VerifyEmailRequest.class)))
                .thenReturn("Email verified successfully");

        mockMvc.perform(get("/verify-email")
                        .param("token", "valid-token-123"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Email verified successfully"));
    }

    @Test
    void verifyEmail_InvalidToken() throws Exception {
        when(authService.verifyEmail(any(VerifyEmailRequest.class)))
                .thenThrow(new IllegalArgumentException("Invalid verification token"));

        mockMvc.perform(get("/verify-email")
                        .param("token", "invalid-token"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Invalid verification token"));
    }

    @Test
    void verifyEmail_ExpiredToken() throws Exception {
        when(authService.verifyEmail(any(VerifyEmailRequest.class)))
                .thenThrow(new IllegalArgumentException("Verification token has expired"));

        mockMvc.perform(get("/verify-email")
                        .param("token", "expired-token"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Verification token has expired"));
    }

    @Test
    void verifyEmail_InternalError() throws Exception {
        when(authService.verifyEmail(any(VerifyEmailRequest.class)))
                .thenThrow(new RuntimeException("Database error"));

        mockMvc.perform(get("/verify-email")
                        .param("token", "some-token"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.error").exists());
    }

    // ==================== Reset Password GET Tests ====================

    @Test
    void getResetPassword_ValidToken() throws Exception {
        mockMvc.perform(get("/reset-password")
                        .param("token", "valid-reset-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Token is valid. Please submit new password."))
                .andExpect(jsonPath("$.token").value("valid-reset-token"));
    }

    @Test
    void getResetPassword_EmptyToken() throws Exception {
        mockMvc.perform(get("/reset-password")
                        .param("token", ""))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Reset token is required"));
    }

    @Test
    void getResetPassword_NullToken() throws Exception {
        mockMvc.perform(get("/reset-password"))
                .andExpect(status().isBadRequest());
    }

    // ==================== Reset Password POST Tests ====================

    @Test
    void resetPassword_Success() throws Exception {
        ResetPasswordRequest request = new ResetPasswordRequest("reset-token", "newPassword123");

        when(authService.resetPassword(any(ResetPasswordRequest.class)))
                .thenReturn("Password reset successfully");

        mockMvc.perform(post("/reset-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Password reset successfully"));
    }

    @Test
    void resetPassword_InvalidToken() throws Exception {
        ResetPasswordRequest request = new ResetPasswordRequest("invalid-token", "newPassword123");

        when(authService.resetPassword(any(ResetPasswordRequest.class)))
                .thenThrow(new IllegalArgumentException("Invalid or expired reset token"));

        mockMvc.perform(post("/reset-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Invalid or expired reset token"));
    }

    @Test
    void resetPassword_InternalError() throws Exception {
        ResetPasswordRequest request = new ResetPasswordRequest("some-token", "newPassword123");

        when(authService.resetPassword(any(ResetPasswordRequest.class)))
                .thenThrow(new RuntimeException("Database error"));

        mockMvc.perform(post("/reset-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.error").exists());
    }

    // ==================== Update Password Tests (Protected) ====================

    @Test
    @WithMockUser
    void updatePassword_Success() throws Exception {
        UpdatePasswordRequest request = new UpdatePasswordRequest("oldPassword", "newPassword123");

        when(authService.updatePassword(anyLong(), any(UpdatePasswordRequest.class)))
                .thenReturn("Password updated successfully");

        mockMvc.perform(put("/users/password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Password updated successfully"));
    }

    @Test
    @WithMockUser
    void updatePassword_WrongCurrentPassword() throws Exception {
        UpdatePasswordRequest request = new UpdatePasswordRequest("wrongPassword", "newPassword123");

        when(authService.updatePassword(anyLong(), any(UpdatePasswordRequest.class)))
                .thenThrow(new IllegalArgumentException("Current password is incorrect"));

        mockMvc.perform(put("/users/password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Current password is incorrect"));
    }

    @Test
    @WithMockUser
    void updatePassword_InternalError() throws Exception {
        UpdatePasswordRequest request = new UpdatePasswordRequest("oldPassword", "newPassword123");

        when(authService.updatePassword(anyLong(), any(UpdatePasswordRequest.class)))
                .thenThrow(new RuntimeException("Database error"));

        mockMvc.perform(put("/users/password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.error").exists());
    }

    // ==================== Update Email Tests (Protected) ====================

    @Test
    @WithMockUser
    void updateEmail_Success() throws Exception {
        UpdateEmailRequest request = new UpdateEmailRequest("newemail@example.com", "currentPassword");

        when(authService.updateEmail(anyLong(), any(UpdateEmailRequest.class)))
                .thenReturn("Email updated successfully. Please verify your new email.");

        mockMvc.perform(put("/users/email")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Email updated successfully. Please verify your new email."));
    }

    @Test
    @WithMockUser
    void updateEmail_EmailAlreadyInUse() throws Exception {
        UpdateEmailRequest request = new UpdateEmailRequest("existing@example.com", "currentPassword");

        when(authService.updateEmail(anyLong(), any(UpdateEmailRequest.class)))
                .thenThrow(new IllegalArgumentException("Email already in use"));

        mockMvc.perform(put("/users/email")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Email already in use"));
    }

    @Test
    @WithMockUser
    void updateEmail_WrongPassword() throws Exception {
        UpdateEmailRequest request = new UpdateEmailRequest("newemail@example.com", "wrongPassword");

        when(authService.updateEmail(anyLong(), any(UpdateEmailRequest.class)))
                .thenThrow(new IllegalArgumentException("Password is incorrect"));

        mockMvc.perform(put("/users/email")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Password is incorrect"));
    }

    @Test
    @WithMockUser
    void updateEmail_InternalError() throws Exception {
        UpdateEmailRequest request = new UpdateEmailRequest("newemail@example.com", "currentPassword");

        when(authService.updateEmail(anyLong(), any(UpdateEmailRequest.class)))
                .thenThrow(new RuntimeException("Database error"));

        mockMvc.perform(put("/users/email")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.error").exists());
    }
}

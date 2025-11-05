package com.eadgequry.auth.services;

import com.eadgequry.auth.client.ProfileServiceClient;
import com.eadgequry.auth.client.dto.ProfileResponse;
import com.eadgequry.auth.dto.ForgotPasswordRequest;
import com.eadgequry.auth.dto.ResetPasswordRequest;
import com.eadgequry.auth.dto.UpdateEmailRequest;
import com.eadgequry.auth.dto.UpdatePasswordRequest;
import com.eadgequry.auth.event.EmailUpdatedEvent;
import com.eadgequry.auth.event.EventProducer;
import com.eadgequry.auth.event.ForgotPasswordEvent;
import com.eadgequry.auth.model.User;
import com.eadgequry.auth.repository.UserRepository;
import com.eadgequry.auth.repository.VerificationTokenRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServicePasswordEmailTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private ProfileServiceClient profileServiceClient;

    @Mock
    private EventProducer eventProducer;

    @Mock
    private VerificationTokenRepository verificationTokenRepository;

    @InjectMocks
    private AuthService authService;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setEmail("john@example.com");
        testUser.setPassword("$2a$10$encodedOldPassword");
        testUser.setProvider("local");
        testUser.setEmailVerifiedAt(LocalDateTime.now());
    }

    // ==================== Forgot Password Tests ====================

    @Test
    void forgotPassword_Success() {
        ForgotPasswordRequest request = new ForgotPasswordRequest("john@example.com");

        ProfileResponse profileResponse = new ProfileResponse(
                1L, 1L, "John Doe", null, "Bio", null, null, null
        );

        when(userRepository.findByEmail("john@example.com")).thenReturn(Optional.of(testUser));
        when(profileServiceClient.getProfile(1L)).thenReturn(ResponseEntity.ok(profileResponse));
        doNothing().when(eventProducer).publishForgotPassword(any(ForgotPasswordEvent.class));

        String result = authService.forgotPassword(request);

        assertThat(result).isEqualTo("Password reset link sent to email");
        verify(userRepository).findByEmail("john@example.com");
        verify(profileServiceClient).getProfile(1L);
        verify(eventProducer).publishForgotPassword(any(ForgotPasswordEvent.class));
    }

    @Test
    void forgotPassword_UserNotFound() {
        ForgotPasswordRequest request = new ForgotPasswordRequest("notfound@example.com");

        when(userRepository.findByEmail("notfound@example.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.forgotPassword(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("User not found");

        verify(userRepository).findByEmail("notfound@example.com");
        verify(eventProducer, never()).publishForgotPassword(any());
    }

    @Test
    void forgotPassword_InvalidEmail() {
        ForgotPasswordRequest request = new ForgotPasswordRequest(null);

        assertThatThrownBy(() -> authService.forgotPassword(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Email is required");

        verify(userRepository, never()).findByEmail(anyString());
    }

    @Test
    void forgotPassword_EmptyEmail() {
        ForgotPasswordRequest request = new ForgotPasswordRequest("");

        assertThatThrownBy(() -> authService.forgotPassword(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Email is required");

        verify(userRepository, never()).findByEmail(anyString());
    }

    @Test
    void forgotPassword_ProfileServiceFails_UsesDefaultName() {
        ForgotPasswordRequest request = new ForgotPasswordRequest("john@example.com");

        when(userRepository.findByEmail("john@example.com")).thenReturn(Optional.of(testUser));
        when(profileServiceClient.getProfile(1L)).thenThrow(new RuntimeException("Service unavailable"));
        doNothing().when(eventProducer).publishForgotPassword(any(ForgotPasswordEvent.class));

        String result = authService.forgotPassword(request);

        assertThat(result).isEqualTo("Password reset link sent to email");
        verify(eventProducer).publishForgotPassword(argThat(event ->
                event.name().equals("User") // Should use default name
        ));
    }

    @Test
    void forgotPassword_EventPublishFails() {
        ForgotPasswordRequest request = new ForgotPasswordRequest("john@example.com");

        ProfileResponse profileResponse = new ProfileResponse(
                1L, 1L, "John Doe", null, "Bio", null, null, null
        );

        when(userRepository.findByEmail("john@example.com")).thenReturn(Optional.of(testUser));
        when(profileServiceClient.getProfile(1L)).thenReturn(ResponseEntity.ok(profileResponse));
        doThrow(new RuntimeException("Kafka unavailable"))
                .when(eventProducer).publishForgotPassword(any(ForgotPasswordEvent.class));

        assertThatThrownBy(() -> authService.forgotPassword(request))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Failed to send password reset email");
    }

    // ==================== Reset Password Tests ====================

    @Test
    void resetPassword_Success() {
        ResetPasswordRequest request = new ResetPasswordRequest("valid-token", "newPassword123");

        String result = authService.resetPassword(request);

        assertThat(result).isEqualTo("Password reset successfully");
    }

    @Test
    void resetPassword_InvalidToken() {
        ResetPasswordRequest request = new ResetPasswordRequest(null, "newPassword123");

        assertThatThrownBy(() -> authService.resetPassword(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Reset token is required");
    }

    @Test
    void resetPassword_EmptyToken() {
        ResetPasswordRequest request = new ResetPasswordRequest("", "newPassword123");

        assertThatThrownBy(() -> authService.resetPassword(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Reset token is required");
    }

    @Test
    void resetPassword_InvalidPassword() {
        ResetPasswordRequest request = new ResetPasswordRequest("valid-token", "short");

        assertThatThrownBy(() -> authService.resetPassword(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Password must be at least 6 characters");
    }

    @Test
    void resetPassword_NullPassword() {
        ResetPasswordRequest request = new ResetPasswordRequest("valid-token", null);

        assertThatThrownBy(() -> authService.resetPassword(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Password must be at least 6 characters");
    }

    // ==================== Update Password Tests ====================

    @Test
    void updatePassword_Success() {
        UpdatePasswordRequest request = new UpdatePasswordRequest("oldPassword", "newPassword123");

        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("oldPassword", "$2a$10$encodedOldPassword")).thenReturn(true);
        when(passwordEncoder.encode("newPassword123")).thenReturn("$2a$10$encodedNewPassword");
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        String result = authService.updatePassword(1L, request);

        assertThat(result).isEqualTo("Password updated successfully");
        verify(userRepository).findById(1L);
        verify(passwordEncoder).matches("oldPassword", "$2a$10$encodedOldPassword");
        verify(passwordEncoder).encode("newPassword123");
        verify(userRepository).save(testUser);
    }

    @Test
    void updatePassword_UserNotFound() {
        UpdatePasswordRequest request = new UpdatePasswordRequest("oldPassword", "newPassword123");

        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.updatePassword(1L, request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("User not found");

        verify(userRepository).findById(1L);
        verify(passwordEncoder, never()).matches(anyString(), anyString());
    }

    @Test
    void updatePassword_WrongCurrentPassword() {
        UpdatePasswordRequest request = new UpdatePasswordRequest("wrongPassword", "newPassword123");

        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("wrongPassword", "$2a$10$encodedOldPassword")).thenReturn(false);

        assertThatThrownBy(() -> authService.updatePassword(1L, request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Current password is incorrect");

        verify(passwordEncoder).matches("wrongPassword", "$2a$10$encodedOldPassword");
        verify(userRepository, never()).save(any());
    }

    @Test
    void updatePassword_InvalidCurrentPassword() {
        UpdatePasswordRequest request = new UpdatePasswordRequest(null, "newPassword123");

        assertThatThrownBy(() -> authService.updatePassword(1L, request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Current password is required");

        verify(userRepository, never()).findById(anyLong());
    }

    @Test
    void updatePassword_InvalidNewPassword() {
        UpdatePasswordRequest request = new UpdatePasswordRequest("oldPassword", "short");

        assertThatThrownBy(() -> authService.updatePassword(1L, request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("New password must be at least 8 characters");

        verify(userRepository, never()).findById(anyLong());
    }

    // ==================== Update Email Tests ====================

    @Test
    void updateEmail_Success() {
        UpdateEmailRequest request = new UpdateEmailRequest("newemail@example.com", "currentPassword");

        ProfileResponse profileResponse = new ProfileResponse(
                1L, 1L, "John Doe", null, "Bio", null, null, null
        );

        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("currentPassword", "$2a$10$encodedOldPassword")).thenReturn(true);
        when(userRepository.existsByEmail("newemail@example.com")).thenReturn(false);
        doNothing().when(verificationTokenRepository).deleteByUserId(1L);
        when(userRepository.save(any(User.class))).thenReturn(testUser);
        when(profileServiceClient.getProfile(1L)).thenReturn(ResponseEntity.ok(profileResponse));
        when(verificationTokenRepository.save(any())).thenReturn(null);
        doNothing().when(eventProducer).publishEmailUpdated(any(EmailUpdatedEvent.class));

        String result = authService.updateEmail(1L, request);

        assertThat(result).isEqualTo("Email updated successfully. Please verify your new email.");
        verify(userRepository).findById(1L);
        verify(passwordEncoder).matches("currentPassword", "$2a$10$encodedOldPassword");
        verify(userRepository).existsByEmail("newemail@example.com");
        verify(verificationTokenRepository).deleteByUserId(1L);
        verify(userRepository).save(testUser);
        verify(eventProducer).publishEmailUpdated(any(EmailUpdatedEvent.class));
    }

    @Test
    void updateEmail_UserNotFound() {
        UpdateEmailRequest request = new UpdateEmailRequest("newemail@example.com", "currentPassword");

        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.updateEmail(1L, request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("User not found");

        verify(userRepository).findById(1L);
        verify(passwordEncoder, never()).matches(anyString(), anyString());
    }

    @Test
    void updateEmail_WrongPassword() {
        UpdateEmailRequest request = new UpdateEmailRequest("newemail@example.com", "wrongPassword");

        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("wrongPassword", "$2a$10$encodedOldPassword")).thenReturn(false);

        assertThatThrownBy(() -> authService.updateEmail(1L, request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Password is incorrect");

        verify(passwordEncoder).matches("wrongPassword", "$2a$10$encodedOldPassword");
        verify(userRepository, never()).existsByEmail(anyString());
    }

    @Test
    void updateEmail_EmailAlreadyInUse() {
        UpdateEmailRequest request = new UpdateEmailRequest("existing@example.com", "currentPassword");

        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("currentPassword", "$2a$10$encodedOldPassword")).thenReturn(true);
        when(userRepository.existsByEmail("existing@example.com")).thenReturn(true);

        assertThatThrownBy(() -> authService.updateEmail(1L, request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Email already in use");

        verify(userRepository).existsByEmail("existing@example.com");
        verify(userRepository, never()).save(any());
    }

    @Test
    void updateEmail_InvalidEmail() {
        UpdateEmailRequest request = new UpdateEmailRequest(null, "currentPassword");

        assertThatThrownBy(() -> authService.updateEmail(1L, request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("New email is required");

        verify(userRepository, never()).findById(anyLong());
    }

    @Test
    void updateEmail_EmptyEmail() {
        UpdateEmailRequest request = new UpdateEmailRequest("", "currentPassword");

        assertThatThrownBy(() -> authService.updateEmail(1L, request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("New email is required");

        verify(userRepository, never()).findById(anyLong());
    }

    @Test
    void updateEmail_InvalidPassword() {
        UpdateEmailRequest request = new UpdateEmailRequest("newemail@example.com", null);

        assertThatThrownBy(() -> authService.updateEmail(1L, request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Password is required for email update");

        verify(userRepository, never()).findById(anyLong());
    }

    @Test
    void updateEmail_ProfileServiceFails_UsesDefaultName() {
        UpdateEmailRequest request = new UpdateEmailRequest("newemail@example.com", "currentPassword");

        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("currentPassword", "$2a$10$encodedOldPassword")).thenReturn(true);
        when(userRepository.existsByEmail("newemail@example.com")).thenReturn(false);
        doNothing().when(verificationTokenRepository).deleteByUserId(1L);
        when(userRepository.save(any(User.class))).thenReturn(testUser);
        when(profileServiceClient.getProfile(1L)).thenThrow(new RuntimeException("Service unavailable"));
        when(verificationTokenRepository.save(any())).thenReturn(null);
        doNothing().when(eventProducer).publishEmailUpdated(any(EmailUpdatedEvent.class));

        String result = authService.updateEmail(1L, request);

        assertThat(result).isEqualTo("Email updated successfully. Please verify your new email.");
        verify(eventProducer).publishEmailUpdated(argThat(event ->
                event.name().equals("User") // Should use default name
        ));
    }
}

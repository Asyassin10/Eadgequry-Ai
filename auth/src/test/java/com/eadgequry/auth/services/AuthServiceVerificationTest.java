package com.eadgequry.auth.services;

import com.eadgequry.auth.client.ProfileServiceClient;
import com.eadgequry.auth.dto.VerifyEmailRequest;
import com.eadgequry.auth.event.EventProducer;
import com.eadgequry.auth.model.User;
import com.eadgequry.auth.model.VerificationToken;
import com.eadgequry.auth.repository.UserRepository;
import com.eadgequry.auth.repository.VerificationTokenRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceVerificationTest {

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
    private VerificationToken testToken;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setEmail("test@example.com");
        testUser.setPassword("hashedPassword");
        testUser.setEmailVerifiedAt(null);

        testToken = new VerificationToken(
                1L,
                "test-token",
                LocalDateTime.now().plusHours(24)
        );
    }

    @Test
    void verifyEmail_Success() {
        VerifyEmailRequest request = new VerifyEmailRequest("test-token");

        when(verificationTokenRepository.findByToken("test-token")).thenReturn(Optional.of(testToken));
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);
        doNothing().when(verificationTokenRepository).delete(testToken);

        String result = authService.verifyEmail(request);

        assertEquals("Email verified successfully", result);
        assertNotNull(testUser.getEmailVerifiedAt());
        verify(verificationTokenRepository).findByToken("test-token");
        verify(userRepository).findById(1L);
        verify(userRepository).save(testUser);
        verify(verificationTokenRepository).delete(testToken);
    }

    @Test
    void verifyEmail_TokenNotFound() {
        VerifyEmailRequest request = new VerifyEmailRequest("invalid-token");

        when(verificationTokenRepository.findByToken("invalid-token")).thenReturn(Optional.empty());

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            authService.verifyEmail(request);
        });

        assertEquals("Invalid verification token", exception.getMessage());
        verify(verificationTokenRepository).findByToken("invalid-token");
        verify(userRepository, never()).findById(any());
    }

    @Test
    void verifyEmail_TokenExpired() {
        VerifyEmailRequest request = new VerifyEmailRequest("expired-token");
        VerificationToken expiredToken = new VerificationToken(
                1L,
                "expired-token",
                LocalDateTime.now().minusHours(1) // Expired 1 hour ago
        );

        when(verificationTokenRepository.findByToken("expired-token")).thenReturn(Optional.of(expiredToken));

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            authService.verifyEmail(request);
        });

        assertTrue(exception.getMessage().contains("expired"));
        verify(verificationTokenRepository).findByToken("expired-token");
        verify(userRepository, never()).findById(any());
    }

    @Test
    void verifyEmail_UserNotFound() {
        VerifyEmailRequest request = new VerifyEmailRequest("test-token");

        when(verificationTokenRepository.findByToken("test-token")).thenReturn(Optional.of(testToken));
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            authService.verifyEmail(request);
        });

        assertEquals("User not found", exception.getMessage());
        verify(verificationTokenRepository).findByToken("test-token");
        verify(userRepository).findById(1L);
        verify(userRepository, never()).save(any());
    }

    @Test
    void verifyEmail_AlreadyVerified() {
        VerifyEmailRequest request = new VerifyEmailRequest("test-token");
        testUser.setEmailVerifiedAt(LocalDateTime.now().minusDays(1));

        when(verificationTokenRepository.findByToken("test-token")).thenReturn(Optional.of(testToken));
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        doNothing().when(verificationTokenRepository).delete(testToken);

        String result = authService.verifyEmail(request);

        assertEquals("Email already verified", result);
        verify(verificationTokenRepository).findByToken("test-token");
        verify(userRepository).findById(1L);
        verify(verificationTokenRepository).delete(testToken);
        verify(userRepository, never()).save(any());
    }
}

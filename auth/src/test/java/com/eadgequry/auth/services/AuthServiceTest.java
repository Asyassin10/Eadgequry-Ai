package com.eadgequry.auth.services;

import com.eadgequry.auth.client.ProfileServiceClient;
import com.eadgequry.auth.client.dto.CreateProfileRequest;
import com.eadgequry.auth.dto.RegisterRequest;
import com.eadgequry.auth.dto.UserResponse;
import com.eadgequry.auth.event.EventProducer;
import com.eadgequry.auth.model.User;
import com.eadgequry.auth.repository.UserRepository;
import com.eadgequry.auth.repository.VerificationTokenRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

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

    private RegisterRequest validRequest;
    private User savedUser;

    @BeforeEach
    void setUp() {
        validRequest = new RegisterRequest("John Doe", "john@example.com", "password123");

        savedUser = new User();
        savedUser.setId(1L);
        savedUser.setEmail("john@example.com");
        savedUser.setPassword("$2a$10$encodedPassword");
        savedUser.setProvider("local");
    }

    @Test
    void register_Success() {
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("$2a$10$encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(savedUser);
        when(verificationTokenRepository.save(any())).thenReturn(null);
        doNothing().when(eventProducer).publishUserRegistered(any());

        UserResponse response = authService.register(validRequest);

        assertThat(response).isNotNull();
        assertThat(response.id()).isEqualTo(1L);
        assertThat(response.email()).isEqualTo("john@example.com");
        assertThat(response.provider()).isEqualTo("local");

        verify(userRepository).existsByEmail("john@example.com");
        verify(passwordEncoder).encode("password123");
        verify(userRepository).save(any(User.class));
        verify(profileServiceClient).createProfile(any(CreateProfileRequest.class));
        verify(verificationTokenRepository).save(any());
        verify(eventProducer).publishUserRegistered(any());
    }

    @Test
    void register_EmailAlreadyExists() {
        when(userRepository.existsByEmail(anyString())).thenReturn(true);

        assertThatThrownBy(() -> authService.register(validRequest))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Email already registered");

        verify(userRepository).existsByEmail("john@example.com");
        verify(passwordEncoder, never()).encode(anyString());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void register_InvalidName_Empty() {
        RegisterRequest invalidRequest = new RegisterRequest("", "john@example.com", "password123");

        assertThatThrownBy(() -> authService.register(invalidRequest))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Name is required");

        verify(userRepository, never()).existsByEmail(anyString());
    }

    @Test
    void register_InvalidName_Null() {
        RegisterRequest invalidRequest = new RegisterRequest(null, "john@example.com", "password123");

        assertThatThrownBy(() -> authService.register(invalidRequest))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Name is required");

        verify(userRepository, never()).existsByEmail(anyString());
    }

    @Test
    void register_InvalidEmail_Empty() {
        RegisterRequest invalidRequest = new RegisterRequest("John Doe", "", "password123");

        assertThatThrownBy(() -> authService.register(invalidRequest))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Email is required");

        verify(userRepository, never()).existsByEmail(anyString());
    }

    @Test
    void register_InvalidEmail_Null() {
        RegisterRequest invalidRequest = new RegisterRequest("John Doe", null, "password123");

        assertThatThrownBy(() -> authService.register(invalidRequest))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Email is required");

        verify(userRepository, never()).existsByEmail(anyString());
    }

    @Test
    void register_InvalidEmail_Format() {
        RegisterRequest invalidRequest = new RegisterRequest("John Doe", "invalid-email", "password123");

        assertThatThrownBy(() -> authService.register(invalidRequest))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Invalid email format");

        verify(userRepository, never()).existsByEmail(anyString());
    }

    @Test
    void register_InvalidPassword_Short() {
        RegisterRequest invalidRequest = new RegisterRequest("John Doe", "john@example.com", "pass");

        assertThatThrownBy(() -> authService.register(invalidRequest))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Password must be at least 6 characters");

        verify(userRepository, never()).existsByEmail(anyString());
    }

    @Test
    void register_InvalidPassword_Null() {
        RegisterRequest invalidRequest = new RegisterRequest("John Doe", "john@example.com", null);

        assertThatThrownBy(() -> authService.register(invalidRequest))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Password must be at least 6 characters");

        verify(userRepository, never()).existsByEmail(anyString());
    }
}

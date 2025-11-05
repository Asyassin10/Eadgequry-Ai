package com.eadgequry.notification.consumer;

import com.eadgequry.notification.dto.EmailUpdatedEvent;
import com.eadgequry.notification.dto.ForgotPasswordEvent;
import com.eadgequry.notification.dto.UserRegisteredEvent;
import com.eadgequry.notification.service.EmailService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthEventConsumerTest {

    @Mock
    private EmailService emailService;

    @InjectMocks
    private AuthEventConsumer authEventConsumer;

    @Test
    void handleUserRegistered_Success() {
        UserRegisteredEvent event = new UserRegisteredEvent(
                1L,
                "John Doe",
                "john@example.com",
                "verification-token"
        );

        doNothing().when(emailService).sendVerificationEmail(
                anyString(), anyString(), anyString()
        );

        authEventConsumer.handleUserRegistered(event);

        verify(emailService).sendVerificationEmail(
                "john@example.com",
                "John Doe",
                "verification-token"
        );
    }

    @Test
    void handleUserRegistered_NullEvent() {
        authEventConsumer.handleUserRegistered(null);

        verify(emailService, never()).sendVerificationEmail(anyString(), anyString(), anyString());
    }

    @Test
    void handleForgotPassword_Success() {
        ForgotPasswordEvent event = new ForgotPasswordEvent(
                1L,
                "John Doe",
                "john@example.com",
                "reset-token"
        );

        doNothing().when(emailService).sendPasswordResetEmail(
                anyString(), anyString(), anyString()
        );

        authEventConsumer.handleForgotPassword(event);

        verify(emailService).sendPasswordResetEmail(
                "john@example.com",
                "John Doe",
                "reset-token"
        );
    }

    @Test
    void handleForgotPassword_NullEvent() {
        authEventConsumer.handleForgotPassword(null);

        verify(emailService, never()).sendPasswordResetEmail(anyString(), anyString(), anyString());
    }

    @Test
    void handleEmailUpdated_Success() {
        EmailUpdatedEvent event = new EmailUpdatedEvent(
                1L,
                "John Doe",
                "old@example.com",
                "new@example.com",
                "verification-token"
        );

        doNothing().when(emailService).sendEmailChangeNotificationOld(
                anyString(), anyString(), anyString()
        );
        doNothing().when(emailService).sendEmailChangeVerificationNew(
                anyString(), anyString(), anyString()
        );

        authEventConsumer.handleEmailUpdated(event);

        verify(emailService).sendEmailChangeNotificationOld(
                "old@example.com",
                "John Doe",
                "new@example.com"
        );
        verify(emailService).sendEmailChangeVerificationNew(
                "new@example.com",
                "John Doe",
                "verification-token"
        );
    }

    @Test
    void handleEmailUpdated_NullEvent() {
        authEventConsumer.handleEmailUpdated(null);

        verify(emailService, never()).sendEmailChangeNotificationOld(anyString(), anyString(), anyString());
        verify(emailService, never()).sendEmailChangeVerificationNew(anyString(), anyString(), anyString());
    }
}

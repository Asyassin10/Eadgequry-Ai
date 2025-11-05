package com.eadgequry.notification.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.util.ReflectionTestUtils;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmailServiceTest {

    @Mock
    private JavaMailSender mailSender;

    @Mock
    private TemplateEngine templateEngine;

    @Mock
    private MimeMessage mimeMessage;

    @InjectMocks
    private EmailService emailService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(emailService, "emailFrom", "noreply@eadgequry.com");
        ReflectionTestUtils.setField(emailService, "emailFromName", "Eadgequry");
        ReflectionTestUtils.setField(emailService, "baseUrl", "http://localhost:8765/auth");
    }

    @Test
    void sendVerificationEmail_Success() {
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
        when(templateEngine.process(anyString(), any(Context.class))).thenReturn("<html>Test</html>");
        doNothing().when(mailSender).send(any(MimeMessage.class));

        emailService.sendVerificationEmail("test@example.com", "John Doe", "test-token");

        verify(mailSender).createMimeMessage();
        verify(templateEngine).process(eq("email-verification"), any(Context.class));
        verify(mailSender).send(any(MimeMessage.class));
    }

    @Test
    void sendVerificationEmail_Failure() {
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
        when(templateEngine.process(anyString(), any(Context.class))).thenReturn("<html>Test</html>");
        doThrow(new RuntimeException("Mail server error")).when(mailSender).send(any(MimeMessage.class));

        assertThrows(RuntimeException.class, () -> {
            emailService.sendVerificationEmail("test@example.com", "John Doe", "test-token");
        });

        verify(mailSender).send(any(MimeMessage.class));
    }

    @Test
    void sendPasswordResetEmail_Success() {
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
        when(templateEngine.process(anyString(), any(Context.class))).thenReturn("<html>Test</html>");
        doNothing().when(mailSender).send(any(MimeMessage.class));

        emailService.sendPasswordResetEmail("test@example.com", "John Doe", "reset-token");

        verify(mailSender).createMimeMessage();
        verify(templateEngine).process(eq("password-reset"), any(Context.class));
        verify(mailSender).send(any(MimeMessage.class));
    }

    @Test
    void sendPasswordResetEmail_Failure() {
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
        when(templateEngine.process(anyString(), any(Context.class))).thenReturn("<html>Test</html>");
        doThrow(new RuntimeException("Mail server error")).when(mailSender).send(any(MimeMessage.class));

        assertThrows(RuntimeException.class, () -> {
            emailService.sendPasswordResetEmail("test@example.com", "John Doe", "reset-token");
        });

        verify(mailSender).send(any(MimeMessage.class));
    }

    @Test
    void sendEmailChangeNotificationOld_Success() {
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
        when(templateEngine.process(anyString(), any(Context.class))).thenReturn("<html>Test</html>");
        doNothing().when(mailSender).send(any(MimeMessage.class));

        emailService.sendEmailChangeNotificationOld("old@example.com", "John Doe", "new@example.com");

        verify(mailSender).createMimeMessage();
        verify(templateEngine).process(eq("email-change-old"), any(Context.class));
        verify(mailSender).send(any(MimeMessage.class));
    }

    @Test
    void sendEmailChangeNotificationOld_Failure() {
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
        when(templateEngine.process(anyString(), any(Context.class))).thenReturn("<html>Test</html>");
        doThrow(new RuntimeException("Mail server error")).when(mailSender).send(any(MimeMessage.class));

        assertThrows(RuntimeException.class, () -> {
            emailService.sendEmailChangeNotificationOld("old@example.com", "John Doe", "new@example.com");
        });

        verify(mailSender).send(any(MimeMessage.class));
    }

    @Test
    void sendEmailChangeVerificationNew_Success() {
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
        when(templateEngine.process(anyString(), any(Context.class))).thenReturn("<html>Test</html>");
        doNothing().when(mailSender).send(any(MimeMessage.class));

        emailService.sendEmailChangeVerificationNew("new@example.com", "John Doe", "verify-token");

        verify(mailSender).createMimeMessage();
        verify(templateEngine).process(eq("email-change-new"), any(Context.class));
        verify(mailSender).send(any(MimeMessage.class));
    }

    @Test
    void sendEmailChangeVerificationNew_Failure() {
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
        when(templateEngine.process(anyString(), any(Context.class))).thenReturn("<html>Test</html>");
        doThrow(new RuntimeException("Mail server error")).when(mailSender).send(any(MimeMessage.class));

        assertThrows(RuntimeException.class, () -> {
            emailService.sendEmailChangeVerificationNew("new@example.com", "John Doe", "verify-token");
        });

        verify(mailSender).send(any(MimeMessage.class));
    }
}

package com.eadgequry.notification.consumer;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import com.eadgequry.notification.dto.EmailUpdatedEvent;
import com.eadgequry.notification.dto.ForgotPasswordEvent;
import com.eadgequry.notification.dto.UserRegisteredEvent;
import com.eadgequry.notification.service.EmailService;

/**
 * Kafka consumer for authentication events
 * Listens to events from Auth Service and sends appropriate emails
 */
@Component
public class AuthEventConsumer {

    private final EmailService emailService;

    public AuthEventConsumer(EmailService emailService) {
        this.emailService = emailService;
    }

    @KafkaListener(
        topics = "${kafka.topics.user-registered}", 
        groupId = "${spring.kafka.consumer.group-id}",
        containerFactory = "userRegisteredKafkaListenerFactory"
    )
    public void handleUserRegistered(UserRegisteredEvent event) {
        emailService.sendVerificationEmail(event.email(), event.name(), event.verificationToken());
    }

    @KafkaListener(
        topics = "${kafka.topics.password-forgot}", 
        groupId = "${spring.kafka.consumer.group-id}",
        containerFactory = "forgotPasswordKafkaListenerFactory"
    )
    public void handleForgotPassword(ForgotPasswordEvent event) {
        emailService.sendPasswordResetEmail(event.email(), event.name(), event.resetToken());
    }

    @KafkaListener(
        topics = "${kafka.topics.email-updated}", 
        groupId = "${spring.kafka.consumer.group-id}",
        containerFactory = "emailUpdatedKafkaListenerFactory"
    )
    public void handleEmailUpdated(EmailUpdatedEvent event) {
        emailService.sendEmailChangeNotificationOld(event.oldEmail(), event.name(), event.newEmail());
        emailService.sendEmailChangeVerificationNew(event.newEmail(), event.name(), event.verificationToken());
    }
}

package com.eadgequry.notification.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

/**
 * Service for sending emails using JavaMailSender and Thymeleaf templates
 */
@Service
public class EmailService {

    private static final Logger logger = LoggerFactory.getLogger(EmailService.class);

    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;

    @Value("${notification.email.from}")
    private String emailFrom;

    @Value("${notification.email.from-name}")
    private String emailFromName;

    @Value("${notification.base-url}")
    private String baseUrl;

    public EmailService(JavaMailSender mailSender, TemplateEngine templateEngine) {
        this.mailSender = mailSender;
        this.templateEngine = templateEngine;
    }

    /**
     * Send welcome/verification email after user registration
     */
    public void sendVerificationEmail(String toEmail, String userName, String verificationToken) {
        try {
            Context context = new Context();
            context.setVariable("userName", userName);
            context.setVariable("verificationLink", baseUrl + "/verify-email?token=" + verificationToken);

            String htmlContent = templateEngine.process("email-verification", context);

            sendHtmlEmail(toEmail, "Verify Your Email - Eadgequry", htmlContent);
            logger.info("Verification email sent to: {}", toEmail);
        } catch (Exception e) {
            logger.error("Failed to send verification email to: {}", toEmail, e);
            throw new RuntimeException("Failed to send verification email", e);
        }
    }

    /**
     * Send password reset email
     */
    public void sendPasswordResetEmail(String toEmail, String userName, String resetToken) {
        try {
            Context context = new Context();
            context.setVariable("userName", userName);
            context.setVariable("resetLink", baseUrl + "/reset-password?token=" + resetToken);

            String htmlContent = templateEngine.process("password-reset", context);

            sendHtmlEmail(toEmail, "Reset Your Password - Eadgequry", htmlContent);
            logger.info("Password reset email sent to: {}", toEmail);
        } catch (Exception e) {
            logger.error("Failed to send password reset email to: {}", toEmail, e);
            throw new RuntimeException("Failed to send password reset email", e);
        }
    }

    /**
     * Send email update notification to old email
     */
    public void sendEmailChangeNotificationOld(String toEmail, String userName, String newEmail) {
        try {
            Context context = new Context();
            context.setVariable("userName", userName);
            context.setVariable("newEmail", newEmail);

            String htmlContent = templateEngine.process("email-change-old", context);

            sendHtmlEmail(toEmail, "Your Email Was Changed - Eadgequry", htmlContent);
            logger.info("Email change notification sent to old email: {}", toEmail);
        } catch (Exception e) {
            logger.error("Failed to send email change notification to: {}", toEmail, e);
            throw new RuntimeException("Failed to send email change notification", e);
        }
    }

    /**
     * Send email verification to new email
     */
    public void sendEmailChangeVerificationNew(String toEmail, String userName, String verificationToken) {
        try {
            Context context = new Context();
            context.setVariable("userName", userName);
            context.setVariable("verificationLink", baseUrl + "/verify-email?token=" + verificationToken);

            String htmlContent = templateEngine.process("email-change-new", context);

            sendHtmlEmail(toEmail, "Verify Your New Email - Eadgequry", htmlContent);
            logger.info("Email change verification sent to new email: {}", toEmail);
        } catch (Exception e) {
            logger.error("Failed to send email change verification to: {}", toEmail, e);
            throw new RuntimeException("Failed to send email change verification", e);
        }
    }

    /**
     * Helper method to send HTML email
     */
    private void sendHtmlEmail(String to, String subject, String htmlContent) throws MessagingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

        helper.setFrom(emailFrom, emailFromName);
        helper.setTo(to);
        helper.setSubject(subject);
        helper.setText(htmlContent, true);

        mailSender.send(message);
    }
}

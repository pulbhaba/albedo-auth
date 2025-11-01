package com.akbo.auth.api.service.notification.impl;

import static java.util.Objects.isNull;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import com.akbo.auth.api.service.notification.PasswordResetNotificationService;
import com.akbo.auth.dao.entity.User;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
@ConditionalOnProperty(name = "notification.email.enabled", havingValue = "true", matchIfMissing = false)
public class EmailPasswordResetNotificationService implements PasswordResetNotificationService {

    private final JavaMailSender mailSender;

    @Value("${notification.email.from-address}")
    private String fromAddress;

    @Value("${notification.email.subject:Albedo Auth Password Reset}")
    private String subject;

    @Value("${notification.email.reset-url:https://example.com/reset?token=}")
    private String resetUrl;

    @Value("${notification.email.body-template:Hello %s,%n%nYou requested to reset your password. "
            + "Use the following link to proceed:%n%s%n%nIf you did not request this change, please ignore this email.%n}")
    private String bodyTemplate;

    @Override
    public void notify(final User user, final String encryptedKey) {
        if (isNull(user)) {
            log.info("Skipping password reset email because the requested username does not exist.");
            return;
        }
        if (isNull(user.getEmailAddress()) || user.getEmailAddress().isBlank()) {
            log.info("Skipping password reset email because user {} does not have a registered email address.",
                    user.getUsername());
            return;
        }
        final String resetLink = resetUrl + encryptedKey;
        final String recipientName = (user.getFirstName() != null && !user.getFirstName().isBlank())
                ? user.getFirstName()
                : user.getUsername();
        final String body = String.format(bodyTemplate, recipientName, resetLink);

        final SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromAddress);
        message.setTo(user.getEmailAddress());
        message.setSubject(subject);
        message.setText(body);
        mailSender.send(message);

        log.info("Password reset email queued for {}", user.getEmailAddress());
    }
}

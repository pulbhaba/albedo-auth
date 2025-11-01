package com.akbo.auth.api.service.notification.impl;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import com.akbo.auth.api.service.notification.PasswordResetNotificationService;
import com.akbo.auth.dao.entity.User;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@ConditionalOnProperty(name = "notification.sms.enabled", havingValue = "true", matchIfMissing = false)
public class SmsPasswordResetNotificationService implements PasswordResetNotificationService {

    @Override
    public void notify(final User user, final String encryptedKey) {
        if (user == null) {
            log.info("Skipping password reset SMS because the requested username does not exist.");
            return;
        }

        log.info("Password reset SMS would be sent to {} with token {}", user.getUsername(), encryptedKey);
        // TODO: Integrate with an SMS provider (e.g., Twilio) when available.
    }
}

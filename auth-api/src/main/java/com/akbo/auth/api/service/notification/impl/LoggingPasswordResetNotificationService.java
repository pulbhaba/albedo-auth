package com.akbo.auth.api.service.notification.impl;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.stereotype.Service;

import com.akbo.auth.api.service.notification.PasswordResetNotificationService;
import com.akbo.auth.dao.entity.User;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@ConditionalOnMissingBean(PasswordResetNotificationService.class)
public class LoggingPasswordResetNotificationService implements PasswordResetNotificationService {

    @Override
    public void notify(final User user, final String encryptedKey) {
        final String username = user != null ? user.getUsername() : "<unknown>";
        log.info("Password reset token for user {} would be delivered via email/SMS: {}", username, encryptedKey);
    }
}

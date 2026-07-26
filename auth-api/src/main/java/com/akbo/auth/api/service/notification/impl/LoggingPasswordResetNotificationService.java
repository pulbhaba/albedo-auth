package com.akbo.auth.api.service.notification.impl;

import com.akbo.auth.api.service.notification.PasswordResetNotificationService;
import com.akbo.auth.dao.entity.User;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class LoggingPasswordResetNotificationService implements PasswordResetNotificationService {

    @Override
    public void notify(final User user, final String encryptedKey) {
        final String username = user != null ? user.getUsername() : "<unknown>";
        log.info("Password reset token for user {} would be delivered via email/SMS: {}", username, encryptedKey);
    }

    @Override
    public NotificationType getType() {
        return null; // Used as a fallback for both
    }
}

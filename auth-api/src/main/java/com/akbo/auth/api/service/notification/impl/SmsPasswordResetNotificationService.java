package com.akbo.auth.api.service.notification.impl;

import com.akbo.auth.api.service.notification.PasswordResetNotificationService;
import com.akbo.auth.dao.entity.User;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@ConditionalOnProperty(prefix = "notification.sms.sns", name = "enabled", havingValue = "true")
public class SmsPasswordResetNotificationService implements PasswordResetNotificationService {

    @Override
    public void notify(final User user, final String encryptedKey) {
        if (user == null) {
            log.info("Skipping password reset SMS because the requested username does not exist.");
            return;
        }

        log.info(
                "Password reset SMS would be sent to {} with token {}",
                user.getUsername(),
                encryptedKey);
        // This class will be replaced/moved or used for a different SMS provider if needed.
    }

    @Override
    public NotificationType getType() {
        return NotificationType.SMS;
    }
}

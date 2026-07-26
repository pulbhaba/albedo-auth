package com.akbo.auth.api.service.notification.impl;

import com.akbo.auth.api.service.notification.PasswordResetNotificationService;
import com.akbo.auth.dao.entity.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Primary
@Service
@RequiredArgsConstructor
public class CompositePasswordResetNotificationService implements PasswordResetNotificationService {

    private final List<PasswordResetNotificationService> notificationServices;
    private final LoggingPasswordResetNotificationService loggingService;

    @Value("${notification.active-provider:logging}")
    private String activeProvider;

    @Override
    public void notify(final User user, final String encryptedKey) {
        if ("logging".equalsIgnoreCase(activeProvider)) {
            loggingService.notify(user, encryptedKey);
            return;
        }

        NotificationType targetType = null;
        if ("email".equalsIgnoreCase(activeProvider)) {
            targetType = NotificationType.EMAIL;
        } else if ("sms".equalsIgnoreCase(activeProvider)) {
            targetType = NotificationType.SMS;
        }

        if (targetType == null) {
            log.warn("Unknown active provider: {}. Falling back to logging.", activeProvider);
            loggingService.notify(user, encryptedKey);
            return;
        }

        final NotificationType finalTargetType = targetType;
        List<PasswordResetNotificationService> candidates =
                notificationServices.stream().filter(s -> s.getType() == finalTargetType).toList();

        if (candidates.isEmpty()) {
            log.warn(
                    "No active notification providers found for type {}, falling back to logging.",
                    finalTargetType);
            loggingService.notify(user, encryptedKey);
            return;
        }

        // We only use the first matching provider as per requirement "single provider to active"
        PasswordResetNotificationService service = candidates.get(0);
        try {
            service.notify(user, encryptedKey);
        } catch (Exception e) {
            log.error("Failed to send notification via {}", service.getClass().getSimpleName(), e);
        }
    }

    @Override
    public NotificationType getType() {
        return null; // Composite
    }
}

package com.akbo.auth.api.service.notification;

import com.akbo.auth.dao.entity.User;

/** Sends password-reset notifications to end users once a reset token is generated. */
public interface PasswordResetNotificationService {

    /**
     * Deliver a password reset notification containing the encrypted request key.
     *
     * @param user the target user; may be {@code null} when the username is unknown.
     * @param encryptedKey the encrypted token that allows the user to complete the reset flow.
     */
    void notify(User user, String encryptedKey);

    /**
     * @return the type of notification (e.g., EMAIL, SMS)
     */
    NotificationType getType();

    enum NotificationType {
        EMAIL,
        SMS
    }
}

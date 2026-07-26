package com.akbo.auth.api.service.notification.impl;

import com.akbo.auth.dao.entity.User;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class LoggingPasswordResetNotificationServiceTest {
    @Test
    void testNotify() {
        LoggingPasswordResetNotificationService service = new LoggingPasswordResetNotificationService();
        User user = new User();
        user.setUsername("testuser");
        service.notify(user, "token");
    }

    @Test
    void testNotify_NullUser() {
        LoggingPasswordResetNotificationService service = new LoggingPasswordResetNotificationService();
        service.notify(null, "token");
    }
}

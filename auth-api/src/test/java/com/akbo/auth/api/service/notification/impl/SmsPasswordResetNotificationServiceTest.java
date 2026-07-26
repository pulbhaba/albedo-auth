package com.akbo.auth.api.service.notification.impl;

import com.akbo.auth.dao.entity.User;
import org.junit.jupiter.api.Test;

class SmsPasswordResetNotificationServiceTest {
    @Test
    void testNotify() {
        SmsPasswordResetNotificationService service = new SmsPasswordResetNotificationService();
        User user = new User();
        user.setUsername("testuser");
        service.notify(user, "token");
    }

    @Test
    void testNotify_NullUser() {
        SmsPasswordResetNotificationService service = new SmsPasswordResetNotificationService();
        service.notify(null, "token");
    }
}

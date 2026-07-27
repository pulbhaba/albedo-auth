package com.akbo.auth.api.service.notification.impl;

import com.akbo.auth.dao.entity.User;
import org.junit.jupiter.api.Test;

class SmsPasswordResetNotificationServiceTest {
    @Test
    void testNotify() {
        // SmsPasswordResetNotificationService is now conditionally enabled for SNS
        // This test is mostly a placeholder now as the implementation was changed.
        SmsPasswordResetNotificationService service = new SmsPasswordResetNotificationService();
        User user = new User();
        user.setUsername("testuser");
        user.setPhoneNumber("+123456789");
        service.notify(user, "token");
    }

    @Test
    void testNotify_NullUser() {
        SmsPasswordResetNotificationService service = new SmsPasswordResetNotificationService();
        service.notify(null, "token");
    }
}

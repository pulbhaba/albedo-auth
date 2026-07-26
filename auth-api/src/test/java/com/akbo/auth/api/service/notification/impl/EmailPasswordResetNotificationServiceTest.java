package com.akbo.auth.api.service.notification.impl;

import com.akbo.auth.dao.entity.User;
import org.junit.jupiter.api.Test;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.test.util.ReflectionTestUtils;
import org.mockito.ArgumentCaptor;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

class EmailPasswordResetNotificationServiceTest {

    @Test
    void testNotify() {
        JavaMailSender mailSender = mock(JavaMailSender.class);
        EmailPasswordResetNotificationService service = new EmailPasswordResetNotificationService(mailSender);
        
        ReflectionTestUtils.setField(service, "fromAddress", "from@example.com");
        ReflectionTestUtils.setField(service, "subject", "Subject");
        ReflectionTestUtils.setField(service, "frontendUrl", "http://frontend");
        ReflectionTestUtils.setField(service, "bodyTemplate", "%s %s");

        User user = new User();
        user.setUsername("testuser");
        user.setEmailAddress("user@example.com");
        
        service.notify(user, "token123");
        
        ArgumentCaptor<SimpleMailMessage> messageCaptor = ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(mailSender).send(messageCaptor.capture());
        SimpleMailMessage sentMessage = messageCaptor.getValue();
        assertTrue(sentMessage.getText().contains("http://frontend/password-reset/token123"));
    }

    @Test
    void testNotify_NoEmail() {
        JavaMailSender mailSender = mock(JavaMailSender.class);
        EmailPasswordResetNotificationService service = new EmailPasswordResetNotificationService(mailSender);
        
        User user = new User();
        user.setUsername("testuser");
        user.setEmailAddress("");
        
        service.notify(user, "token123");
        
        verify(mailSender, never()).send(any(SimpleMailMessage.class));
    }
    @Test
    void testNotify_NullUser() {
        JavaMailSender mailSender = mock(JavaMailSender.class);
        EmailPasswordResetNotificationService service = new EmailPasswordResetNotificationService(mailSender);
        
        service.notify(null, "token123");
        
        verify(mailSender, never()).send(any(SimpleMailMessage.class));
    }
}

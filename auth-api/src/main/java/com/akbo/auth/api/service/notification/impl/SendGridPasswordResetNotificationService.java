package com.akbo.auth.api.service.notification.impl;

import static java.util.Objects.isNull;
import com.akbo.auth.api.service.notification.PasswordResetNotificationService;
import com.akbo.auth.dao.entity.User;
import com.sendgrid.Method;
import com.sendgrid.Request;
import com.sendgrid.Response;
import com.sendgrid.SendGrid;
import com.sendgrid.helpers.mail.Mail;
import com.sendgrid.helpers.mail.objects.Content;
import com.sendgrid.helpers.mail.objects.Email;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.AllNestedConditions;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Conditional;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Slf4j
@Service
@RequiredArgsConstructor
@Conditional(SendGridPasswordResetNotificationService.SendGridCondition.class)
public class SendGridPasswordResetNotificationService implements PasswordResetNotificationService {

    static class SendGridCondition extends AllNestedConditions {
        SendGridCondition() {
            super(ConfigurationPhase.REGISTER_BEAN);
        }

        @ConditionalOnProperty(name = "notification.active-provider", havingValue = "email")
        static class ActiveProviderEmail {}

        @ConditionalOnProperty(
                prefix = "notification.email",
                name = "provider",
                havingValue = "sendgrid")
        static class EmailProviderSendGrid {}
    }

    @Value("${notification.email.sendgrid.api-key}")
    private String apiKey;

    @Value("${notification.email.from-address}")
    private String fromAddress;

    @Value("${notification.email.subject}")
    private String subject;

    @Value("${notification.frontend-url}")
    private String frontendUrl;

    @Value("${notification.email.body-template}")
    private String bodyTemplate;

    @Override
    public void notify(final User user, final String encryptedKey) {
        if (isNull(user)) {
            log.info("Skipping SendGrid email because user is null.");
            return;
        }
        if (isNull(user.getEmailAddress()) || user.getEmailAddress().isBlank()) {
            log.info(
                    "Skipping SendGrid email because user {} does not have an email address.",
                    user.getUsername());
            return;
        }

        final String resetLink = frontendUrl + "/password-reset/" + encryptedKey;
        final String recipientName =
                (user.getFirstName() != null && !user.getFirstName().isBlank())
                        ? user.getFirstName()
                        : user.getUsername();
        final String body = String.format(bodyTemplate, recipientName, resetLink);

        Email from = new Email(fromAddress);
        Email to = new Email(user.getEmailAddress());
        Content content = new Content("text/plain", body);
        Mail mail = new Mail(from, subject, to, content);

        SendGrid sg = new SendGrid(apiKey);
        Request request = new Request();
        try {
            request.setMethod(Method.POST);
            request.setEndpoint("mail/send");
            request.setBody(mail.build());
            Response response = sg.api(request);
            log.info("SendGrid response status code: {}", response.getStatusCode());
        } catch (IOException ex) {
            log.error("Failed to send email via SendGrid", ex);
        }
    }

    @Override
    public NotificationType getType() {
        return NotificationType.EMAIL;
    }
}

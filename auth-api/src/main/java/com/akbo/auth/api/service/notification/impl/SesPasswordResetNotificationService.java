package com.akbo.auth.api.service.notification.impl;

import static java.util.Objects.isNull;
import com.akbo.auth.api.service.notification.PasswordResetNotificationService;
import com.akbo.auth.dao.entity.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.AllNestedConditions;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Conditional;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.ses.SesClient;
import software.amazon.awssdk.services.ses.model.*;

@Slf4j
@Service
@RequiredArgsConstructor
@Conditional(SesPasswordResetNotificationService.SesCondition.class)
public class SesPasswordResetNotificationService implements PasswordResetNotificationService {

    static class SesCondition extends AllNestedConditions {
        SesCondition() {
            super(ConfigurationPhase.REGISTER_BEAN);
        }

        @ConditionalOnProperty(name = "notification.active-provider", havingValue = "email")
        static class ActiveProviderEmail {}

        @ConditionalOnProperty(
                prefix = "notification.email",
                name = "provider",
                havingValue = "ses")
        static class EmailProviderSes {}
    }

    @Value("${notification.aws.region}")
    private String awsRegion;

    @Value("${notification.aws.access-key}")
    private String accessKey;

    @Value("${notification.aws.secret-key}")
    private String secretKey;

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
        if (isNull(user) || isNull(user.getEmailAddress()) || user.getEmailAddress().isBlank()) {
            log.info("Skipping SES email because user or email is missing.");
            return;
        }

        final String resetLink = frontendUrl + "/password-reset/" + encryptedKey;
        final String recipientName =
                (user.getFirstName() != null && !user.getFirstName().isBlank())
                        ? user.getFirstName()
                        : user.getUsername();
        final String body = String.format(bodyTemplate, recipientName, resetLink);

        try (SesClient client =
                SesClient.builder()
                        .region(Region.of(awsRegion))
                        .credentialsProvider(
                                StaticCredentialsProvider.create(
                                        AwsBasicCredentials.create(accessKey, secretKey)))
                        .build()) {

            SendEmailRequest request =
                    SendEmailRequest.builder()
                            .destination(
                                    Destination.builder()
                                            .toAddresses(user.getEmailAddress())
                                            .build())
                            .message(
                                    Message.builder()
                                            .subject(Content.builder().data(subject).build())
                                            .body(
                                                    Body.builder()
                                                            .text(
                                                                    Content.builder()
                                                                            .data(body)
                                                                            .build())
                                                            .build())
                                            .build())
                            .source(fromAddress)
                            .build();

            client.sendEmail(request);
            log.info("Password reset email sent via AWS SES to {}", user.getEmailAddress());
        } catch (SesException e) {
            log.error("Failed to send email via AWS SES", e);
        }
    }

    @Override
    public NotificationType getType() {
        return NotificationType.EMAIL;
    }
}

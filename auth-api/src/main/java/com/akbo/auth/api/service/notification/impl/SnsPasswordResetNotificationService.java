package com.akbo.auth.api.service.notification.impl;

import com.akbo.auth.api.service.notification.PasswordResetNotificationService;
import com.akbo.auth.dao.entity.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.sns.SnsClient;
import software.amazon.awssdk.services.sns.model.PublishRequest;
import software.amazon.awssdk.services.sns.model.SnsException;

import static java.util.Objects.isNull;

@Slf4j
@Service
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "notification.sms", name = "provider", havingValue = "sns")
public class SnsPasswordResetNotificationService implements PasswordResetNotificationService {

    @Value("${notification.aws.region}")
    private String awsRegion;

    @Value("${notification.aws.access-key}")
    private String accessKey;

    @Value("${notification.aws.secret-key}")
    private String secretKey;

    @Value("${notification.frontend-url}")
    private String frontendUrl;

    @Override
    public void notify(final User user, final String encryptedKey) {
        // Assume user has a phone number field or use username as a fallback if it looks like a phone number
        // For this implementation, we'll check if we have a way to get the phone number.
        // The current User entity has phoneNumber field now.
        String phoneNumber = user.getPhoneNumber(); 

        if (isNull(user) || isNull(phoneNumber) || phoneNumber.isBlank()) {
            log.info("Skipping SNS SMS because user or phone number is missing.");
            return;
        }

        final String resetLink = frontendUrl + "/password-reset/" + encryptedKey;
        final String message = "Reset your password: " + resetLink;

        try (SnsClient client = SnsClient.builder()
                .region(Region.of(awsRegion))
                .credentialsProvider(StaticCredentialsProvider.create(AwsBasicCredentials.create(accessKey, secretKey)))
                .build()) {

            PublishRequest request = PublishRequest.builder()
                    .message(message)
                    .phoneNumber(phoneNumber)
                    .build();

            client.publish(request);
            log.info("Password reset SMS sent via AWS SNS to {}", phoneNumber);
        } catch (SnsException e) {
            log.error("Failed to send SMS via AWS SNS", e);
        }
    }

    @Override
    public NotificationType getType() {
        return NotificationType.SMS;
    }
}

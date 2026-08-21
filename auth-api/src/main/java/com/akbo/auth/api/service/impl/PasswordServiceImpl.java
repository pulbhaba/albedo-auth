package com.akbo.auth.api.service.impl;

import com.akbo.auth.api.service.PasswordService;
import com.akbo.auth.api.service.notification.PasswordResetNotificationService;
import com.akbo.auth.dao.entity.PasswordChangeRequest;
import com.akbo.auth.dao.repository.PasswordResetRequestRepository;
import com.akbo.auth.dao.repository.UserRepository;
import com.akbo.auth.dto.PasswordChangeDto;
import com.akbo.auth.exception.UnauthorizedException;
import com.akbo.auth.util.PasswordPolicy;
import com.akbo.auth.util.PasswordTools;
import lombok.RequiredArgsConstructor;
import lombok.extern.java.Log;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Map;

import javax.crypto.SecretKey;

@Log
@Service
@RequiredArgsConstructor
public class PasswordServiceImpl implements PasswordService {

    private final PasswordResetRequestRepository passwordResetRequestRepository;
    private final UserRepository userRepository;
    private final ModelMapper modelMapper;
    private final PasswordEncoder passwordEncoder;
    private final SecretKey symKey;
    private final PasswordResetNotificationService passwordResetNotificationService;

    @Value("${app.auth.password-reset-token-ttl:PT15M}")
    private String tokenTtl;

    @Override
    public Map<String, String> changePassword(final PasswordChangeDto request) {
        PasswordPolicy.validate(request.getNewPassword());
        final String requestIdWithStr =
                PasswordTools.decrypt(PasswordTools.urlAlgorithm, request.getRequestKey(), symKey);
        final String[] idAndString = requestIdWithStr.split("\\|");
        final var requestId = Long.valueOf(idAndString[0]);
        final var randomString = idAndString[1];
        return passwordResetRequestRepository
                .findOneByIdAndRandomStringNotExpired(requestId, randomString)
                .map(
                        resetRequest -> {
                            if (isExpired(resetRequest)) {
                                throw new UnauthorizedException(
                                        "Your request to change password has expired.");
                            }
                            final var user = resetRequest.getUser();
                            user.setPassword(passwordEncoder.encode(request.getNewPassword()));
                            userRepository.save(user);
                            resetRequest.setPasswordChanged(true);
                            passwordResetRequestRepository.save(resetRequest);
                            return Map.of("message", "Password successfully changed.");
                        })
                .orElseThrow(
                        () ->
                                new UnauthorizedException(
                                        "Your request to change password is invalid."));
    }

    private boolean isExpired(PasswordChangeRequest resetRequest) {
        if (Boolean.TRUE.equals(resetRequest.getExpired())) {
            return true;
        }
        LocalDateTime expiryTime = resetRequest.getCreatedTime().plus(Duration.parse(tokenTtl));
        return LocalDateTime.now().isAfter(expiryTime);
    }

    @Override
    public void RequestPasswordChange(final String username) {
        final var request = new PasswordChangeRequest();
        final var userOptional = userRepository.findByUsername(username);

        // Always generate a random string and "encrypt" it to maintain consistent timing
        // even if the user doesn't exist.
        String randomString = PasswordTools.generateRandomString();
        request.setRandomString(randomString);

        if (userOptional.isPresent()) {
            request.setUser(userOptional.get());
            final var savedRequest = passwordResetRequestRepository.save(request);
            final var idAndString =
                    String.join(
                            "|", savedRequest.getId().toString(), savedRequest.getRandomString());
            final var encryptedKey =
                    PasswordTools.encrypt(PasswordTools.urlAlgorithm, idAndString, symKey);

            passwordResetNotificationService.notify(userOptional.get(), encryptedKey);
        } else {
            // Log for debugging but don't reveal to the user
            log.info("Password reset requested for non-existent user: " + username);
            // Simulate work to prevent timing attacks
            PasswordTools.encrypt(PasswordTools.urlAlgorithm, "0|" + randomString, symKey);
            passwordResetNotificationService.notify(null, "dummy-key");
        }
    }
}

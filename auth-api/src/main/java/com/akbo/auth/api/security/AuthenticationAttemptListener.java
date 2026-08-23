package com.akbo.auth.api.security;

import com.akbo.auth.api.config.AccountLockoutProperties;
import com.akbo.auth.dao.entity.User;
import com.akbo.auth.dao.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.security.authentication.event.AuthenticationFailureBadCredentialsEvent;
import org.springframework.security.authentication.event.AuthenticationSuccessEvent;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class AuthenticationAttemptListener {

    private final UserRepository userRepository;
    private final AccountLockoutProperties properties;
    private final Clock clock;

    @EventListener
    @Transactional
    public void onAuthenticationFailure(AuthenticationFailureBadCredentialsEvent event) {
        String username = event.getAuthentication().getName();
        userRepository.findByUsernameForUpdate(username).ifPresent(this::recordFailure);
    }

    @EventListener
    @Transactional
    public void onAuthenticationSuccess(AuthenticationSuccessEvent event) {
        userRepository
                .findByUsernameForUpdate(event.getAuthentication().getName())
                .ifPresent(
                        user -> {
                            user.setFailedLoginAttempts(0);
                            user.setAccountNonLocked(true);
                            user.setLockedUntil(null);
                            userRepository.save(user);
                        });
    }

    private void recordFailure(User user) {
        LocalDateTime now = LocalDateTime.now(clock);
        if (user.getLockedUntil() != null && !now.isBefore(user.getLockedUntil())) {
            user.setFailedLoginAttempts(0);
            user.setAccountNonLocked(true);
            user.setLockedUntil(null);
        }

        int failedAttempts = user.getFailedLoginAttempts() + 1;
        user.setFailedLoginAttempts(failedAttempts);
        if (failedAttempts >= properties.getMaxFailedAttempts()) {
            user.setAccountNonLocked(false);
            user.setLockedUntil(now.plus(properties.getDuration()));
        }
        userRepository.save(user);
    }
}

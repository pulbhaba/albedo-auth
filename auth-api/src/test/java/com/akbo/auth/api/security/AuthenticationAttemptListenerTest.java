package com.akbo.auth.api.security;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.akbo.auth.api.config.AccountLockoutProperties;
import com.akbo.auth.dao.entity.User;
import com.akbo.auth.dao.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authentication.event.AuthenticationFailureBadCredentialsEvent;
import org.springframework.security.authentication.event.AuthenticationSuccessEvent;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Optional;

@ExtendWith(MockitoExtension.class)
class AuthenticationAttemptListenerTest {

    private static final LocalDateTime NOW = LocalDateTime.of(2026, 8, 23, 12, 0);

    @Mock private UserRepository userRepository;

    private User user;
    private AuthenticationAttemptListener listener;
    private UsernamePasswordAuthenticationToken authentication;

    @BeforeEach
    void setUp() {
        AccountLockoutProperties properties = new AccountLockoutProperties();
        properties.setMaxFailedAttempts(3);
        properties.setDuration(java.time.Duration.ofMinutes(15));
        listener =
                new AuthenticationAttemptListener(
                        userRepository,
                        properties,
                        Clock.fixed(Instant.parse("2026-08-23T12:00:00Z"), ZoneOffset.UTC));
        user = new User();
        user.setUsername("alice");
        authentication = UsernamePasswordAuthenticationToken.unauthenticated("alice", "wrong");
        when(userRepository.findByUsernameForUpdate("alice")).thenReturn(Optional.of(user));
    }

    @Test
    void locksAccountAtConfiguredFailureThreshold() {
        listener.onAuthenticationFailure(
                new AuthenticationFailureBadCredentialsEvent(
                        authentication, new BadCredentialsException("bad credentials")));
        listener.onAuthenticationFailure(
                new AuthenticationFailureBadCredentialsEvent(
                        authentication, new BadCredentialsException("bad credentials")));
        listener.onAuthenticationFailure(
                new AuthenticationFailureBadCredentialsEvent(
                        authentication, new BadCredentialsException("bad credentials")));

        assertEquals(3, user.getFailedLoginAttempts());
        assertFalse(user.getAccountNonLocked());
        assertEquals(NOW.plusMinutes(15), user.getLockedUntil());
        verify(userRepository, org.mockito.Mockito.times(3)).save(user);
    }

    @Test
    void successfulLoginResetsFailuresAndUnlocksAccount() {
        user.setFailedLoginAttempts(3);
        user.setAccountNonLocked(false);
        user.setLockedUntil(NOW.plusMinutes(15));

        listener.onAuthenticationSuccess(new AuthenticationSuccessEvent(authentication));

        assertEquals(0, user.getFailedLoginAttempts());
        assertTrue(user.isAccountNonLocked());
        assertNull(user.getLockedUntil());
        verify(userRepository).save(user);
    }

    @Test
    void expiredLockoutStartsFreshFailureWindow() {
        user.setFailedLoginAttempts(3);
        user.setAccountNonLocked(false);
        user.setLockedUntil(NOW.minusMinutes(1));

        assertTrue(user.isAccountNonLocked());

        listener.onAuthenticationFailure(
                new AuthenticationFailureBadCredentialsEvent(
                        authentication, new BadCredentialsException("bad credentials")));

        assertEquals(1, user.getFailedLoginAttempts());
        assertTrue(user.isAccountNonLocked());
        assertNull(user.getLockedUntil());
    }
}

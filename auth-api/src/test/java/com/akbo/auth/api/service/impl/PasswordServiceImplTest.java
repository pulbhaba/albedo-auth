package com.akbo.auth.api.service.impl;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import com.akbo.auth.api.service.notification.PasswordResetNotificationService;
import com.akbo.auth.dao.entity.PasswordChangeRequest;
import com.akbo.auth.dao.entity.User;
import com.akbo.auth.dao.repository.PasswordResetRequestRepository;
import com.akbo.auth.dao.repository.UserRepository;
import com.akbo.auth.dto.PasswordChangeDto;
import com.akbo.auth.exception.BadRequestException;
import com.akbo.auth.exception.UnauthorizedException;
import com.akbo.auth.util.PasswordTools;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;

import javax.crypto.SecretKey;

@ExtendWith(MockitoExtension.class)
class PasswordServiceImplTest {

    @Mock private PasswordResetRequestRepository passwordResetRequestRepository;
    @Mock private UserRepository userRepository;
    @Mock private ModelMapper modelMapper;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private PasswordResetNotificationService passwordResetNotificationService;

    private SecretKey symKey;

    private PasswordServiceImpl passwordService;

    @BeforeEach
    void setUp() {
        symKey = PasswordTools.getKeyFromPassword("test-password", "test-salt");
        passwordService =
                new PasswordServiceImpl(
                        passwordResetRequestRepository,
                        userRepository,
                        modelMapper,
                        passwordEncoder,
                        symKey,
                        passwordResetNotificationService);
        org.springframework.test.util.ReflectionTestUtils.setField(
                passwordService, "tokenTtl", "PT15M");
    }

    @Test
    void requestPasswordChange_userExists() {
        User user = new User();
        user.setUsername("testuser");
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));

        PasswordChangeRequest request = new PasswordChangeRequest();
        request.setId(100L);
        request.setRandomString("RANDOM123456789012");
        when(passwordResetRequestRepository.save(any(PasswordChangeRequest.class)))
                .thenReturn(request);

        passwordService.RequestPasswordChange("testuser");

        verify(passwordResetRequestRepository).save(any(PasswordChangeRequest.class));
        verify(passwordResetNotificationService).notify(eq(user), anyString());
    }

    @Test
    void changePassword_success() {
        PasswordChangeDto requestDto = new PasswordChangeDto();
        requestDto.setNewPassword("NewPassword1!");

        String randomString = "RANDOM123456789012";
        Long requestId = 100L;
        String rawKey = requestId + "|" + randomString;
        String encryptedKey = PasswordTools.encrypt(PasswordTools.urlAlgorithm, rawKey, symKey);
        requestDto.setRequestKey(encryptedKey);

        User user = new User();
        user.setUsername("testuser");

        PasswordChangeRequest resetRequest = new PasswordChangeRequest();
        resetRequest.setUser(user);
        resetRequest.setCreatedTime(LocalDateTime.now());

        when(passwordResetRequestRepository.findOneByIdAndRandomStringNotExpired(
                        requestId, randomString))
                .thenReturn(Optional.of(resetRequest));
        when(passwordEncoder.encode("NewPassword1!")).thenReturn("encodedNewPass");
        when(userRepository.save(user)).thenReturn(user);

        Map<String, String> result = passwordService.changePassword(requestDto);

        assertNotNull(result);
        assertEquals("Password successfully changed.", result.get("message"));
        assertTrue(resetRequest.getPasswordChanged());
        verify(userRepository).save(user);
        verify(passwordResetRequestRepository).save(resetRequest);
    }

    @Test
    void changePassword_expired() {
        PasswordChangeDto requestDto = new PasswordChangeDto();
        requestDto.setNewPassword("NewPassword1!");

        String randomString = "RANDOM123456789012";
        Long requestId = 100L;
        String rawKey = requestId + "|" + randomString;
        String encryptedKey = PasswordTools.encrypt(PasswordTools.urlAlgorithm, rawKey, symKey);
        requestDto.setRequestKey(encryptedKey);

        PasswordChangeRequest resetRequest = new PasswordChangeRequest();
        resetRequest.setCreatedTime(LocalDateTime.now().minusMinutes(30));

        when(passwordResetRequestRepository.findOneByIdAndRandomStringNotExpired(
                        requestId, randomString))
                .thenReturn(Optional.of(resetRequest));

        assertThrows(UnauthorizedException.class, () -> passwordService.changePassword(requestDto));
    }

    @Test
    void changePassword_invalidRequest() {
        PasswordChangeDto requestDto = new PasswordChangeDto();
        requestDto.setNewPassword("NewPassword1!");
        requestDto.setRequestKey(
                PasswordTools.encrypt(PasswordTools.urlAlgorithm, "1|wrong", symKey));

        when(passwordResetRequestRepository.findOneByIdAndRandomStringNotExpired(1L, "wrong"))
                .thenReturn(Optional.empty());

        assertThrows(UnauthorizedException.class, () -> passwordService.changePassword(requestDto));
    }

    @Test
    void changePassword_weakPassword_isRejectedBeforeTokenLookup() {
        PasswordChangeDto requestDto = new PasswordChangeDto();
        requestDto.setNewPassword("weak");

        assertThrows(BadRequestException.class, () -> passwordService.changePassword(requestDto));
        verifyNoInteractions(passwordResetRequestRepository, userRepository, passwordEncoder);
    }
}

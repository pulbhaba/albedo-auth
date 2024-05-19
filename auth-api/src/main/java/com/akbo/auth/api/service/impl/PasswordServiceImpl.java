package com.akbo.auth.api.service.impl;

import com.akbo.auth.dto.PasswordChangeDto;
import com.akbo.auth.dto.UserDto;
import com.akbo.auth.exception.UnauthorizedException;
import com.akbo.auth.util.PasswordTools;

import javax.crypto.SecretKey;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.actuate.autoconfigure.metrics.MetricsProperties.System;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.akbo.auth.api.service.*;
import com.akbo.auth.dao.entity.PasswordChangeRequest;
import com.akbo.auth.dao.repository.PasswordResetRequestRepository;
import com.akbo.auth.dao.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.java.Log;

@Log
@Service
@RequiredArgsConstructor
public class PasswordServiceImpl implements PasswordService {



    private final PasswordResetRequestRepository passwordResetRequestRepository;
    private final UserRepository userRepository;
    private final ModelMapper modelMapper;
    private final PasswordEncoder passwordEncoder;
    private final SecretKey symKey;

    @Override
    public UserDto changePassword(final PasswordChangeDto request) {
        final String requestIdWithStr = PasswordTools.decrypt(PasswordTools.urlAlgorithm, request.getRequestKey(), symKey);
        final String[] idAndString = requestIdWithStr.split("\\|");
        final var requestId = Long.valueOf(idAndString[0]);
        final var randomString = idAndString[1];
        return passwordResetRequestRepository.findOneByIdAndRandomStringNotExpired(requestId, randomString)
                .map(resetRequest -> {
                    final var user = resetRequest.getUser();
                    user.setPassword(passwordEncoder.encode(request.getNewPassword()));
                    final var savedUser = userRepository.save(user);
                    resetRequest.setPasswordChanged(true);
                    passwordResetRequestRepository.save(resetRequest);
                    return modelMapper.map(savedUser, UserDto.class);
                }).orElseThrow(() -> new UnauthorizedException("Your request to change password is invalid."));
    }

    @Override
    public void RequestPasswordChange(String username) {
        final var request = new PasswordChangeRequest();
        userRepository.findByUsername(username)
                .ifPresent(user -> request.setUser(user));
        request.setRandomString(PasswordTools.generateRandomString());

        final var savedRequest = passwordResetRequestRepository.save(request);
        final var idAndString = String.join("|", savedRequest.getId().toString(), savedRequest.getRandomString());
        final var encryptedKey = PasswordTools.encrypt(PasswordTools.urlAlgorithm, idAndString, symKey);
        // Remove this line for production
        log.info("The encrypted key for password reset is: " + encryptedKey);
    }
}

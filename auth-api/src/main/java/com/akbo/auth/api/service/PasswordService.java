package com.akbo.auth.api.service;

import com.akbo.auth.dto.PasswordChangeDto;

import java.util.Map;

public interface PasswordService {

    Map<String, String> changePassword(final PasswordChangeDto request);

    void RequestPasswordChange(final String username);
}

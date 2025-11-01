package com.akbo.auth.api.service;

import com.akbo.auth.dto.PasswordChangeDto;
import com.akbo.auth.dto.UserDto;

public interface PasswordService {

    UserDto changePassword(final PasswordChangeDto request);

    void RequestPasswordChange(final String username);
}

package com.akbo.auth.api.service;

import com.akbo.auth.dto.PasswordChangeDto;
import com.akbo.auth.dto.UserDto;

public interface PasswordService {

    public UserDto changePassword(final PasswordChangeDto request);
    public void RequestPasswordChange(final String username);
}

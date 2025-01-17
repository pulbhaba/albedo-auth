package com.akbo.auth.api.service;

import org.apache.coyote.BadRequestException;
import org.springframework.security.provisioning.UserDetailsManager;

import com.akbo.auth.dto.PasswordChangeDto;
import com.akbo.auth.dto.UserDto;

public interface UserService extends UserDetailsManager{
    public UserDto createUser(final UserDto user);

    public UserDto getUser(final String username);
}

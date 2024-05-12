package com.akbo.auth.api.service;

import org.springframework.security.provisioning.GroupManager;
import org.springframework.security.provisioning.UserDetailsManager;

import com.akbo.auth.dto.UserDto;

public interface UserService extends UserDetailsManager,GroupManager {
    public UserDto createUser(final UserDto user);

    public UserDto getUser(final String username);
}

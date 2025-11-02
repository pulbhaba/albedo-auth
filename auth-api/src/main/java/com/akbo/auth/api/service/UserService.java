package com.akbo.auth.api.service;

import com.akbo.auth.dto.UserDto;
import org.springframework.security.provisioning.UserDetailsManager;

public interface UserService extends UserDetailsManager {
    UserDto createUser(final UserDto user);

    UserDto getUser(final String username);
}

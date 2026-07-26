package com.akbo.auth.api.controller;

import com.akbo.auth.api.service.PasswordService;
import com.akbo.auth.api.service.UserService;
import com.akbo.auth.dto.PasswordChangeDto;
import com.akbo.auth.dto.UserDto;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RequiredArgsConstructor
@RestController
@RequestMapping("public/")
public class PublicController {
    private final PasswordService passwordService;
    private final UserService userService;

    @PostMapping("change-password/")
    public Map<String, String> changePassword(
            @RequestBody final PasswordChangeDto passwordChangeDto) {
        return passwordService.changePassword(passwordChangeDto);
    }

    @GetMapping("user/{username}/reset-password")
    public void requestPasswordReset(@PathVariable("username") final String username) {
        passwordService.RequestPasswordChange(username);
    }

    @PostMapping("user/register")
    public UserDto registerUser(@RequestBody final UserDto userDto) {
        return userService.createUser(userDto);
    }
}

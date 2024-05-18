package com.akbo.auth.api.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.akbo.auth.api.service.PasswordService;
import com.akbo.auth.dto.PasswordChangeDto;
import com.akbo.auth.dto.UserDto;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@RestController
@RequestMapping("public/")
public class PublicController {
	private final PasswordService passwordService;

	@PostMapping("change-password/")
	public UserDto changePassword(@RequestBody final PasswordChangeDto passwordChangeDto) {
		return passwordService.changePassword(passwordChangeDto);
	}

	@GetMapping("user/{username}/reset-password")
	public void requestPasswordReset(@PathVariable("username") String username) {
		passwordService.RequestPasswordChange(username);
	}
}

package com.akbo.auth.api.controller;

import static org.springframework.http.ResponseEntity.ok;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.akbo.auth.api.service.UserService;
import com.akbo.auth.dto.UserDto;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("user/")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("{username}")
    public ResponseEntity<UserDto> getUser(@PathVariable("username") String username) {
        return ok(userService.getUser(username));
    }
}

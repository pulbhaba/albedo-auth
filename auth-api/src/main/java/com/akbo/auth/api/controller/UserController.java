package com.akbo.auth.api.controller;

import static org.springframework.http.ResponseEntity.ok;
import com.akbo.auth.api.service.RoleUpdateRequestService;
import com.akbo.auth.api.service.TokenLogoutService;
import com.akbo.auth.api.service.UserService;
import com.akbo.auth.dto.LogoutRequestDto;
import com.akbo.auth.dto.RoleUpdateRequestCreateDto;
import com.akbo.auth.dto.RoleUpdateRequestDto;
import com.akbo.auth.dto.UserDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("user/")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final TokenLogoutService tokenLogoutService;
    private final RoleUpdateRequestService roleUpdateRequestService;

    @GetMapping("{username}")
    public ResponseEntity<UserDto> getUser(@PathVariable("username") String username) {
        return ok(userService.getUser(username));
    }

    @PostMapping("logout")
    public ResponseEntity<Void> logout(
            @AuthenticationPrincipal final Jwt jwt,
            @RequestBody(required = false) final LogoutRequestDto logoutRequest) {
        tokenLogoutService.logout(
                jwt, logoutRequest == null ? null : logoutRequest.getRefreshToken());
        return ResponseEntity.noContent().build();
    }

    @PostMapping("role-requests")
    public ResponseEntity<RoleUpdateRequestDto> requestRoleUpdate(
            @AuthenticationPrincipal final Jwt jwt,
            @RequestBody final RoleUpdateRequestCreateDto request) {
        return ok(roleUpdateRequestService.createRequest(jwt.getSubject(), request));
    }

    @GetMapping("role-requests/me")
    public ResponseEntity<List<RoleUpdateRequestDto>> listMyRoleUpdateRequests(
            @AuthenticationPrincipal final Jwt jwt) {
        return ok(roleUpdateRequestService.listRequestsForUser(jwt.getSubject()));
    }
}

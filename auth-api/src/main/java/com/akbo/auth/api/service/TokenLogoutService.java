package com.akbo.auth.api.service;

import org.springframework.security.oauth2.jwt.Jwt;

public interface TokenLogoutService {

    void logout(Jwt jwt, String refreshToken);

    boolean isAccessTokenRevoked(Jwt jwt);
}

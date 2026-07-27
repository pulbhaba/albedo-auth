package com.akbo.auth.api.service.impl;

import com.akbo.auth.api.service.TokenLogoutService;
import com.akbo.auth.dao.entity.RevokedJwtToken;
import com.akbo.auth.dao.repository.RevokedJwtTokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.authorization.OAuth2Authorization;
import org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationService;
import org.springframework.security.oauth2.server.authorization.OAuth2TokenType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
@RequiredArgsConstructor
public class TokenLogoutServiceImpl implements TokenLogoutService {

    private final RevokedJwtTokenRepository revokedJwtTokenRepository;
    private final OAuth2AuthorizationService authorizationService;

    @Override
    @Transactional
    public void logout(final Jwt jwt, final String refreshToken) {
        revokeAccessToken(jwt);
        revokeRefreshToken(refreshToken);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isAccessTokenRevoked(final Jwt jwt) {
        if (jwt == null || jwt.getId() == null) {
            return false;
        }

        return revokedJwtTokenRepository.existsByTokenIdAndExpiresAtAfter(
                jwt.getId(), Instant.now());
    }

    private void revokeAccessToken(final Jwt jwt) {
        if (jwt == null || jwt.getId() == null || jwt.getExpiresAt() == null) {
            return;
        }

        revokedJwtTokenRepository.deleteByExpiresAtBefore(Instant.now());
        if (!revokedJwtTokenRepository.existsById(jwt.getId())) {
            revokedJwtTokenRepository.save(new RevokedJwtToken(jwt.getId(), jwt.getExpiresAt()));
        }
    }

    private void revokeRefreshToken(final String refreshToken) {
        if (refreshToken == null || refreshToken.isBlank()) {
            return;
        }

        final OAuth2Authorization authorization =
                authorizationService.findByToken(refreshToken, OAuth2TokenType.REFRESH_TOKEN);
        if (authorization != null) {
            authorizationService.remove(authorization);
        }
    }
}

package com.akbo.auth.api.oauth.password;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.*;
import org.springframework.security.oauth2.core.endpoint.OAuth2ParameterNames;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.authorization.OAuth2Authorization;
import org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationService;
import org.springframework.security.oauth2.server.authorization.authentication.OAuth2AccessTokenAuthenticationToken;
import org.springframework.security.oauth2.server.authorization.authentication.OAuth2ClientAuthenticationToken;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

/**
 * AuthenticationProvider for Resource Owner Password Credentials grant (password).
 */
public class OAuth2PasswordAuthenticationProvider implements org.springframework.security.authentication.AuthenticationProvider {

    private final AuthenticationManager authenticationManager;
    private final OAuth2AuthorizationService authorizationService;
    private final RegisteredClientRepository registeredClientRepository;

    public OAuth2PasswordAuthenticationProvider(AuthenticationManager authenticationManager,
                                                OAuth2AuthorizationService authorizationService,
                                                RegisteredClientRepository registeredClientRepository) {
        this.authenticationManager = authenticationManager;
        this.authorizationService = authorizationService;
        this.registeredClientRepository = registeredClientRepository;
    }

    @Override
    public Authentication authenticate(Authentication authentication) throws OAuth2AuthenticationException {
        if (!(authentication instanceof OAuth2PasswordAuthenticationToken)) {
            return null;
        }
        OAuth2PasswordAuthenticationToken passwordAuth = (OAuth2PasswordAuthenticationToken) authentication;

        // Validate client
        Authentication clientPrincipal = getAuthenticatedClient(passwordAuth);
        RegisteredClient registeredClient = getRegisteredClient(clientPrincipal);
        if (!registeredClient.getAuthorizationGrantTypes().contains(new AuthorizationGrantType("password"))) {
            throw new OAuth2AuthenticationException(new OAuth2Error(OAuth2ErrorCodes.UNAUTHORIZED_CLIENT));
        }

        Map<String, Object> params = passwordAuth.getAdditionalParameters();
        String username = (String) params.get("username");
        String password = (String) params.get("password");
        String requestedScope = (String) params.get(OAuth2ParameterNames.SCOPE);

        if (username == null || password == null) {
            throw new OAuth2AuthenticationException(new OAuth2Error(OAuth2ErrorCodes.INVALID_REQUEST, "Missing username or password", null));
        }

        // Authenticate resource owner
        Authentication userAuth = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(username, password));

        // Determine scopes
        Set<String> authorizedScopes = resolveScopes(registeredClient, requestedScope);

        // Generate simple access token (opaque token for now)
        Instant issuedAt = Instant.now();
        Instant expiresAt = issuedAt.plus(registeredClient.getTokenSettings().getAccessTokenTimeToLive());

        String tokenValue = UUID.randomUUID().toString();
        OAuth2AccessToken accessToken = new OAuth2AccessToken(
                OAuth2AccessToken.TokenType.BEARER,
                tokenValue,
                issuedAt,
                expiresAt,
                authorizedScopes);

        // Optionally create refresh token
        OAuth2RefreshToken refreshToken = null;
        if (registeredClient.getAuthorizationGrantTypes().contains(AuthorizationGrantType.REFRESH_TOKEN)) {
            Instant rtIssuedAt = Instant.now();
            Instant rtExpiresAt = rtIssuedAt.plus(registeredClient.getTokenSettings().getRefreshTokenTimeToLive());
            refreshToken = new OAuth2RefreshToken(UUID.randomUUID().toString(), rtIssuedAt, rtExpiresAt);
        }

        // Build authorization
        OAuth2Authorization.Builder authorizationBuilder = OAuth2Authorization.withRegisteredClient(registeredClient)
                .principalName(userAuth.getName())
                .authorizationGrantType(new AuthorizationGrantType("password"))
                .attribute(OAuth2Authorization.AUTHORIZED_SCOPE_ATTRIBUTE_NAME, authorizedScopes)
                .token(accessToken);
        if (refreshToken != null) {
            authorizationBuilder.refreshToken(refreshToken);
        }

        OAuth2Authorization authorization = authorizationBuilder.build();
        authorizationService.save(authorization);

        Map<String, Object> additionalParameters = Collections.emptyMap();

        return new OAuth2AccessTokenAuthenticationToken(
                registeredClient, clientPrincipal, accessToken,
                refreshToken,
                additionalParameters);
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return OAuth2PasswordAuthenticationToken.class.isAssignableFrom(authentication);
    }

    private Authentication getAuthenticatedClient(OAuth2PasswordAuthenticationToken authentication) {
        Authentication clientPrincipal = authentication.getPrincipal();
        if (!(clientPrincipal instanceof OAuth2ClientAuthenticationToken)) {
            throw new OAuth2AuthenticationException(new OAuth2Error(OAuth2ErrorCodes.INVALID_CLIENT));
        }
        OAuth2ClientAuthenticationToken clientAuth = (OAuth2ClientAuthenticationToken) clientPrincipal;
        if (!clientAuth.isAuthenticated()) {
            throw new OAuth2AuthenticationException(new OAuth2Error(OAuth2ErrorCodes.INVALID_CLIENT));
        }
        return clientAuth;
    }

    private RegisteredClient getRegisteredClient(Authentication clientPrincipal) {
        String clientId = ((OAuth2ClientAuthenticationToken) clientPrincipal).getRegisteredClient().getClientId();
        RegisteredClient registeredClient = registeredClientRepository.findByClientId(clientId);
        if (registeredClient == null) {
            throw new OAuth2AuthenticationException(new OAuth2Error(OAuth2ErrorCodes.INVALID_CLIENT));
        }
        return registeredClient;
    }

    private Set<String> resolveScopes(RegisteredClient registeredClient, String requestedScope) {
        if (requestedScope == null || requestedScope.isBlank()) {
            return registeredClient.getScopes();
        }
        Set<String> requested = Arrays.stream(requestedScope.split(" "))
                .map(String::trim)
                .filter(s -> !s.isBlank())
                .collect(Collectors.toSet());
        if (!registeredClient.getScopes().containsAll(requested)) {
            throw new OAuth2AuthenticationException(new OAuth2Error(OAuth2ErrorCodes.INVALID_SCOPE));
        }
        return requested;
    }

    private Map<String, Object> jwtClaims(Object token) {
        if (token instanceof Jwt) {
            return ((Jwt) token).getClaims();
        }
        return Collections.emptyMap();
    }
}

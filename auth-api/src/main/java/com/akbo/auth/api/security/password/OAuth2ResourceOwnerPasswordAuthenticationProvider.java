package com.akbo.auth.api.security.password;

import static java.util.Collections.emptyMap;

import java.time.Instant;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2ErrorCodes;
import org.springframework.security.oauth2.core.OAuth2RefreshToken;
import org.springframework.security.oauth2.core.endpoint.OAuth2ParameterNames;
import org.springframework.security.oauth2.jose.jws.SignatureAlgorithm;
import org.springframework.security.oauth2.jwt.JoseHeader;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.server.authorization.OAuth2Authorization;
import org.springframework.security.oauth2.server.authorization.OAuth2Authorization.Token;
import org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationService;
import org.springframework.security.oauth2.server.authorization.authentication.OAuth2AccessTokenAuthenticationToken;
import org.springframework.security.oauth2.server.authorization.authentication.OAuth2AuthenticationProviderUtils;
import org.springframework.security.oauth2.server.authorization.authentication.OAuth2ClientAuthenticationToken;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.config.ProviderSettings;
import org.springframework.util.CollectionUtils;

import com.akbo.auth.api.config.AuthorizationServerConfiguration;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class OAuth2ResourceOwnerPasswordAuthenticationProvider
        implements org.springframework.security.authentication.AuthenticationProvider {

    private final AuthenticationManager authenticationManager;
    private final OAuth2AuthorizationService authorizationService;
    private final JwtEncoder jwtEncoder;
    private final ProviderSettings providerSettings;

    @Override
    public Authentication authenticate(final Authentication authentication) {
        final OAuth2ResourceOwnerPasswordAuthenticationToken passwordAuthentication =
                (OAuth2ResourceOwnerPasswordAuthenticationToken) authentication;

        final OAuth2ClientAuthenticationToken clientPrincipal =
                OAuth2AuthenticationProviderUtils.getAuthenticatedClientElseThrowInvalidClient(authentication);
        final RegisteredClient registeredClient = clientPrincipal.getRegisteredClient();
        if (registeredClient == null) {
            throw new OAuth2AuthenticationException(OAuth2ErrorCodes.INVALID_CLIENT);
        }
        if (!registeredClient.getAuthorizationGrantTypes().contains(AuthorizationServerConfiguration.PASSWORD_GRANT_TYPE)) {
            throw new OAuth2AuthenticationException(OAuth2ErrorCodes.UNAUTHORIZED_CLIENT);
        }

        final Authentication userAuthentication = authenticationManager
                .authenticate(passwordAuthentication.getUsernamePasswordAuthentication());

        Set<String> requestedScopes = passwordAuthentication.getScopes();
        if (CollectionUtils.isEmpty(requestedScopes)) {
            requestedScopes = registeredClient.getScopes();
        } else if (!registeredClient.getScopes().containsAll(requestedScopes)) {
            throw new OAuth2AuthenticationException(new OAuth2Error(OAuth2ErrorCodes.INVALID_SCOPE));
        } else {
            requestedScopes = new HashSet<>(requestedScopes);
        }

        final Instant issuedAt = Instant.now();
        final Instant expiresAt = issuedAt.plus(registeredClient.getTokenSettings().getAccessTokenTimeToLive());

        final JoseHeader joseHeader = JoseHeader.withAlgorithm(SignatureAlgorithm.RS256).build();
        final JwtClaimsSet.Builder claims = JwtClaimsSet.builder()
                .subject(userAuthentication.getName())
                .issuedAt(issuedAt)
                .expiresAt(expiresAt)
                .claim(OAuth2ParameterNames.CLIENT_ID, registeredClient.getClientId())
                .claim(OAuth2ParameterNames.SCOPE, requestedScopes);

        if (providerSettings.getIssuer() != null) {
            claims.issuer(providerSettings.getIssuer());
        }

        final Jwt jwt = jwtEncoder.encode(JwtEncoderParameters.from(joseHeader, claims.build()));

        final OAuth2AccessToken accessToken = new OAuth2AccessToken(
                OAuth2AccessToken.TokenType.BEARER,
                jwt.getTokenValue(),
                jwt.getIssuedAt(),
                jwt.getExpiresAt(),
                requestedScopes);

        OAuth2Authorization.Builder authorizationBuilder = OAuth2Authorization.withRegisteredClient(registeredClient)
                .principalName(userAuthentication.getName())
                .authorizationGrantType(AuthorizationServerConfiguration.PASSWORD_GRANT_TYPE)
                .attribute(OAuth2Authorization.AUTHORIZED_SCOPE_ATTRIBUTE_NAME, requestedScopes)
                .token(accessToken, metadata -> metadata.put(Token.CLAIMS_METADATA_NAME, jwt.getClaims()));

        OAuth2RefreshToken refreshToken = null;
        if (shouldIssueRefreshToken(clientPrincipal, registeredClient)) {
            refreshToken = createRefreshToken(registeredClient);
            authorizationBuilder.refreshToken(refreshToken);
        }

        authorizationService.save(authorizationBuilder.build());

        return new OAuth2AccessTokenAuthenticationToken(registeredClient, clientPrincipal, accessToken, refreshToken,
                emptyMap());
    }

    @Override
    public boolean supports(final Class<?> authentication) {
        return OAuth2ResourceOwnerPasswordAuthenticationToken.class.isAssignableFrom(authentication);
    }

    private static boolean shouldIssueRefreshToken(final OAuth2ClientAuthenticationToken clientPrincipal,
            final RegisteredClient registeredClient) {
        return registeredClient.getAuthorizationGrantTypes().contains(AuthorizationGrantType.REFRESH_TOKEN)
                && !ClientAuthenticationMethod.NONE.equals(clientPrincipal.getClientAuthenticationMethod());
    }

    private static OAuth2RefreshToken createRefreshToken(final RegisteredClient registeredClient) {
        final Instant issuedAt = Instant.now();
        final Instant expiresAt = issuedAt.plus(registeredClient.getTokenSettings().getRefreshTokenTimeToLive());
        return new OAuth2RefreshToken(UUID.randomUUID().toString(), issuedAt, expiresAt);
    }
}

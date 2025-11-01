package com.akbo.auth.api.config;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.time.Duration;
import java.time.format.DateTimeParseException;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.security.oauth2.server.authorization.InMemoryOAuth2AuthorizationConsentService;
import org.springframework.security.oauth2.server.authorization.InMemoryOAuth2AuthorizationService;
import org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationConsentService;
import org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationService;
import org.springframework.security.oauth2.server.authorization.client.InMemoryRegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.config.ProviderSettings;
import org.springframework.security.oauth2.server.authorization.config.TokenSettings;
import org.springframework.security.oauth2.server.authorization.config.annotation.web.configuration.OAuth2AuthorizationServerConfiguration;
import org.springframework.security.oauth2.server.authorization.config.annotation.web.configurers.OAuth2AuthorizationServerConfigurer;
import org.springframework.security.oauth2.server.authorization.settings.ClientSettings;
import org.springframework.security.web.SecurityFilterChain;

import com.akbo.auth.api.security.password.OAuth2ResourceOwnerPasswordAuthenticationConverter;
import com.akbo.auth.api.security.password.OAuth2ResourceOwnerPasswordAuthenticationProvider;

import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.source.ImmutableJWKSet;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.SecurityContext;

@Configuration
public class AuthorizationServerConfiguration {

    public static final AuthorizationGrantType PASSWORD_GRANT_TYPE = new AuthorizationGrantType("password");

    @Bean
    @Order(Ordered.HIGHEST_PRECEDENCE)
    public SecurityFilterChain authorizationServerSecurityFilterChain(final HttpSecurity http,
            final OAuth2ResourceOwnerPasswordAuthenticationConverter passwordAuthenticationConverter,
            final OAuth2ResourceOwnerPasswordAuthenticationProvider passwordAuthenticationProvider) throws Exception {

        OAuth2AuthorizationServerConfiguration.applyDefaultSecurity(http);

        http.getConfigurer(OAuth2AuthorizationServerConfigurer.class)
                .tokenEndpoint(tokenEndpoint -> tokenEndpoint
                        .accessTokenRequestConverter(passwordAuthenticationConverter)
                        .authenticationProvider(passwordAuthenticationProvider))
                .oidc(Customizer.withDefaults());
        http.oauth2ResourceServer(oauth2 -> oauth2.jwt(Customizer.withDefaults()));

        return http.build();
    }

    @Bean
    public RegisteredClientRepository registeredClientRepository(
            final PasswordEncoder passwordEncoder,
            @Value("${app.auth.client-id:albedo-client}") final String clientId,
            @Value("${app.auth.client-secret:albedo-secret}") final String clientSecret,
            @Value("${app.auth.client-scopes:openid,profile,read,write}") final String scopes,
            @Value("${app.auth.client-redirect-uri:http://127.0.0.1:8080/login/oauth2/code/albedo-client}") final String redirectUri,
            @Value("${app.auth.access-token-ttl:PT1H}") final String accessTokenTtlValue,
            @Value("${app.auth.refresh-token-ttl:PT12H}") final String refreshTokenTtlValue) {

        final Duration accessTokenTtl = parseDuration(accessTokenTtlValue, "app.auth.access-token-ttl");
        final Duration refreshTokenTtl = parseDuration(refreshTokenTtlValue, "app.auth.refresh-token-ttl");

        final TokenSettings tokenSettings = TokenSettings.builder()
                .accessTokenTimeToLive(accessTokenTtl)
                .refreshTokenTimeToLive(refreshTokenTtl)
                .reuseRefreshTokens(true)
                .build();

        final ClientSettings clientSettings = ClientSettings.builder()
                .requireAuthorizationConsent(false)
                .build();

        final RegisteredClient.Builder clientBuilder = RegisteredClient.withId(UUID.randomUUID().toString())
                .clientId(clientId)
                .clientSecret(passwordEncoder.encode(clientSecret))
                .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_BASIC)
                .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_POST)
                .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                .authorizationGrantType(AuthorizationGrantType.CLIENT_CREDENTIALS)
                .authorizationGrantType(AuthorizationGrantType.REFRESH_TOKEN)
                .authorizationGrantType(PASSWORD_GRANT_TYPE)
                .tokenSettings(tokenSettings)
                .clientSettings(clientSettings)
                .redirectUri(redirectUri);

        for (final String scope : scopes.split(",")) {
            final String trimmed = scope.trim();
            if (!trimmed.isEmpty()) {
                clientBuilder.scope(trimmed);
            }
        }

        final RegisteredClient registeredClient = clientBuilder.build();

        return new InMemoryRegisteredClientRepository(registeredClient);
    }

    @Bean
    public ProviderSettings providerSettings(
            @Value("${app.auth.issuer:http://localhost:8080}") final String issuer) {
        return ProviderSettings.builder()
                .issuer(issuer)
                .build();
    }

    @Bean
    public JWKSource<SecurityContext> jwkSource() {
        final KeyPair keyPair = generateRsaKey();
        final RSAKey rsaKey = new RSAKey.Builder((RSAPublicKey) keyPair.getPublic())
                .privateKey((RSAPrivateKey) keyPair.getPrivate())
                .keyID(UUID.randomUUID().toString())
                .build();
        final JWKSet jwkSet = new JWKSet(rsaKey);
        return new ImmutableJWKSet<>(jwkSet);
    }

    @Bean
    public JwtEncoder jwtEncoder(final JWKSource<SecurityContext> jwkSource) {
        return new NimbusJwtEncoder(jwkSource);
    }

    @Bean
    public JwtDecoder jwtDecoder(final JWKSource<SecurityContext> jwkSource) {
        return OAuth2AuthorizationServerConfiguration.jwtDecoder(jwkSource);
    }

    @Bean
    public OAuth2ResourceOwnerPasswordAuthenticationConverter passwordAuthenticationConverter() {
        return new OAuth2ResourceOwnerPasswordAuthenticationConverter();
    }

    @Bean
    public OAuth2ResourceOwnerPasswordAuthenticationProvider passwordAuthenticationProvider(
            final AuthenticationManager authenticationManager,
            final OAuth2AuthorizationService authorizationService,
            final JwtEncoder jwtEncoder,
            final ProviderSettings providerSettings) {
        return new OAuth2ResourceOwnerPasswordAuthenticationProvider(authenticationManager, authorizationService,
                jwtEncoder, providerSettings);
    }

    @Bean
    public OAuth2AuthorizationService authorizationService() {
        return new InMemoryOAuth2AuthorizationService();
    }

    @Bean
    public OAuth2AuthorizationConsentService authorizationConsentService() {
        return new InMemoryOAuth2AuthorizationConsentService();
    }

    private static KeyPair generateRsaKey() {
        try {
            final KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
            keyPairGenerator.initialize(2048);
            return keyPairGenerator.generateKeyPair();
        } catch (final Exception ex) {
            throw new IllegalStateException("Failed to generate RSA key pair", ex);
        }
    }

    private static Duration parseDuration(final String value, final String propertyName) {
        try {
            return Duration.parse(value);
        } catch (final DateTimeParseException ex) {
            throw new IllegalArgumentException("Invalid duration for property '" + propertyName + "': " + value, ex);
        }
    }
}

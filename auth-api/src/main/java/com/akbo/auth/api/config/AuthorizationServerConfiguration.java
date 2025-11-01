package com.akbo.auth.api.config;

import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.SecurityContext;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.OAuth2AuthorizationServerConfiguration;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.oauth2.server.authorization.JdbcOAuth2AuthorizationConsentService;
import org.springframework.security.oauth2.server.authorization.JdbcOAuth2AuthorizationService;
import org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationConsentService;
import org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationService;
import org.springframework.security.oauth2.server.authorization.client.JdbcRegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.config.ClientSettings;
import org.springframework.security.oauth2.server.authorization.config.ProviderSettings;
import org.springframework.security.oauth2.server.authorization.config.TokenSettings;
import org.springframework.security.web.SecurityFilterChain;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.time.Duration;
import java.time.format.DateTimeParseException;
import java.util.UUID;

@Configuration
public class AuthorizationServerConfiguration {

    @Bean
    @Order(Ordered.HIGHEST_PRECEDENCE)
    public SecurityFilterChain authorizationServerSecurityFilterChain(final HttpSecurity http) throws Exception {
        OAuth2AuthorizationServerConfiguration.applyDefaultSecurity(http);
        return http.formLogin(Customizer.withDefaults()).build();
    }

    @Bean
    public RegisteredClientRepository registeredClientRepository(
            final JdbcTemplate jdbcTemplate,
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
                .authorizationGrantType(AuthorizationGrantType.PASSWORD)
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

        final JdbcRegisteredClientRepository repository = new JdbcRegisteredClientRepository(jdbcTemplate);
        if (repository.findByClientId(clientId) == null) {
            repository.save(registeredClient);
        }
        return repository;
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
        return (jwkSelector, securityContext) -> jwkSelector.select(jwkSet);
    }

    @Bean
    public OAuth2AuthorizationService authorizationService(final JdbcTemplate jdbcTemplate,
                                                           final RegisteredClientRepository registeredClientRepository) {
        return new JdbcOAuth2AuthorizationService(jdbcTemplate, registeredClientRepository);
    }

    @Bean
    public OAuth2AuthorizationConsentService authorizationConsentService(final JdbcTemplate jdbcTemplate,
                                                                         final RegisteredClientRepository registeredClientRepository) {
        return new JdbcOAuth2AuthorizationConsentService(jdbcTemplate, registeredClientRepository);
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

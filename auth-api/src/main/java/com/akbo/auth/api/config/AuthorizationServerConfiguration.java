package com.akbo.auth.api.config;

import com.akbo.auth.api.jose.Keys;
import com.akbo.auth.api.oauth.password.OAuth2PasswordAuthenticationProvider;
import com.akbo.auth.api.oauth.password.PasswordAuthenticationConverter;
import com.nimbusds.jose.jwk.JWK;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.SecurityContext;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
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
import org.springframework.security.oauth2.server.authorization.config.annotation.web.configuration.OAuth2AuthorizationServerConfiguration;
import org.springframework.security.oauth2.server.authorization.config.annotation.web.configurers.OAuth2AuthorizationServerConfigurer;
import org.springframework.security.oauth2.server.authorization.settings.AuthorizationServerSettings;
import org.springframework.security.oauth2.server.authorization.settings.ClientSettings;
import org.springframework.security.oauth2.server.authorization.settings.TokenSettings;
import org.springframework.security.web.SecurityFilterChain;

import java.time.Duration;
import java.time.format.DateTimeParseException;
import java.util.List;

@Configuration
public class AuthorizationServerConfiguration {

    @Bean
    @Order(Ordered.HIGHEST_PRECEDENCE)
    public SecurityFilterChain authorizationServerSecurityFilterChain(final HttpSecurity http,
                                                                      final OAuth2PasswordAuthenticationProvider passwordAuthenticationProvider) throws Exception {
        OAuth2AuthorizationServerConfiguration.applyDefaultSecurity(http);

        // Register password grant converter and provider on the token endpoint
        OAuth2AuthorizationServerConfigurer authorizationServerConfigurer = http.getConfigurer(OAuth2AuthorizationServerConfigurer.class);
        if (authorizationServerConfigurer != null) {
            authorizationServerConfigurer
                    .tokenEndpoint(tokenEndpoint -> tokenEndpoint
                            .accessTokenRequestConverter(new PasswordAuthenticationConverter())
                            .authenticationProvider(passwordAuthenticationProvider)
                    );
        }

        return http.build();
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
                .requireProofKey(false)
                .build();

        @SuppressWarnings("deprecation") final RegisteredClient.Builder clientBuilder = RegisteredClient
                .withId(clientId)
                .clientId(clientId)
                .clientSecret(passwordEncoder.encode(clientSecret))
                .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_BASIC)
                .authorizationGrantType(AuthorizationGrantType.CLIENT_CREDENTIALS)
                .authorizationGrantType(AuthorizationGrantType.PASSWORD)
                .authorizationGrantType(AuthorizationGrantType.REFRESH_TOKEN)
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
    public AuthorizationServerSettings authorizationServerSettings(
            @Value("${app.auth.issuer:http://localhost:8080}") final String issuer) {
        return AuthorizationServerSettings.builder()
                .issuer(issuer)
                .build();
    }

    @Bean
    public JWKSource<SecurityContext> jwkSource(@Value("${spring.security.oauth2.resource-owner.jwt.signing-key}") final String symKey) {
        List<JWK> jwkKeys = List.of(Keys.getHs256Jwk(symKey), Keys.getRsaJwk());
        JWKSet jwkSet = new JWKSet(jwkKeys);
        return (jwkSelector, securityContext) -> jwkSelector.select(jwkSet);
    }

    @Bean
    public org.springframework.security.oauth2.jwt.JwtEncoder jwtEncoder(JWKSource<SecurityContext> jwkSource) {
        return new org.springframework.security.oauth2.jwt.NimbusJwtEncoder(jwkSource);
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


    @Bean
    public OAuth2PasswordAuthenticationProvider passwordAuthenticationProvider(
            @Lazy final AuthenticationManager authenticationManager,
            final OAuth2AuthorizationService authorizationService,
            final RegisteredClientRepository registeredClientRepository,
            final org.springframework.security.oauth2.jwt.JwtEncoder jwtEncoder,
            final AuthorizationServerSettings authorizationServerSettings) {
        return new OAuth2PasswordAuthenticationProvider(
                authenticationManager, authorizationService, registeredClientRepository, jwtEncoder, authorizationServerSettings);
    }

    private static Duration parseDuration(final String value, final String propertyName) {
        try {
            return Duration.parse(value);
        } catch (final DateTimeParseException ex) {
            throw new IllegalArgumentException("Invalid duration for property '" + propertyName + "': " + value, ex);
        }
    }
}

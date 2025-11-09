package com.akbo.auth.api.oauth.password;

import org.springframework.lang.Nullable;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.server.authorization.authentication.OAuth2AuthorizationGrantAuthenticationToken;

import java.util.Map;

/**
 * Authentication token representing the Resource Owner Password Credentials grant request.
 */
public class OAuth2PasswordAuthenticationToken extends OAuth2AuthorizationGrantAuthenticationToken {

    private final Authentication clientPrincipal;

    public OAuth2PasswordAuthenticationToken(Authentication clientPrincipal,
                                             Map<String, Object> additionalParameters) {
        super(new AuthorizationGrantType("password"), clientPrincipal, additionalParameters);
        this.clientPrincipal = clientPrincipal;
    }

    @Override
    @Nullable
    public Object getCredentials() {
        return "";
    }

    @Override
    public Authentication getPrincipal() {
        return this.clientPrincipal;
    }
}

package com.akbo.auth.api.security.password;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.server.authorization.authentication.OAuth2AuthorizationGrantAuthenticationToken;

public class OAuth2ResourceOwnerPasswordAuthenticationToken extends OAuth2AuthorizationGrantAuthenticationToken {

    private final UsernamePasswordAuthenticationToken usernamePasswordAuthentication;
    private final Set<String> scopes;

    public OAuth2ResourceOwnerPasswordAuthenticationToken(final AuthorizationGrantType authorizationGrantType,
            final Authentication clientPrincipal,
            final UsernamePasswordAuthenticationToken usernamePasswordAuthentication,
            final Set<String> scopes,
            final Map<String, Object> additionalParameters) {
        super(authorizationGrantType, clientPrincipal,
                additionalParameters != null ? Collections.unmodifiableMap(new HashMap<>(additionalParameters))
                        : Collections.emptyMap());
        this.usernamePasswordAuthentication = usernamePasswordAuthentication;
        this.scopes = scopes != null ? Set.copyOf(scopes) : Collections.emptySet();
    }

    public UsernamePasswordAuthenticationToken getUsernamePasswordAuthentication() {
        return usernamePasswordAuthentication;
    }

    public Set<String> getScopes() {
        return scopes;
    }
}

package com.akbo.auth.api.security.password;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.AuthenticationConverter;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2ErrorCodes;
import org.springframework.security.oauth2.core.endpoint.OAuth2ParameterNames;

import com.akbo.auth.api.config.AuthorizationServerConfiguration;

public class OAuth2ResourceOwnerPasswordAuthenticationConverter implements AuthenticationConverter {

    @Override
    public Authentication convert(final HttpServletRequest request) {
        final String grantType = request.getParameter(OAuth2ParameterNames.GRANT_TYPE);
        if (!AuthorizationServerConfiguration.PASSWORD_GRANT_TYPE.getValue().equals(grantType)) {
            return null;
        }

        final String username = request.getParameter(OAuth2ParameterNames.USERNAME);
        final String password = request.getParameter(OAuth2ParameterNames.PASSWORD);
        if (username == null || password == null) {
            throw new OAuth2AuthenticationException(
                    new OAuth2Error(OAuth2ErrorCodes.INVALID_REQUEST, "username and password are required", null));
        }

        final String scope = request.getParameter(OAuth2ParameterNames.SCOPE);
        final Set<String> scopes;
        if (scope != null && !scope.isBlank()) {
            scopes = new HashSet<>();
            for (final String requestedScope : scope.split("\\s+")) {
                if (!requestedScope.isBlank()) {
                    scopes.add(requestedScope.trim());
                }
            }
        } else {
            scopes = Collections.emptySet();
        }

        final Map<String, Object> additionalParameters = new HashMap<>();
        request.getParameterMap()
                .forEach((key, value) -> {
                    if (!OAuth2ParameterNames.GRANT_TYPE.equals(key)
                            && !OAuth2ParameterNames.USERNAME.equals(key)
                            && !OAuth2ParameterNames.PASSWORD.equals(key)
                            && !OAuth2ParameterNames.SCOPE.equals(key)) {
                        additionalParameters.put(key, value != null && value.length > 0 ? value[0] : null);
                    }
                });

        final Authentication clientPrincipal = SecurityContextHolder.getContext().getAuthentication();
        if (clientPrincipal == null) {
            throw new OAuth2AuthenticationException(new OAuth2Error(OAuth2ErrorCodes.INVALID_CLIENT));
        }
        final UsernamePasswordAuthenticationToken usernamePasswordAuthentication =
                new UsernamePasswordAuthenticationToken(username, password);

        return new OAuth2ResourceOwnerPasswordAuthenticationToken(
                AuthorizationServerConfiguration.PASSWORD_GRANT_TYPE,
                clientPrincipal,
                usernamePasswordAuthentication,
                scopes,
                additionalParameters);
    }
}

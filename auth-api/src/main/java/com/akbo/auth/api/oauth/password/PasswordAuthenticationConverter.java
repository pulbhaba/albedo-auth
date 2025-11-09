package com.akbo.auth.api.oauth.password;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.AuthenticationConverter;

import java.util.HashMap;
import java.util.Map;

/**
 * Converts an OAuth2 Token Endpoint request with grant_type=password into
 * {@link OAuth2PasswordAuthenticationToken} for further authentication.
 */
public class PasswordAuthenticationConverter implements AuthenticationConverter {

    @Override
    public Authentication convert(HttpServletRequest request) {
        String grantType = request.getParameter("grant_type");
        if (!"password".equals(grantType)) {
            return null;
        }
        Map<String, Object> additional = new HashMap<>();
        putIfNotNull(additional, "username", request.getParameter("username"));
        putIfNotNull(additional, "password", request.getParameter("password"));
        putIfNotNull(additional, "scope", request.getParameter("scope"));

        Authentication clientPrincipal = SecurityContextHolder.getContext().getAuthentication();
        return new OAuth2PasswordAuthenticationToken(clientPrincipal, additional);
    }

    private static void putIfNotNull(Map<String, Object> map, String key, String value) {
        if (value != null) {
            map.put(key, value);
        }
    }
}

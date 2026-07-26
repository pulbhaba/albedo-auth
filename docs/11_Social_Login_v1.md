# 11. Social Login (Federated Identity)

Albedo Auth supports Social Login (Federated Identity) using OAuth 2.0 and OpenID Connect (OIDC). This allows users to sign in using their existing accounts from providers like Google, Facebook, and GitHub.

## Overview

Social login is implemented using Spring Security OAuth2 Client. When enabled, users can authenticate via a third-party provider, and Albedo Auth will automatically create or link a local user account based on the information provided by the social provider.

## Configuration

Social login is opt-in and can be configured in `auth-api/src/main/resources/application-social.properties`.

### Enabling Providers

You can enable or disable specific providers using properties:

```properties
app.auth.social-login.enabled=true
app.auth.social-login.providers.google.enabled=true
app.auth.social-login.providers.facebook.enabled=true
app.auth.social-login.providers.github.enabled=true
```

### Provider Credentials

For each enabled provider, you must provide a `client-id` and `client-secret` obtained from the provider's developer console.

```properties
spring.security.oauth2.client.registration.google.client-id=${GOOGLE_CLIENT_ID}
spring.security.oauth2.client.registration.google.client-secret=${GOOGLE_CLIENT_SECRET}
spring.security.oauth2.client.registration.google.scope=openid,profile,email
```

## Supported Providers

- **Google**: Supports OIDC.
- **Facebook**: Supports OAuth 2.0.
- **GitHub**: Supports OAuth 2.0.
- **Microsoft**: Configuration placeholder exists, disabled by default.

## Flow

1. **Initiate Login**: The client application redirects the user to the Albedo Auth social login initiation endpoint (e.g., `/oauth2/authorization/google`).
2. **Provider Authentication**: The user is redirected to the social provider (e.g., Google) to authenticate and authorize the application.
3. **Callback**: The provider redirects back to Albedo Auth with an authorization code.
4. **Token Exchange**: Albedo Auth exchanges the code for an access token and retrieves the user's profile.
5. **User Mapping**: Albedo Auth maps the social profile to a local `User` entity. If the user does not exist, a new record is created.
6. **Session/Token Issuance**: A local session is established, or an OAuth2 token is issued to the client.

## Implementation Details

The core logic for handling social logins is located in:
- `FederatedIdentityOAuth2UserService`: Custom service that handles the mapping between OAuth2/OIDC user attributes and the local `User` entity.
- `SocialLoginProperties`: Configuration properties for social login.
- `application-social.properties`: Externalized configuration for providers.

## Development Notes

- For local development, ensure that the redirect URIs configured in the provider's console match your local environment (e.g., `http://localhost:8080/login/oauth2/code/google`).
- Social login is disabled by default in the main `application.properties`. To enable it, set `app.auth.social-login.enabled=true` and ensure the `social` Spring profile is active or properties are correctly set.

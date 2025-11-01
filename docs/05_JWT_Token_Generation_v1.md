# 5. JWT Token Generation

Albedo Auth exposes the OAuth 2.0 token endpoint provided by Spring Authorization Server.  
Clients authenticate with their client credentials and, for user login, request tokens
using the Resource Owner Password flow implemented in the API.

## OAuth 2.0 Token Endpoint

- **URL:** `POST /oauth2/token`
- **Authentication:** HTTP Basic (or `client_id`/`client_secret` in the request body)
- **Content-Type:** `application/x-www-form-urlencoded`

### Supported Grant Types

- `password` – authenticates an existing user with username and password.
- `client_credentials` – service-to-service access (no end user).
- `refresh_token` – obtain a new access token using a previously issued refresh token.
- `authorization_code` – available for future UI integrations; requires the redirect URI configured for the client.

### Password Grant Request

```
POST /oauth2/token
Authorization: Basic YWxiZWRvLWNsaWVudDphbGJlZG8tc2VjcmV0
Content-Type: application/x-www-form-urlencoded

grant_type=password&username=reader01&password=P%40ssw0rd123&scope=read%20write
```

### Successful Response

```json
{
  "access_token": "eyJraWQiOiI1Ym...snip...TQ",
  "token_type": "Bearer",
  "expires_in": 3600,
  "scope": "read write",
  "refresh_token": "eyJraWQiOiI5MD...snip...0Q"
}
```

- `access_token`: JWT signed with the server’s RSA key.
- `token_type`: Always `Bearer`.
- `expires_in`: Access token lifetime in seconds (default 3600 seconds).
- `scope`: Granted scopes; defaults to the client’s configured scopes when omitted.
- `refresh_token`: Present when the client is allowed to receive refresh tokens.

### Error Responses

The endpoint follows OAuth 2.0 error semantics, e.g.:

- `invalid_grant`: Wrong username or password.
- `invalid_client`: Missing or invalid client credentials.
- `invalid_scope`: Requested scope is not registered on the client.

## Token Structure

JWT access tokens produced by the server contain:

- **Header:** `alg` (`RS256`), `typ` (`JWT`), `kid` (identifier of the active RSA key).
- **Payload Claims:** Standard claims (`iss`, `sub`, `aud`, `exp`, `iat`, `jti`) and granted scopes under `scope`.
- **Signature:** RS256 signature generated from a 2048-bit RSA key that is rotated at startup.

Tokens are issued by the configured issuer (`app.auth.issuer`, default `http://localhost:8080`).

## Lifetimes

- **Access Token TTL:** Configurable via `app.auth.access-token-ttl` (default 1 hour).
- **Refresh Token TTL:** Configurable via `app.auth.refresh-token-ttl` (default 12 hours).
- Refresh tokens are reusable (`reuseRefreshTokens=true`); set to false when stricter rotation is required.

## Client Configuration

Register confidential clients through configuration (see `application.properties`):

```
app.auth.client-id=albedo-client
app.auth.client-secret=albedo-secret
app.auth.client-scopes=openid,profile,read,write
app.auth.client-redirect-uri=http://127.0.0.1:8080/login/oauth2/code/albedo-client
```

- Secrets are stored hashed with the configured `PasswordEncoder`.
- Additional clients can be added by expanding the registration logic.

## Usage Notes

- Protect client credentials; use app passwords or secrets management in production.
- Prefer the `authorization_code` flow with PKCE for browser-based clients.
- When exposing the password grant for trusted first-party applications, enforce TLS and rate limits on `/oauth2/token`.

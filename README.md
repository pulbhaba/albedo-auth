# Albedo Auth

A modular Spring Boot–based authentication and authorization service. It provides:

- OAuth2 Authorization Server (Authorization Code, Client Credentials, Password, Refresh Token)
- Basic authentication for selected endpoints
- User registration and lookup
- Password change and password reset request with pluggable notification channels (email, SMS, logging)
- MySQL persistence for users, roles, and OAuth2 tokens/clients

This repository is organized as a Gradle multi-module project.

## Repository Structure

- `auth-api/` — Spring Boot application exposing REST endpoints and hosting the OAuth2 Authorization Server
- `auth-data/` — JPA entities and Spring Data repositories (database layer)
- `auth-client/` — DTOs, exceptions, and utilities shared with clients/other modules
- `docs/` — Additional system documentation (architecture, flows, deployment, etc.)
- `api.http` — HTTP request examples for local testing (IntelliJ HTTP client compatible)

## Key Features

- Spring Boot 2.7.x, Java 11
- OAuth2 Authorization Server (`spring-security-oauth2-authorization-server:0.2.0`)
- Basic Auth for `/user/**` and `/admin/**` paths; `/public/**` is open
- Token lifetimes and OAuth2 client registration configurable via properties/env vars
- Pluggable password reset notification channels (email, SMS, log)
- CORS enabled and permissive by default (configure for production)
- Actuator enabled with mappings endpoint exposed

## Prerequisites

- Java 11+
- Gradle (wrapper included)
- MySQL 8.x running locally with a database named `auth` and credentials configured (see Configuration)

Quick MySQL setup (example):

```
CREATE DATABASE auth CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE USER 'root'@'%' IDENTIFIED BY 'test_pass';
GRANT ALL PRIVILEGES ON auth.* TO 'root'@'%';
FLUSH PRIVILEGES;
```

Adjust credentials as needed. In development, you can use any user; just ensure the `spring.datasource.*` properties
match.

## Build

From the project root:

```
./gradlew clean build
```

This builds all subprojects and runs tests (if present). The main application lives in `auth-api`.

## Run Locally

From the project root, run the API app:

```
./gradlew :auth-api:bootRun
```

The application starts by default on `http://localhost:8080`.

On startup, roles from `auth-client`'s `Role` enum are inserted via a `CommandLineRunner` (`AuthApplication`).

## Configuration

Most configuration lives in `auth-api/src/main/resources/application.properties` (and `application-dev.yaml`). You can
override properties via environment variables. Important properties:

Database:

- `spring.datasource.driver-class-name` (default: `com.mysql.cj.jdbc.Driver`)
- `spring.datasource.url` (default: `jdbc:mysql://localhost:3306/auth`)
- `spring.datasource.username` (default: `root`)
- `spring.datasource.password` (default: `test_pass`)
- `spring.jpa.hibernate.ddl-auto` (default: `update`)

OAuth2 and Client Registration:

- `app.auth.issuer` (env: `APP_AUTH_ISSUER`, default: `http://localhost:8080`)
- `app.auth.client-id` (env: `APP_AUTH_CLIENT_ID`, default: `albedo-client`)
- `app.auth.client-secret` (env: `APP_AUTH_CLIENT_SECRET`, default: `albedo-secret`)
- `app.auth.client-scopes` (env: `APP_AUTH_CLIENT_SCOPES`, default: `openid,profile,read,write`)
- `app.auth.client-redirect-uri` (env: `APP_AUTH_CLIENT_REDIRECT_URI`, default:
  `http://127.0.0.1:8080/login/oauth2/code/albedo-client`)
- `app.auth.access-token-ttl` (env: `APP_AUTH_ACCESS_TOKEN_TTL`, ISO-8601 duration, default: `PT1H`)
- `app.auth.refresh-token-ttl` (env: `APP_AUTH_REFRESH_TOKEN_TTL`, ISO-8601 duration, default: `PT12H`)

Notifications (Password Reset):

- `notification.email.enabled` (default: `false`)
- `notification.email.from-address` (env: `MAIL_FROM_ADDRESS`)
- `notification.email.subject` (default: `Albedo Auth Password Reset`)
- `notification.email.reset-url` (env: `PASSWORD_RESET_URL`, base URL that receives the token param)
- `notification.sms.enabled` (default: `false`)
- Spring Mail properties are present but commented; configure `spring.mail.*` and set `notification.email.enabled=true`
  to send emails.

Actuator:

- `management.endpoints.web.exposure.include=mappings` (exposes mappings endpoint)

Schema Initialization:

- `spring.sql.init.schema-locations` references the SQL schemas required by the Authorization Server tables. With
  `spring.jpa.hibernate.ddl-auto=update`, JPA-managed tables evolve automatically; the OAuth2 tables are initialized
  from these scripts.

## Security Overview

- Basic Auth is enabled for `/user/**` and `/admin/**`; `/public/**` is accessible without authentication. See
  `BasicAuthWebSecurityConfiguration`.
- OAuth2 Authorization Server is configured in `AuthorizationServerConfiguration` with registered client details sourced
  from properties. On startup, it writes the client to the database if not present.
- CORS is enabled with `*` for origins, headers, and methods by default. Harden this for production.

## REST Endpoints (selected)

Public (no auth):

- `POST /public/change-password/` — Change password given a `PasswordChangeDto`
- `GET /public/user/{username}/reset-password` — Initiate a password reset (sends notification)
- `POST /public/user/register` — Register a new user with `UserDto`

User (Basic Auth):

- `GET /user/{username}` — Fetch user details (`UserDto`)

Authorization Server (standard endpoints, base at `/oauth2` with issuer configured):

- `/oauth2/authorize` — Authorization endpoint
- `/oauth2/token` — Token endpoint
- `/oauth2/jwks` — JWKS endpoint

Note: The exact well-known endpoints depend on the Authorization Server defaults and the configured issuer. Provider
settings are built with the `app.auth.issuer` property.

## Example Requests

IntelliJ HTTP client file `api.http` contains ready-to-run examples. You can also use curl:

1) Register a user:

```
curl -X POST http://localhost:8080/public/user/register \
  -H 'Content-Type: application/json' \
  -d '{
        "username": "alice",
        "password": "Password123!",
        "email": "alice@example.com"
      }'
```

2) Change password:

```
curl -X POST http://localhost:8080/public/change-password/ \
  -H 'Content-Type: application/json' \
  -d '{
        "username": "alice",
        "oldPassword": "Password123!",
        "newPassword": "NewPassword456!"
      }'
```

3) Request password reset:

```
curl http://localhost:8080/public/user/alice/reset-password
```

4) Get user (Basic Auth):

```
curl -u alice:NewPassword456! http://localhost:8080/user/alice
```

5) Get token via Client Credentials:

```
CLIENT_ID=albedo-client
CLIENT_SECRET=albedo-secret
curl -u "$CLIENT_ID:$CLIENT_SECRET" \
  -d 'grant_type=client_credentials' \
  -d 'scope=read' \
  http://localhost:8080/oauth2/token
```

## Modules Overview

- `auth-api`
    - Spring Boot application class: `AuthApplication`
    - Config: `AuthorizationServerConfiguration` (OAuth2 AS), `BasicAuthWebSecurityConfiguration` (security & CORS)
    - Controllers: `PublicController`, `UserController`
    - Services: `UserService`, `PasswordService` (with `impl`), notifications under `service/notification`
    - Resources: `application.properties`, `application-dev.yaml`, banner, etc.

- `auth-data`
    - Entities: `User`, `UserRole`, `PasswordChangeRequest`
    - Repositories: `UserRepository`, `UserRoleRepository`, `PasswordResetRequestRepository`

- `auth-client`
    - DTOs: `UserDto`, `PasswordChangeDto`, `ErrorDto`, etc.
    - Exceptions: `BadRequestException`, `ForbiddenException`, `NotFoundException`, `UnauthorizedException`
    - Utilities: `PasswordTools`

## Development Notes

- Default password encoder: `BCryptPasswordEncoder(8)`
- Model mapping via `org.modelmapper:modelmapper`
- ID encoding utilities via `org.sqids:sqids`
- Logging level defaults to INFO; tweak via `logging.level.*` in properties

## Testing

Run tests (if present):

```
./gradlew test
```

## Deployment

- Build a bootable jar from `auth-api` if you prefer:

```
./gradlew :auth-api:bootJar
```

The jar will be in `auth-api/build/java/libs/` (custom `buildDir` is set to `build/java`).

- Configure environment variables for the target environment (database, issuer, OAuth2 client, mail) and start the app
  with your process manager/container.

## Documentation

See the `docs/` directory for detailed guides:

- `01_Introduction_v1.md`
- `02_System_Architecture_v1.md`
- `03_User_Registration_v1.md`
- `04_Roles_and_Permissions_v1.md`
- `05_JWT_Token_Generation_v1.md`
- `06_Password_Reset_v1.md`
- `07_Security_Considerations_v1.md`
- `08_Implementation_Details_v1.md`
- `09_Testing_v1.md`
- `10_Deployment_v1.md`

## License

This project is licensed under the terms of the `LICENSE` file included in this repository.

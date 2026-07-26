# 8. Implementation Details

## Database Schema

Albedo Auth uses MySQL as its primary data store. The schema consists of core tables for user management and standard tables required by Spring Authorization Server.

### Core Tables

#### `users`
Stores user profile information.
- `id` (BIGINT, PK): Primary key, generated via sequence.
- `username` (VARCHAR, Unique): Unique username for the user.
- `password` (VARCHAR): BCrypt encoded password.
- `email_address` (VARCHAR): User's email address.
- `first_name` (VARCHAR): User's first name.
- `last_name` (VARCHAR): User's last name.
- `phone_number` (VARCHAR): User's phone number.
- `enabled` (BIT): Account status.
- `account_non_expired` (BIT): Whether the account has expired.
- `account_non_locked` (BIT): Whether the account is locked.
- `credentials_non_expired` (BIT): Whether the credentials have expired.
- `created_time` (DATETIME): Timestamp when the user was created.
- `last_updated_time` (DATETIME): Timestamp of the last update.
- `created_by` (VARCHAR): User who created the record.
- `last_updated_by` (VARCHAR): User who last updated the record.

#### `roles`
Stores the available roles in the system (e.g., `ROLE_USER`, `ROLE_ADMIN`).
- `role` (VARCHAR, PK): The role name.

#### `user_roles`
Join table mapping users to their roles.
- `user_id` (BIGINT, FK): Reference to `users.id`.
- `role` (VARCHAR, FK): Reference to `roles.role`.

#### `password_change_request`
Tracks password reset requests.
- `id` (BIGINT, PK): Primary key.
- `user_id` (BIGINT, FK): Reference to `users.id`.
- `random_string` (VARCHAR): Verification string for the reset request.
- `expired` (BIT): Whether the request has expired.
- `password_changed` (BIT): Whether the password was successfully changed.
- `email_notification_sent` (DATETIME): When the notification was sent.

### OAuth2 Tables
The system also includes standard tables for Spring Authorization Server:
- `oauth2_authorization`: Stores authorization grants.
- `oauth2_authorization_consent`: Stores user consent.
- `oauth2_registered_client`: Stores registered OAuth2 clients.

## API Documentation

### Public Endpoints (`/public/**`)
No authentication required.

#### 1. User Registration
- **URL:** `POST /public/user/register`
- **Request Body:** `UserDto` (JSON)
- **Response:** `UserDto` (JSON)
- **Description:** Creates a new user in the system.

#### 2. Request Password Reset
- **URL:** `GET /public/user/{username}/reset-password`
- **Response:** Empty (200 OK)
- **Description:** Generates a password reset token and sends it via the configured notification channel.

#### 3. Change Password
- **URL:** `POST /public/change-password/`
- **Request Body:** `PasswordChangeDto` (JSON)
- **Response:** Success message (JSON)
- **Description:** Completes the password reset process using the encrypted request key.

### User Endpoints (`/user/**`)
Requires HTTP Basic Authentication.

#### 1. Get User Profile
- **URL:** `GET /user/{username}`
- **Response:** `UserDto` (JSON)
- **Description:** Retrieves the profile details of the specified user.

### OAuth2 Endpoints
See `05_JWT_Token_Generation_v1.md` for details.

## Error Handling

Albedo Auth uses a centralized error handling mechanism via `GlobalExceptionHandler`. Errors are returned in a standard format:

### Error Response Format (`ErrorDto`)
```json
{
  "message": "Error message description",
  "statusCode": 401,
  "data": {
    "error": "Detailed error message",
    "stackTrace": "..."
  }
}
```

### Standard Error Codes
- `400 Bad Request`: Invalid request data or missing parameters.
- `401 Unauthorized`: Authentication failed or token expired.
- `403 Forbidden`: Authenticated user lacks sufficient permissions.
- `404 Not Found`: Requested resource (user, etc.) does not exist.
- `500 Internal Server Error`: Unexpected server error.

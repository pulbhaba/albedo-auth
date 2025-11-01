
# 6. Password Reset

The password reset flow in the codebase is fully implemented through the
`PublicController` and `PasswordServiceImpl` classes. It provides a
two-step process: generating an encrypted reset token and using that token
to set a new password.

## Components Involved
- `PublicController` (`auth-api/src/main/java/com/akbo/auth/api/controller/PublicController.java`)
  - `GET /public/user/{username}/reset-password`
  - `POST /public/change-password/`
- `PasswordServiceImpl` orchestrates token issuance, encryption, and password updates.
- `PasswordResetNotificationService` interface defines the notification contract; `EmailPasswordResetNotificationService`
  is currently active and sends messages through the configured personal SMTP account.
- `SmsPasswordResetNotificationService` is available but disabled until an SMS provider is selected.
- `PasswordChangeRequest` entity (`auth-data/src/main/java/com/akbo/auth/dao/entity/PasswordChangeRequest.java`)
  persists reset attempts with auditing enabled.
- `PasswordTools` (`auth-client/src/main/java/com/akbo/auth/util/PasswordTools.java`) handles AES encryption/decryption.

## Requesting a Reset Token
### Endpoint
`GET /public/user/{username}/reset-password`

### Behaviour
- Generates a new `PasswordChangeRequest` row with a random 18-character string.
- Encrypts the `{requestId}|{randomString}` pair using AES/CBC with a shared key.
- Logs the encrypted token (`requestKey`) for observability and emails it to the user through the active notification service.
  Emails are sent from the developer's configured personal SMTP account, making the feature usable without a third-party provider.
  When email delivery is disabled or no user record exists, the system falls back to logging the token only.
- Returns `200 OK` with an empty body. Unknown usernames still trigger token generation
  without an associated user to avoid disclosing account existence.

### Example (development log extract)
```
INFO  PasswordServiceImpl - The encrypted key for password reset is: NsAq4th5PwXs2lVwQV9qgQ==
```

## Submitting a New Password
### Endpoint
`POST /public/change-password/`

### Request Body
```json
{
  "requestKey": "NsAq4th5PwXs2lVwQV9qgQ==",
  "newPassword": "NewSecureP@55word"
}
```

### Processing Steps
1. `PasswordServiceImpl` decrypts `requestKey` with the shared AES key.
2. It splits the payload into `requestId` and `randomString`.
3. `PasswordResetRequestRepository#findOneByIdAndRandomStringNotExpired` validates:
   - Token exists
   - Not expired
   - Not previously used (`passwordChanged` flag)
4. On success, the user’s password is encoded via the configured `PasswordEncoder` and saved.
5. The reset request is flagged `passwordChanged = true` to prevent reuse.

### Responses
- `200 OK` with the updated `UserDto` (sensitive fields such as the encoded password must be ignored by clients).
- `401 Unauthorized` with `ErrorDto` when the token does not match a stored request or has already been used.
- `500 Internal Server Error` if the encrypted key cannot be decrypted (the current implementation rethrows as a runtime exception).

## Data Model Notes
- `PasswordChangeRequest` tracks:
  - `user` reference (nullable when the username is unknown)
  - `randomString`, `expired`, `passwordChanged`, and `emailNotificationSent`
- Entities are Envers-audited, enabling traceability of reset actions.
- `User` passwords are always stored as hashes; raw passwords never persist.

## Operational Considerations
- **Delivery channel:** Email delivery is active when `notification.email.enabled=true`. Provide SMTP details (e.g., Gmail) via `MAIL_*`
  environment variables so the application can authenticate with the personal account. When disabled, the fallback implementation only logs the token.
- **Token lifetime:** The current implementation relies on the repository query to exclude expired tokens, but no automatic expiry timestamp is set yet. Consider extending the entity to record an expiration instant.
- **Security logging:** Logs include the encrypted token. Rotate logs or mask tokens before moving to production.
- **Rate limiting:** Add throttling on the reset request endpoint to prevent abuse.
- **Global error mapping:** Hook `UsernameNotFoundException` into a `404` response once public disclosure is acceptable and add a dedicated handler for decryption failures to return `400 Bad Request`.

## Future Enhancements
- Wire notifications to send the `requestKey` via configured channels.
- Add configurable validity periods and automatic expiration for reset requests.
- Record the requesting IP and user agent in `PasswordChangeRequest` for audit trails.
- Provide localisation-ready email/SMS templates referencing the reset URL.

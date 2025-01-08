
# 6. Password Reset
## Request Password Reset
### Endpoint
`/password-reset/request`

### HTTP Method
POST

### Request Payload
```json
{
  "email": "string"
}
```

### Response
```json
{
  "message": "Password reset link sent to email"
}
```

## Reset Password
### Endpoint
`/password-reset/confirm`

### HTTP Method
POST

### Request Payload
```json
{
  "token": "reset_token",
  "newPassword": "string"
}
```

### Response
```json
{
  "message": "Password reset successfully"
}
```


# 4. Roles and Permissions
## Roles
Define the different roles (e.g., USER, ADMIN).

## Permissions
Detail the permissions associated with each role.

## Endpoint
`/roles`

### HTTP Method
POST

### Request Payload
```json
{
  "roleName": "string",
  "permissions": ["permission1", "permission2"]
}
```

### Response
```json
{
  "message": "Role created successfully",
  "roleId": "uuid"
}
```

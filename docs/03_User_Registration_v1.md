
# 3. User Registration
## Endpoint
```
/user/register
```
## HTTP Method
```
POST
```
## Request Payload
```
{
"username": "string",
"firstName": "string",
"lastName": "string",
"enabled": boolean,
"password": "string",
"roles": ["string"]
}
```
## Response
```
{
"message": "Registration request submitted successfully. Please await admin approval.",
"userId": "uuid"
}
```
## Validation Rules
*  **Username**: Must be unique and meet length requirements (e.g., 3-20 characters).
*  **First Name**: Must be a string.
*  **Last Name**: Must be a string.
*  **Enabled**: Must be a boolean value.
*  **Password**: Must meet complexity requirements (e.g., minimum 8 characters, including uppercase, lowercase, numbers, and special characters).
*  **Roles**: Must be an array of strings representing user roles.
## Approval Process
1.  **User Registration**: Users can register themselves using the `/user/register` endpoint.
2.  **Pending Approval**: Registered users are marked as pending approval.
3.  **Admin Approval**: An admin must approve the registration request using the `/admin/users/{userId}/approve` endpoint.
## Admin Approval Endpoint
### Endpoint
```
/admin/users/{userId}/approve
```
### HTTP Method
```
POST
```
### Request Payload
```
{
"status": "approved"
}
```
### Response
```
{
"message": "User registration approved successfully."
}
```
### Description
* Approves a pending user registration request.
* The `userId` in the URL path specifies the user to be approved.
## Additional Considerations
#### **User Verification**
- Implement a verification process utilizing either email or SMS verification methods.
- Introduce a `identities` attribute in the user entity to store the user's contact information for verification, which could be an array of email addresses and phone numbers.
- Utilize the `contact_type` attribute to distinguish between email and phone number for the selected verification process.
- The `username` attribute will store the user's phone number with the country code for unique identification and login purposes.
- The `country` attribute will capture the user's country information to ensure proper handling of the chosen verification method and communication.

By storing the user's contact information in an array within the `identities` attribute and using the `contact_type` attribute, the user's contact details, including multiple email addresses and phone numbers, can be effectively managed and verified during the registration process.
####  **Email Verification**:
- Introduce an email verification step to confirm the user’s email address before approval.
- Use the following endpoint structure for email verification: `/public/confirm/{temporary-token}`.
- The temporary token should be a JWT token containing the user ID.
- Upon clicking the verification link, the backend server should validate the JWT token and set the "verified" attribute in the user's account to true.
- Invalidate the JWT token after it has been used for verification to prevent future use.
####  **SMS Verification**:
- Implement SMS verification to validate the user's contact information during registration.
- Evaluate the use of a reliable SMS service provider to handle verification code delivery and confirmation.
- Use the following endpoint structure for SMS verification: `/public/confirm/{temporary-token}`.
- The temporary token should be a JWT token containing the user ID.
- Upon entering the verification code sent via SMS, the frontend should send a request to the backend server to validate the JWT token and set the "verified" attribute in the user's account to true.
- Invalidate the JWT token after it has been used for verification to prevent future use.
####  **Admin Notifications(Future)**:
- Establish a notification system to alert administrators of new registration requests needing approval.
- Define the appropriate triggers and thresholds for admin notifications to ensure timely review and approval of user registrations.
- The notification system should be set up to send email notifications to designated administrators when new registration requests are submitted for approval.
- If there are multiple administrators, the notification system should distribute notifications to two administrators at a time to ensure a fair distribution of workload.
- Administrators should review and approve registration requests based on the value of the "require_approval" attribute in the user's role.
- If "require_approval" is set to true, one of the administrators must approve the user registration before the "approved" attribute in the user's account is set to true.
- For other users, the "approved" attribute will only be set to true once an administrator approves the registration request.
####  **Account Activation Notification for Users**:
- Implement an automated email notification system to notify users when their registration is approved and their account is activated.
####  **User Roles**:
- Define roles and permissions within the application to differentiate between regular users and administrators.
- Ensure that role management functionalities are in place to assign and manage user roles based on organizational requirements.
- Implement role-based access control to distinguish between regular users and administrators.
- Regular users with the "require_approval" attribute set to false can register without requiring approval from administrators.
- Administrators should have the authority to review and approve user registrations, based on the "require_approval" attribute in the user's role.
- The "approved" attribute for regular users should be set to true once they confirm their email, indicating that their account is activated and approved for use.
####  **Audit Logs**:
- Implement robust logging mechanisms, such as `Envers`, to capture and maintain audit logs of registration requests and administrative actions.
- Utilize database audit tables to store historical data and track changes in user registration activities, ensuring security, compliance, and auditing requirements are met.
- Implement logging with correlation IDs to facilitate tracing and debugging of registration-related processes and associated administrative actions.

By incorporating both email and SMS verification methods and introducing the relevant user attributes, the registration process can be enhanced with a multi-step verification approach while ensuring flexibility and security for user verification.

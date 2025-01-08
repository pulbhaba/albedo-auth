# Albedo Auth Service Architecture

## High-Level Architecture Diagram
*Include a diagram showing the interaction between the auth-service and other services.*

## Technology Stack
The Albedo Auth service will utilize the following technologies:
- **Spring Boot**: Backend framework.
- **JWT**: Authentication.
- **Gradle Groovy**: Project management.

## Project Structure
The Albedo Auth service is divided into three separate sub-projects using Gradle Groovy:

1. **auth-client**
   - Contains models and client implementations.
   - Future plans include implementing clients in multiple languages.

2. **auth-data**
   - Contains entities and repositories.

3. **auth-api**
   - Contains the service backend implementation.
   - Manages the core authentication and authorization logic.

## Controllers

The service includes three main controllers:

1. **User Controller**
   - Handles user-related tasks such as registration, login, and password reset.
   - Example endpoints:
     - `/user/register`: User registration.
     - `/user/login`: User login.
     - `/user/password-reset`: Password reset request.

2. **Admin Controller**
   - Manages administrative tasks such as role management and user administration.
   - Example endpoints:
     - `/admin/users`: Manage users.
     - `/admin/roles`: Manage roles.
     - `/admin/users/{userId}/approve`: Approve user registration.

3. **Public Controller**
   - Manages public-facing tasks that do not necessitate authentication.
   - Example endpoints:
     - `/public/docs`: Access API documentation or user guide.
     - `/public/terms`: Display the terms and conditions of using the service.
     - `/public/contact`: Show contact details or support information for users.
     - `/public/status`: Provide a basic status message indicating the service is up and running.
     - `/public/confirm/{temporary-token}`: Endpoint for email verification, which uses a JWT token to confirm the user’s email address before approval.

4. **OAuth Token Endpoint**
   - Managed by Spring Security OAuth2 configuration.
   - Example endpoint:
     - `/oauth/token`: Obtain OAuth2 tokens.
     - *Note*: This endpoint is provided and configured through Spring Security’s OAuth2 features, so no separate controller class is needed for this functionality.


# Albedo Auth Service - Build and Usage Instructions

## Instructions for Use

1. **Clone the Repository**: Clone the repository to your local machine.
    ```bash
    git clone <repository-url>
    cd albedo-auth
    ```

2. **Build the Project**: Use Gradle to build the project.
    ```bash
    ./gradlew build
    ```

3. **Run the Service**: Start the service using Gradle.
    ```bash
    ./gradlew bootRun
    ```

4. **Access the Endpoints**: Use a tool like Postman or cURL to interact with the API endpoints provided by the controllers.

## Additional Notes

- Ensure you have Java and Gradle installed on your machine.
- Refer to the high-level architecture diagram to understand the interaction between different components.

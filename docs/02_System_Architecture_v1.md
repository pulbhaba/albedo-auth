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

## Controllers & Endpoints

The authenticated API surface is intentionally small and composed of:

1. **UserController (`/user`)**
   - `GET /user/{username}` – Retrieve a user profile (requires authentication).

2. **PublicController (`/public`)**
   - `POST /public/user/register` – Self-service registration.
   - `GET /public/user/{username}/reset-password` – Initiate password reset (sends the encrypted token via email).
   - `POST /public/change-password/` – Finalise password change with the encrypted token.

3. **OAuth 2.0 Token Endpoint (`/oauth2/token`)**
   - Provided by Spring Authorization Server; supports `password`, `client_credentials`,
     `refresh_token`, and `authorization_code` grant types for issuing JWT access tokens.
   - Client authentication uses HTTP Basic or `client_id`/`client_secret` request parameters.


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

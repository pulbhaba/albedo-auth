# Remaining Tasks

This document tracks the tasks that are still pending or could be improved in the Albedo Auth project.

## Documentation
- [x] Complete Database Schema details in `08_Implementation_Details_v1.md`
- [x] Detail API Endpoints in `08_Implementation_Details_v1.md`
- [x] Define Error Handling mechanisms in `08_Implementation_Details_v1.md`
- [x] Add Social Login documentation in a new file `11_Social_Login_v1.md`
- [ ] Create a high-level architecture diagram for `02_System_Architecture_v1.md` (currently placeholder exists)
- [ ] Document the `api.http` file usage in more detail

## Security
- [ ] Implement rate limiting for public endpoints (registration, password reset)
- [ ] Enhance password strength validation during registration and change
- [ ] Configure HTTPS/TLS for all environments (currently mentioned as a consideration)
- [ ] Add account lockout mechanism after multiple failed login attempts

## Features
- [ ] Implement Email/SMS notification sub-providers (currently only `logging` is fully active by default)
- [ ] Add support for Multi-Factor Authentication (MFA)
- [ ] Expand Social Login providers (e.g., Apple, Twitter)
- [ ] Provide client libraries in other languages (currently only Java `auth-client` exists)

## Testing
- [ ] Increase unit test coverage for `auth-api` services
- [ ] Add performance/load tests for the authorization server
- [ ] Implement automated security scans in the CI/CD pipeline

## Infrastructure
- [ ] Provide Docker Compose setup for easier local development (database + app)
- [ ] Add Kubernetes manifests for deployment
- [ ] Configure centralized logging and monitoring (Actuator is enabled, but integration with Prometheus/ELK is missing)

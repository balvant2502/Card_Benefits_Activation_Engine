# Card Benefits Activation Engine

Backend service for detecting card benefit eligibility, activating claims, and
tracking the claim lifecycle for card transactions.

## What it provides

- JWT-based registration and login
- Demo virtual-card creation during registration
- Role-based access for customers and administrators
- Benefit and benefit-rule management
- Transaction capture and benefit eligibility checks
- Claim activation, submission, approval, rejection, and audit history
- Operational metrics for eligibility and claim processing

## Technology stack

- Java 17
- Spring Boot 4.1.1
- Spring Web, Spring Data JPA, Spring Security, and Thymeleaf
- PostgreSQL (runtime database) or H2 for local use
- Maven Wrapper
- JJWT for token generation and validation

## Project structure

```text
backend/
└── Benefits_Engine/
    ├── src/main/java/com/viva/benefits_engine/
    │   ├── controller/     REST endpoints
    │   ├── models/         JPA entities and enums
    │   ├── repository/     Data access interfaces
    │   ├── service/        Business logic and rules engine
    │   └── security/       JWT authentication
    └── src/test/            Tests
```

## Prerequisites

- JDK 17 or later
- PostgreSQL, unless using an H2 configuration
- Git

## Configuration

The backend reads configuration from
`backend/Benefits_Engine/src/main/resources/application.properties`.
The default server port is `8090`.

Before starting the application:

1. Configure `spring.datasource.url`, `spring.datasource.username`, and
   `spring.datasource.password` for a database you control.
2. Set `app.jwt.secret` to a long, unique secret and configure
   `app.jwt.expiration` in milliseconds.
3. Keep credentials and JWT secrets out of source control. Prefer environment
   variables or an external Spring configuration in deployed environments.

## Run locally

From the repository root:

```powershell
cd backend\Benefits_Engine
.\mvnw.cmd spring-boot:run
```

The API is then available at `http://localhost:8090`.

To build and run the tests:

```powershell
cd backend\Benefits_Engine
.\mvnw.cmd test
.\mvnw.cmd clean package
```

## Authentication

Register or log in through `/api/auth/**`. Both endpoints return a JWT. Send
the token on protected requests using the standard header:

```http
Authorization: Bearer <token>
```

The security policy grants access as follows:

| Area | Required role |
| --- | --- |
| `/api/auth/**` | Public |
| `/api/transactions/**` | `CUSTOMER` or `ADMIN` |
| `/api/eligibility/**` | `CUSTOMER` or `ADMIN` |
| `/api/claims/**` | `CUSTOMER` or `ADMIN` |
| `/api/benefits/**` | `ADMIN` |
| `/api/metrics/**` | `ADMIN` |
| `/api/cards/**` | `CUSTOMER` or `ADMIN` |

Example registration and login requests:

```powershell
Invoke-RestMethod -Method Post `
  -Uri http://localhost:8090/api/auth/register `
  -ContentType 'application/json' `
  -Body '{"name":"Jane Doe","email":"jane@example.com","password":"change-me","phone":"555-0100","address":"1 Main St"}'

Invoke-RestMethod -Method Post `
  -Uri http://localhost:8090/api/auth/login `
  -ContentType 'application/json' `
  -Body '{"email":"jane@example.com","password":"change-me"}'
```

Each successful registration creates one simulated virtual card. After login,
use the returned JWT to retrieve the authenticated user's cards:

```http
GET /api/cards/me
Authorization: Bearer <token>
```

The current provider is deliberately a mock for hackathon development. It
returns only safe metadata such as the network and last four digits; it never
creates or stores a real card number, expiry date, or CVV. Replace
`MockVirtualCardProvider` with an issuer implementation when a card provider is
available.

## API overview

| Resource | Representative endpoints | Purpose |
| --- | --- | --- |
| Auth | `POST /api/auth/register`, `POST /api/auth/login`, `GET /api/auth/me` | Account and token management |
| Transactions | `POST /api/transactions`, `GET /api/transactions/{id}` | Record and retrieve card transactions |
| Eligibility | `GET /api/eligibility/{txnId}`, `POST /api/eligibility/check` | Evaluate a transaction against benefit rules |
| Benefits | `GET /api/benefits`, `POST /api/benefits`, `PUT /api/benefits/{id}` | Manage available benefits (admin) |
| Claims | `POST /api/claims`, `PUT /api/claims/{id}/activate`, `PATCH /api/claims/{id}/approve` | Manage the claim lifecycle |
| Metrics | `GET /api/metrics/latest`, `POST /api/metrics/record` | View and record operational metrics (admin) |

All controller routes are prefixed with `/api`. Refer to the controller classes
under `backend/Benefits_Engine/src/main/java/com/viva/benefits_engine/controller`
for the complete endpoint and request-field definitions.
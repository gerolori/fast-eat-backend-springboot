# Fast Eat - Spring Boot Backend

<div align="center">
  <img style="max-width: 100%; max-height: 150px;" alt="Fast Eat Logo" src="https://github.com/user-attachments/assets/06b824b2-0437-4f83-89b9-bf7ab3d69bac" />
</div>

---

Spring Boot implementation of the Fast Eat food ordering and delivery tracking API. This backend serves mobile applications built with React Native and Kotlin Android.

**For complete API specification, data models, and validation requirements, see the [Architecture Repository](https://github.com/gerolori/fast-eat-architecture).**

**Quick Navigation:** [Architecture & API Spec](https://github.com/gerolori/fast-eat-architecture) • [Kotlin Android App](https://github.com/gerolori/fast-eat-kotlin) • [React Native App](https://github.com/gerolori/fast-eat-react-native)

---

[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2%2B-brightgreen?logo=springboot)](https://spring.io/projects/spring-boot)
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-15%2B-blue?logo=postgresql)](https://www.postgresql.org/)
[![Docker](https://img.shields.io/badge/Docker-Enabled-2496ED?logo=docker)](https://www.docker.com/)
[![Java](https://img.shields.io/badge/Java-17-orange?logo=openjdk)](https://openjdk.org/)

---

## Table of Contents

- [Fast Eat - Spring Boot Backend](#fast-eat---spring-boot-backend)
  - [Table of Contents](#table-of-contents)
  - [Technology Stack](#technology-stack)
  - [Connection to Thesis Work](#connection-to-thesis-work)
    - [Shared Architectural Patterns](#shared-architectural-patterns)
    - [Technology Continuity](#technology-continuity)
  - [Prerequisites](#prerequisites)
  - [Project Structure](#project-structure)
  - [Quick Start](#quick-start)
    - [Option 1: Docker (Recommended)](#option-1-docker-recommended)
    - [Option 2: Local Development](#option-2-local-development)
    - [Testing the API](#testing-the-api)
  - [Spring Boot Implementation Details](#spring-boot-implementation-details)
    - [Security Implementation](#security-implementation)
    - [Exception Handling](#exception-handling)
    - [Event-Driven Processing](#event-driven-processing)
    - [Data Layer](#data-layer)
    - [Validation](#validation)
  - [Configuration](#configuration)
    - [Spring Profiles](#spring-profiles)
    - [Environment Variables](#environment-variables)
    - [Database Configuration](#database-configuration)
  - [Docker Setup](#docker-setup)
    - [Dockerfile](#dockerfile)
    - [docker-compose.yml](#docker-composeyml)
    - [Makefile Commands](#makefile-commands)
  - [Testing](#testing)
    - [Unit Tests](#unit-tests)
    - [Integration Tests](#integration-tests)
    - [Test Coverage](#test-coverage)
  - [API Documentation](#api-documentation)
    - [Swagger UI](#swagger-ui)
    - [SpringDoc Configuration](#springdoc-configuration)
  - [Development Workflow](#development-workflow)
  - [Related Repositories](#related-repositories)
  - [License](#license)

---

## Technology Stack

| Category          | Technology                                |
| ----------------- | ----------------------------------------- |
| **Framework**     | Spring Boot 3.2+                          |
| **Language**      | Java 17 (LTS)                             |
| **Database**      | PostgreSQL 15+                            |
| **ORM**           | Spring Data JPA with Hibernate            |
| **Security**      | Spring Security 6 + JWT                   |
| **JWT Library**   | Auth0 java-jwt                            |
| **Validation**    | Jakarta Bean Validation                   |
| **Documentation** | SpringDoc OpenAPI 3.2                     |
| **Build Tool**    | Maven (multi-module)                      |
| **Utilities**     | Lombok, Jackson, MapStruct                |
| **Testing**       | JUnit 5, Mockito, AssertJ, TestContainers |
| **Container**     | Docker with multi-stage builds            |

---

## Connection to Thesis Work

This project applies patterns and technologies from my thesis project, a Human Resources Management system built with Spring Boot for enterprise HR workflows.

### Shared Architectural Patterns

| Pattern                            | Thesis                        | This Project (Fast Eat)       |
| ---------------------------------- | ----------------------------- | ----------------------------- |
| Architecture Style                 | Multi-module Maven monolith   | Multi-module Maven monolith   |
| Layer Organization                 | trigger/handler/repository    | trigger/handler/repository    |
| Domain Design                      | Contact, Employee, Application| User, Menu, Order             |
| Security                           | Custom @Admin, @HR annotations| Custom @Customer, @Admin      |
| Event Processing                   | ApplicationEventPublisher     | ApplicationEventPublisher     |
| DTO Pattern                        | Java Records (nested)         | Java Records (nested)         |
| Error Handling                     | @ControllerAdvice centralized | @ControllerAdvice centralized |
| Async Operations                   | @Scheduled cron tasks         | @Scheduled status updates     |
| Docker Strategy                    | Multi-stage builds            | Multi-stage builds            |
| Configuration Management           | Profile-based (local/docker)  | Profile-based (local/docker)  |

### Technology Continuity

- Spring Boot 3.2+ with Java 17 (LTS)
- Spring Data JPA with PostgreSQL (relational data modeling)
- Spring Security with JWT authentication
- SpringDoc OpenAPI for automatic API documentation
- Docker containerization with health checks
- JUnit 5 + Mockito for testing

---

## Prerequisites

- **Docker 24+** and Docker Compose (required)
- **Java 17** (optional, only for local development outside Docker)
- **Maven 3.9+** (optional, only for local development)
- **Git** (for cloning repository)

---

## Project Structure

This project uses a multi-module Maven architecture inspired by enterprise Spring Boot patterns:

```bash
fast-eat-backend-springboot/
├── fast-eat-api/              # REST controllers, DTOs, request/response handling
├── fast-eat-service/          # Business logic, service layer, event handling
├── fast-eat-domain/           # JPA entities, Spring Data repositories
├── fast-eat-security/         # JWT configuration, authentication filters
├── fast-eat-common/           # Shared utilities, custom exceptions, constants
├── docker-compose.yml         # Docker orchestration
├── Dockerfile                 # Multi-stage Docker build
├── Makefile                   # Development commands
├── DEVELOPMENT.md             # Implementation roadmap and guides
└── pom.xml                    # Root Maven configuration
```

**Architecture Pattern:**

- **Trigger Layer** (`fast-eat-api`) - REST controllers, HTTP request/response handling
- **Handler Layer** (`fast-eat-service`) - Business logic, validation, orchestration
- **Repository Layer** (`fast-eat-domain`) - Data access, JPA entities, database operations

For detailed architectural patterns and data models, see the [Architecture README](https://github.com/gerolori/fast-eat-architecture).

---

## Quick Start

### Option 1: Docker (Recommended)

```bash
# Clone repository
git clone https://github.com/gerolori/fast-eat-backend-springboot
cd fast-eat-backend-springboot

# Start with Docker Compose
docker-compose up

# Or use Makefile
make dev
```

**Access Points:**

- **API Base URL:** `http://localhost:8080/api/v1`
- **Swagger Documentation:** `http://localhost:8080/api/docs`
- **MailDev (optional):** `http://localhost:1080`

### Option 2: Local Development

```bash
# Ensure PostgreSQL is running locally on port 5432

# Build and run
mvn clean install
mvn spring-boot:run -pl fast-eat-api

# Or use Makefile
make run-local
```

### Testing the API

**Register a User:**

```bash
curl -X POST http://localhost:8080/api/v1/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "email": "user@example.com",
    "password": "SecurePass123!",
    "firstName": "John",
    "lastName": "Doe",
    "cardFullName": "John Doe",
    "cardNumber": "1234567812345678",
    "cardExpireMonth": 12,
    "cardExpireYear": 2028,
    "cardCVV": "123"
  }'
```

**Get Available Menus:**

```bash
curl "http://localhost:8080/api/v1/menus?lat=45.4642&lng=9.1900"
```

**Create an Order:**

```bash
curl -X POST http://localhost:8080/api/v1/orders \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_ACCESS_TOKEN" \
  -d '{
    "menuId": "menu-uuid-from-previous-call",
    "deliveryLocation": {
      "lat": 45.4642,
      "lng": 9.1900
    }
  }'
```

---

## Spring Boot Implementation Details

### Security Implementation

**Spring Security Configuration:**

- JWT-based authentication with stateless session management
- Custom security filter chain for token validation
- BCrypt password encoding with configurable strength

**Custom Authorization Annotations:**

- `@Customer` - Restricts endpoints to authenticated customer users (implemented with Spring Security SpEL)
- `@Admin` - Restricts endpoints to admin users for future restaurant management

**JWT Strategy:**

- Access tokens with 7-day expiration
- Refresh tokens with 30-day expiration
- Secure token signing with environment-specific secrets
- Token validation on every protected endpoint request

See [Security Architecture](https://github.com/gerolori/fast-eat-architecture#security-architecture) for detailed security requirements.

### Exception Handling

**Centralized Error Handling:**

- `@ControllerAdvice` annotation for global exception handling across all controllers
- `@ExceptionHandler` methods for specific exception types
- Custom exception hierarchy extending base `ApplicationException`

**Exception Types Implemented:**

- `NotFoundError` - Resource not found (404)
- `ValidationError` - Input validation failed (400)
- `ConflictError` - Business rule conflict, e.g., duplicate email or active order exists (409)
- `PaymentRequiredError` - Payment validation failed (402)
- `UnauthorizedError` - Authentication failed (401)
- `ForbiddenError` - Authorization failed (403)

All error responses follow the standardized format defined in [Error Handling Standards](https://github.com/gerolori/fast-eat-architecture#error-handling-standards).

### Event-Driven Processing

**Spring Application Events:**

- Order status changes trigger events using `ApplicationEventPublisher`
- `@EventListener` methods handle asynchronous processing for notifications and emails
- Decouples order processing logic from notification logic for better maintainability

**Async Processing:**

- `@Async` annotation configuration for non-blocking operations
- Thread pool executor configured for concurrent event handling
- Optional MailDev integration for development email testing

### Data Layer

**JPA Entity Design:**

- Entities mapped to PostgreSQL tables using JPA annotations
- Bidirectional relationships configured where appropriate
- Cascade types configured for dependent entities
- Audit fields (`createdAt`, `updatedAt`) managed with JPA lifecycle callbacks

**Spring Data JPA Repositories:**

- Repository interfaces extend `JpaRepository` for CRUD operations
- Custom query methods using Spring Data method naming conventions
- `@Query` annotations for complex queries requiring JPQL or native SQL
- Built-in pagination and sorting support

See [Data Models](https://github.com/gerolori/fast-eat-architecture#data-models) for entity schema definitions.

### Validation

**Jakarta Bean Validation:**

- `@Valid` annotation on controller request bodies triggers automatic validation
- Field-level annotations including `@Email`, `@Size`, `@Pattern`, `@NotNull`
- Custom validators for complex business rules

**Validation Layers:**

- **DTO-level validation** - Handled in controllers before service layer
- **Business rule validation** - Enforced in service layer (e.g., single active order rule)
- **Database constraint validation** - Unique constraints, foreign keys enforced by PostgreSQL

See [Validation Requirements](https://github.com/gerolori/fast-eat-architecture#validation-requirements) for complete validation rules.

---

## Configuration

### Spring Profiles

| Profile     | Purpose                      | Activation                           |
| ----------- | ---------------------------- | ------------------------------------ |
| **local**   | Local development with IDE   | `SPRING_PROFILES_ACTIVE=local`       |
| **docker**  | Docker container environment | `SPRING_PROFILES_ACTIVE=docker`      |
| **test**    | Test execution               | Automatically activated during tests |

### Environment Variables

**Spring Boot Specific:**

```bash
SPRING_PROFILES_ACTIVE=docker
SPRING_DATASOURCE_URL=jdbc:postgresql://postgres:5432/fasteat
SPRING_DATASOURCE_USERNAME=fasteat
SPRING_DATASOURCE_PASSWORD=fasteat_password
SPRING_JPA_HIBERNATE_DDL_AUTO=validate
```

**JWT Configuration:**

```bash
JWT_SECRET=your-secret-key-change-in-production
JWT_EXPIRATION=604800  # 7 days in seconds
JWT_REFRESH_EXPIRATION=2592000  # 30 days in seconds
```

**CORS Configuration:**

```bash
CORS_ORIGINS=http://localhost:19006,http://localhost:8081
```

**Logging:**

```bash
LOG_LEVEL=INFO
```

### Database Configuration

**PostgreSQL Setup:**

- Database name: `fasteat`
- Default port: 5432
- Schema migrations handled manually or via Flyway/Liquibase (optional)

**Docker Compose Database:**

- PostgreSQL 15 Alpine image for minimal footprint
- Named volume for data persistence across container restarts
- Health checks configured to ensure database availability before application startup

---

## Docker Setup

### Dockerfile

Multi-stage Docker build optimized for Spring Boot applications:

```dockerfile
# Stage 1: Build
FROM maven:3.9-eclipse-temurin-17 AS builder
WORKDIR /app
COPY pom.xml .
COPY fast-eat-*/pom.xml ./fast-eat-*/
RUN mvn dependency:go-offline
COPY . .
RUN mvn clean package -DskipTests

# Stage 2: Runtime
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY --from=builder /app/fast-eat-api/target/*.jar app.jar
RUN addgroup -S appgroup && adduser -S appuser -G appgroup
USER appuser
EXPOSE 8080
HEALTHCHECK --interval=30s --timeout=10s --retries=3 --start-period=40s \
  CMD wget --no-verbose --tries=1 --spider http://localhost:8080/actuator/health || exit 1
CMD ["java", "-jar", "app.jar"]
```

**Benefits:** 60-70% smaller image size, security-hardened with non-root user, efficient layer caching.

### docker-compose.yml

Orchestrates Spring Boot backend with PostgreSQL and optional services:

```yaml
version: '3.8'

services:
  postgres:
    image: postgres:15-alpine
    environment:
      POSTGRES_DB: fasteat
      POSTGRES_USER: fasteat
      POSTGRES_PASSWORD: fasteat_password
    volumes:
      - postgres-data:/var/lib/postgresql/data
    ports:
      - "5432:5432"
    healthcheck:
      test: ["CMD-SHELL", "pg_isready -U fasteat"]
      interval: 10s
      timeout: 5s
      retries: 5

  backend:
    build: .
    ports:
      - "8080:8080"
    environment:
      SPRING_PROFILES_ACTIVE: docker
      SPRING_DATASOURCE_URL: jdbc:postgresql://postgres:5432/fasteat
      SPRING_DATASOURCE_USERNAME: fasteat
      SPRING_DATASOURCE_PASSWORD: fasteat_password
      JWT_SECRET: ${JWT_SECRET:-dev-secret-change-in-production}
    depends_on:
      postgres:
        condition: service_healthy

volumes:
  postgres-data:
```

### Makefile Commands

```makefile
# Development
dev:          # Start development environment with docker-compose
down:         # Stop all services
logs:         # View container logs
shell:        # Open shell in backend container

# Testing
test:         # Run all tests (unit + integration)
test-unit:    # Run unit tests only
test-int:     # Run integration tests only

# Building
build:        # Build Docker images
rebuild:      # Rebuild without cache

# Cleanup
clean:        # Stop services and remove volumes
prune:        # Deep clean (images, volumes, orphans)

# Database
db-shell:     # Open PostgreSQL shell
db-migrate:   # Run database migrations (if using Flyway)
db-seed:      # Seed database with test data
```

---

## Testing

### Unit Tests

**Testing Framework:**

- JUnit 5 (Jupiter) for test execution and assertions
- Mockito for mocking service dependencies
- AssertJ for fluent, readable assertions

**Focus Areas:**

- Service layer business logic validation
- Order status state machine transitions
- JWT token generation and validation
- Custom validation logic

**Running Unit Tests:**

```bash
mvn test
# or
make test-unit
```

### Integration Tests

**Testing Framework:**

- `@SpringBootTest` annotation for full Spring application context
- TestContainers for real PostgreSQL database in tests
- MockMvc for testing controller endpoints without starting server

**Focus Areas:**

- Complete API request/response cycles through controllers
- Database operations with real PostgreSQL instance
- Authentication flows (register, login, token refresh)
- Transaction management and rollback scenarios

**Running Integration Tests:**

```bash
mvn verify
# or
make test-int
```

### Test Coverage

For coverage goals and critical test scenarios, see [Testing Requirements](https://github.com/gerolori/fast-eat-architecture#testing-requirements).

**Generate Coverage Report:**

```bash
mvn clean verify jacoco:report
# Report available at: target/site/jacoco/index.html
```

---

## API Documentation

### Swagger UI

Interactive API documentation auto-generated using SpringDoc OpenAPI:

**Access:** `http://localhost:8080/api/docs`

**Features:**

- Browse all endpoints with request/response schemas
- Test API calls directly from browser interface
- View authentication requirements for each endpoint
- Download OpenAPI specification (JSON/YAML format)

### SpringDoc Configuration

- OpenAPI 3.0.3 specification compliance
- JWT bearer authentication scheme documented
- Custom API metadata (title, version, description)
- Tag-based endpoint organization for better navigation

For complete API specification, see [openapi.yaml](https://github.com/gerolori/fast-eat-architecture/blob/main/api/openapi.yaml).

---

## Development Workflow

For detailed implementation roadmap, development best practices, module-by-module implementation guides, and debugging tips, see [DEVELOPMENT.md](./DEVELOPMENT.md).

**Quick Links:**

- Module implementation guide (domain, service, api, security, common)
- Git workflow and commit conventions
- Code review checklist
- Database management
- Debugging in Docker
- Performance optimization

---

## Related Repositories

- **[Architecture & API Specification](https://github.com/gerolori/fast-eat-architecture)** - API contracts, data models, validation rules, testing requirements
- **[Kotlin Android App](https://github.com/gerolori/fast-eat-kotlin)** - Native Android client application
- **[React Native App](https://github.com/gerolori/fast-eat-react-native)** - Cross-platform Expo client application

---

## License

This project is for educational and portfolio purposes.

**Academic Context:**

- **University:** Università degli Studi di Milano
- **Course:** Mobile Computing 2024/25
- **Thesis Reference:** HRM "Airing" Application (enterprise Spring Boot patterns)

---

*For questions or issues, please open an issue in the GitHub repository.*

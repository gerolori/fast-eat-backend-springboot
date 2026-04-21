# Fast Eat - Spring Boot Backend

<div align="center">
  <img style="max-width: 100%; max-height: 150px;" alt="Fast Eat Logo" src="https://github.com/user-attachments/assets/06b824b2-0437-4f83-89b9-bf7ab3d69bac" />
</div>

---

Spring Boot backend for the Fast Eat mobile clients.

**Companion repositories:**
- [Architecture & API specification](https://github.com/gerolori/fast-eat-architecture)
- [Kotlin Android app](https://github.com/gerolori/fast-eat-kotlin)
- [React Native app](https://github.com/gerolori/fast-eat-react-native)

---

[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-4.0.3-brightgreen?logo=springboot)](https://spring.io/projects/spring-boot)
[![Java](https://img.shields.io/badge/Java-17-orange?logo=openjdk)](https://openjdk.org/)
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-15%2B-blue?logo=postgresql)](https://www.postgresql.org/)
[![Docker](https://img.shields.io/badge/Docker-Baseline-2496ED?logo=docker)](https://www.docker.com/)

## Current publish-state baseline

This repository is currently a **single-module** Spring Boot application (not a multi-module Maven build).

### Technology stack

| Category | Current implementation |
| --- | --- |
| Framework | Spring Boot 4.0.3 |
| Language | Java 17 |
| Build tool | Maven Wrapper (`mvnw`, `mvnw.cmd`) |
| API | Spring Web MVC |
| Data | Spring Data JPA + PostgreSQL |
| Security | Spring Security + JWT (`io.jsonwebtoken`) |
| API docs | SpringDoc OpenAPI (`/v3/api-docs`, Swagger UI) |
| Testing | JUnit 5 + Spring Boot Test + H2 test profile |
| Quality gates | Spotless, Checkstyle, SpotBugs, JaCoCo (wired to `verify`) |
| Container runtime | Dockerfile + `docker-compose.yml` baseline |

## Project structure

```text
fast-eat-backend-springboot/
├── src/
│   ├── main/java/com/gerolori/fasteat/
│   ├── main/resources/application*.properties
│   └── test/java/com/gerolori/fasteat/
├── .github/workflows/ci-baseline.yml
├── docker-compose.yml
├── Dockerfile
├── pom.xml
├── mvnw
├── mvnw.cmd
├── DEVELOPMENT.md
└── README.md
```

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

- Java 17
- Docker + Docker Compose plugin (`docker compose`) for containerized startup
- Git

## Run locally (without Docker)

1. Ensure PostgreSQL is available (defaults in `application-local.properties` target `localhost:5432/fasteat`).
2. Run:

```powershell
./mvnw.cmd spring-boot:run
```

Default local profile is `local` (`spring.profiles.default`).

## Run with Docker

```powershell
docker compose up --build
```

This baseline compose stack includes:
- `backend` (built from local `Dockerfile`)
- `postgres` (`postgres:15-alpine`)

## Runtime URLs

- API base: `http://localhost:8080`
- OpenAPI JSON: `http://localhost:8080/v3/api-docs`
- Swagger UI: `http://localhost:8080/swagger-ui/index.html`

## API surface (current route prefixes)

- `/auth`
- `/users`
- `/menus`
- `/orders`
- `/restaurants`
- `/admin/*`

There is currently **no global `/api/v1` context path** configured.

## Configuration

### Profiles

- `local` (default): `application-local.properties`
- `docker`: `application-docker.properties`
- `test`: `application-test.properties`

### Key environment variables

The active configuration uses the `FASTEAT_*` contract, for example:

- `FASTEAT_DB_URL`
- `FASTEAT_DB_USERNAME`
- `FASTEAT_DB_PASSWORD`
- `FASTEAT_JPA_DDL_AUTO`
- `FASTEAT_JWT_SECRET`
- `FASTEAT_JWT_EXPIRATION_MS`
- `FASTEAT_JWT_ISSUER`

See `src/main/resources/application*.properties` for profile-specific defaults.

## CI, testing, and quality

### GitHub Actions baseline

Workflow: `.github/workflows/ci-baseline.yml`

- Runner: `windows-latest`
- Java: Temurin 17
- CI command: `./mvnw.cmd verify`

### Local verification commands

```powershell
./mvnw.cmd test
./mvnw.cmd verify
./mvnw.cmd jacoco:report
```

`verify` is the main quality gate and includes:
- unit + integration test phases (Surefire + Failsafe)
- Spotless check
- Checkstyle check
- SpotBugs check
- JaCoCo report + minimum coverage rule

## Docker baseline details

- Docker image build uses a multi-stage Dockerfile (`maven:3.9.11-eclipse-temurin-17` -> `eclipse-temurin:17-jre-jammy`).
- Container profile is set to `docker`.
- Compose file currently targets backend + PostgreSQL only.
- Redis is noted as a **future upgrade path** in `docker-compose.yml` comments and is not part of the running baseline.

## Future-state notes (not yet implemented here)

The following are roadmap items, not current baseline guarantees:
- multi-module split/extraction
- expanded CI matrix beyond the baseline verify job
- additional container services (for example Redis) once finalized

## License

This project is for educational and portfolio purposes (University of Milan, Mobile Computing course).

# fast-2xm.1 — Container runtime assumptions and environment contract

## Current container runtime assumptions

- Backend runs as a single Spring Boot process in one container.
- Primary datastore is PostgreSQL (single instance service in Docker Compose).
- Runtime configuration is environment-driven through `FASTEAT_*` variables.
- Docker profile should disable local-only bootstrap behaviors by default.

## Environment contract (backend)

- `SPRING_PROFILES_ACTIVE` (set to `docker` in Compose)
- `FASTEAT_DB_URL`
- `FASTEAT_DB_USERNAME`
- `FASTEAT_DB_PASSWORD`
- `FASTEAT_DB_DRIVER` (optional, defaults to PostgreSQL driver)
- `FASTEAT_JPA_DDL_AUTO` (optional, runtime schema mode)
- `FASTEAT_JPA_SHOW_SQL` (optional, SQL logging)
- `FASTEAT_APP_LOG_LEVEL` (optional, app log level)
- `FASTEAT_JWT_SECRET`
- `FASTEAT_JWT_EXPIRATION_MS` (optional)
- `FASTEAT_JWT_ISSUER` (optional)
- `FASTEAT_SEED_DEMO_ENABLED` (optional, defaults false for docker profile)
- `FASTEAT_ADMIN_BOOTSTRAP_ENABLED` (optional, defaults false for docker profile)
- `FASTEAT_ADMIN_BOOTSTRAP_EMAILS` (optional)
- `FASTEAT_ADMIN_BOOTSTRAP_CREATE_MISSING_USERS` (optional)
- `FASTEAT_ADMIN_BOOTSTRAP_PASSWORD` (optional)

## Deferred path (not in this lane)

- Redis remains a future upgrade path for caching/session concerns.
- Redis is intentionally **not** added to the current Compose stack.

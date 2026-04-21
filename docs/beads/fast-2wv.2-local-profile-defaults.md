# fast-2wv.2 local profile defaults

## Purpose

This bead establishes a minimal, single-module-safe baseline for runtime configuration split by profile.

## Profile intent

- `application.properties` (shared baseline)
  - Common defaults for app identity, profile default behavior, JPA/logging baseline, and placeholder JWT keys.
- `application-local.properties`
  - Local IDE/dev defaults for PostgreSQL on `localhost` with env-overridable values.
- `application-test.properties`
  - Test-safe overrides with an in-memory H2 datasource and short-lived JWT defaults.

## Activation expectations

- Default profile is `local` unless `SPRING_PROFILES_DEFAULT` is provided.
- Local development can run with no extra profile flag and optional env overrides.
- Test profile is expected to be activated explicitly (for example via `SPRING_PROFILES_ACTIVE=test` or test annotations/config).

## Env-backed settings in this baseline

- Datasource: `FASTEAT_DB_URL`, `FASTEAT_DB_USERNAME`, `FASTEAT_DB_PASSWORD`, `FASTEAT_DB_DRIVER`
- JPA local tuning: `FASTEAT_JPA_DDL_AUTO`, `FASTEAT_JPA_SHOW_SQL`
- Logging local tuning: `FASTEAT_APP_LOG_LEVEL`, `FASTEAT_SQL_LOG_LEVEL`
- JWT placeholders: `FASTEAT_JWT_SECRET`, `FASTEAT_JWT_EXPIRATION_MS`, `FASTEAT_JWT_ISSUER`
- Test JWT placeholders: `FASTEAT_TEST_JWT_SECRET`, `FASTEAT_TEST_JWT_EXPIRATION_MS`, `FASTEAT_TEST_JWT_ISSUER`

## Intentionally deferred

- Production and docker-specific profile files
- Strict configuration validation classes
- Secret management hardening (Vault/SOPS/KMS)
- Full security/auth behavior wiring beyond placeholder JWT settings

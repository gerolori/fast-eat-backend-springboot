# fast-2wv.4 developer bootstrap path and smoke checks

## Purpose

Provide a minimal, truthful onboarding path for contributors based on the current single-module backend baseline.

This document is intentionally scoped to local bootstrap and sanity checks only.

## Prerequisites (current repo reality)

- Java 17 (from `pom.xml` property `java.version=17`)
- Maven Wrapper usage from repo root (`mvnw.cmd` on Windows)
- Local PostgreSQL reachable for `local` profile defaults (`application-local.properties`)
  - Default URL: `jdbc:postgresql://localhost:5432/fasteat`
  - Default username/password: `postgres` / `postgres`
- No additional module setup is required (current baseline is single-module)

## Profile assumptions to know before running

- `application.properties` sets `spring.profiles.default=${SPRING_PROFILES_DEFAULT:local}`.
- If no profile is passed, runtime defaults to `local`.
- `test` profile is isolated in `application-test.properties` (H2 in-memory datasource) and is activated explicitly by tests such as `FasteatApplicationTests` via `@ActiveProfiles("test")`.

## Minimal local startup path

From repository root:

1. Ensure PostgreSQL is running and accessible.
2. (Optional) override DB/JPA/logging/JWT placeholder values with environment variables from the `FASTEAT_*` keys documented in `fast-2wv.2`.
3. Start application with local defaults:

```powershell
.\mvnw.cmd spring-boot:run
```

Notes:

- This uses the default `local` profile unless `SPRING_PROFILES_DEFAULT` or `SPRING_PROFILES_ACTIVE` is overridden.
- Keep this as bootstrap only; do not assume specific API endpoints are implemented.

## Narrow smoke checks for contributors

Run the smallest checks that validate bootstrap health today:

1. Build/package sanity

```powershell
.\mvnw.cmd -DskipTests package
```

2. Spring context load sanity on `test` profile

```powershell
.\mvnw.cmd -Dtest=FasteatApplicationTests test
```

3. Profile sanity expectations

- Local run (`spring-boot:run`) should resolve to `local` defaults when no profile flag is provided.
- The `FasteatApplicationTests` smoke test should load context with `test` profile and H2 settings.

4. Beads workspace sanity (tracking state)

```powershell
br list --no-db --no-auto-flush
br count --no-db --no-auto-flush --by-type
```

## Explicit non-goals

- This document does not promise endpoint availability or feature completeness.
- This document does not redefine architecture or module split plans.

# fast-2wv.3 foundational configuration boundaries

## Purpose

Define a stable, single-module-safe ownership model for foundational configuration after `fast-2wv.2`, so future config/security work can evolve without scattering keys or profile behavior.

## Canonical configuration domains and prefixes

Use these as the canonical boundaries for the current single-module backend:

- **Application identity and profile behavior**: `spring.application.*`, `spring.profiles.*`
- **Datasource baseline**: `spring.datasource.*`
- **JPA/Hibernate baseline**: `spring.jpa.*`
- **Logging baseline**: `logging.level.*`
- **JWT/security placeholders (app namespace)**: `fasteat.security.jwt.*`

Environment variable bridge convention (already used in `application*.properties`):

- `FASTEAT_*` for app-specific env-backed values
- `SPRING_PROFILES_*` for Spring profile selection

## Ownership by file/profile

### Shared baseline (`application.properties`)

Owns cross-profile defaults and key declarations:

- app name and default profile wiring
- shared datasource key presence (without forcing local host assumptions)
- conservative JPA/logging defaults
- placeholder JWT keys under `fasteat.security.jwt.*`

Rule: this file defines **global defaults/contracts**, not developer-machine-specific tuning.

### Local profile (`application-local.properties`)

Owns developer-safe convenience overrides only:

- localhost PostgreSQL defaults
- local JPA/logging tuning (`ddl-auto`, SQL visibility, app log verbosity)

Rule: local profile may be opinionated for productivity, but must remain env-overridable.

### Test profile (`application-test.properties`)

Owns deterministic test runtime behavior:

- in-memory datasource (H2)
- isolated JPA behavior (`create-drop`)
- reduced/noisy logging controls
- test JWT placeholders via dedicated `FASTEAT_TEST_*` variables

Rule: test profile must be hermetic and independent from local/dev infrastructure.

## Fallback policy (safe defaults vs eventually required inputs)

### Allowed local-safe fallbacks now

- `spring.profiles.default` fallback to `local`
- local datasource defaults in `application-local.properties`
- local/test JWT placeholders intended only for non-production use
- non-sensitive logging/JPA convenience defaults

### Should become required secret/runtime input (future hardening)

- production datasource credentials/connection values
- production JWT signing secret/key material
- any environment-specific issuer/audience/signing parameters used for real auth trust boundaries

Rule: production-like profiles should avoid embedded secret fallbacks; values must come from runtime secret/config providers.

## Centralization direction for future config code (single module)

Keep configuration code centralized under the existing single-module package root:

- package direction: `com.gerolori.fasteat.config`
- bind app-owned namespaces (starting with `fasteat.security.jwt.*`) via typed `@ConfigurationProperties`
- keep validation/defaulting logic in one config area instead of duplicating value parsing in services/controllers
- use profile files for environment shape, and typed properties classes for app-level contracts

This preserves single-module structure while preparing low-churn extraction later.

## Alignment with `fast-2wv.2`

This boundary document keeps `fast-2wv.2` decisions intact:

- shared baseline + local/test profile split remains authoritative
- existing env variable names remain valid (`FASTEAT_DB_*`, `FASTEAT_JPA_*`, `FASTEAT_*_LOG_LEVEL`, `FASTEAT_JWT_*`, `FASTEAT_TEST_JWT_*`)
- no new profile files or module split introduced in this bead

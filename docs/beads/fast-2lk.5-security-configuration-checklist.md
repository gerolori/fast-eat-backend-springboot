# fast-2lk.5 — Security Configuration Checklist (S1.5)

This bead defines an implementation-ready **local/dev security configuration checklist** for the upcoming Spring Security setup work.

It aligns with:

- `docs/beads/fast-2lk.1-auth-flow-baseline.md`
- `docs/beads/fast-2lk.2-authorization-model.md`
- current profile/config baselines in `src/main/resources/application*.properties`

No security classes are implemented in this bead.

## Scope assumptions (local/dev baseline)

- [ ] Baseline is for `local` dev profile defaults and current test profile behavior.
- [ ] JWT settings remain env-overridable placeholders (`fasteat.security.jwt.*`) until later hardening work.
- [ ] Configuration must avoid breaking existing startup defaults (`spring.profiles.default=local`).
- [ ] Production-grade key management, rotation, and external secret managers are explicitly out of scope here.

## Endpoint access checklist (baseline direction)

### A) Public path baseline

- [ ] Allow unauthenticated access to `/auth/**` endpoints (S1.1 login entrypoint direction).
- [ ] Allow unauthenticated read access for menu discovery endpoints (S1.2):
  - [ ] `GET /menu`
  - [ ] `GET /menu/{itemId}`
- [ ] Keep public exposure intentionally narrow; do not add broad wildcard public access.

### B) Authenticated path baseline

- [ ] Require authenticated principal for `/users/me`.
- [ ] Require authenticated principal for customer order endpoints (`/orders/**`) with ownership checks enforced in business/controller layers.
- [ ] Use resolved principal identity (`userId`-equivalent) as source of truth; do not trust caller-supplied user IDs for self-scope endpoints.

### C) Admin-only direction

- [ ] Reserve management/privileged endpoints for `ADMIN` role checks.
- [ ] Treat admin pathing as explicit and auditable (e.g., management endpoints under dedicated route groups).
- [ ] Do not allow role escalation via request payload/header values; role source must come from authenticated context.

## Authentication mechanism checklist

- [ ] Password verification baseline uses `BCryptPasswordEncoder` (compatible with S1.1 bcrypt direction).
- [ ] No plaintext password comparison is allowed.
- [ ] JWT bearer authentication uses `Authorization: Bearer <token>` parsing for protected APIs.
- [ ] Security config should enforce stateless API behavior (sessionless direction) for JWT-backed endpoints.

## JWT settings checklist (placeholder-safe direction)

- [ ] Consume JWT properties from config keys already present:
  - [ ] `fasteat.security.jwt.secret`
  - [ ] `fasteat.security.jwt.expiration-ms`
  - [ ] `fasteat.security.jwt.issuer`
- [ ] Maintain env override behavior for local/test (`FASTEAT_JWT_*`, `FASTEAT_TEST_JWT_*`).
- [ ] Reject empty/invalid JWT secret at startup in future implementation work (validation requirement captured now).
- [ ] Keep `local-dev-jwt-secret-change-me` and test fallback values as non-production placeholders only.

## Logging hygiene checklist

- [ ] Do not log raw credentials, password hashes, JWT secrets, or full JWT token values.
- [ ] Keep security/auth logging at info/warn/error levels suitable for local troubleshooting without sensitive leakage.
- [ ] If token context is logged, log minimal identifiers/claims needed for diagnostics (never secret material).
- [ ] Keep compatibility with current logging baselines in `application.properties`, `application-local.properties`, and `application-test.properties`.

## CORS direction checklist (local/dev)

- [ ] Define explicit allowed origins for local UI/client development instead of permissive `*` defaults.
- [ ] Allow only required methods/headers for MVP client flows.
- [ ] Ensure `Authorization` header is permitted for bearer token requests from approved origins.
- [ ] Keep CORS policy profile-aware so local/dev convenience does not imply production openness.

## Implementation handoff checklist for follow-up security config task

- [ ] Map the access buckets above into concrete Spring Security route rules.
- [ ] Wire password encoder and authentication provider/manager with BCrypt-based verification.
- [ ] Wire JWT filter/resolver to establish authenticated principal context for protected routes.
- [ ] Add startup validation for required JWT settings in non-test profiles.
- [ ] Add focused security tests covering:
  - [ ] public endpoint allow-list behavior
  - [ ] authenticated endpoint enforcement
  - [ ] admin-only denial for non-admin principal
  - [ ] ownership guard behavior for order/user scoped access

## Out of scope / deferred

- Detailed JWT claim schema and refresh semantics (S1.3 scope)
- Security/auth error response contract shape (S1.4 scope)
- Full production hardening and secret management rollout

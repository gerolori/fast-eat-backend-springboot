# fast-3q7.5 — Platform Configuration Review Checklist (P1.5)

This bead captures a lightweight configuration review checklist for local/dev/test readiness.

## Checklist

### Runtime and profile basics

- [ ] `application.properties` keeps safe defaults (`INFO` logging baseline).
- [ ] `application-local.properties` overrides are dev-friendly and non-secret.
- [ ] `application-test.properties` is deterministic for CI/test usage.
- [ ] Active profile selection is explicit in deployment/runtime docs.

### Security-sensitive config

- [ ] JWT/secret values are env-injected and never hardcoded in tracked files.
- [ ] Error messages avoid leaking secret/token internals.
- [ ] Protected endpoints remain aligned with S1.2 authz buckets.

### API contract consistency

- [ ] `/users/me` remains principal-scoped.
- [ ] Menu contract naming is canonical (`/menus`, `/menus/{menuId}`).
- [ ] Shared menu fields stay stable (`menuId`, `summary`, `price.amount`, `price.currency`).
- [ ] Order statuses use canonical enum set from D1.6.

### Logging and diagnostics

- [ ] `X-Correlation-Id`/`correlationId` convention is preserved.
- [ ] Error envelopes can expose `traceId` mapped to correlation ID.
- [ ] Log level overrides are intentional and bounded.

### Build and test hygiene

- [ ] `mvnw.cmd test` passes locally.
- [ ] `mvnw.cmd verify` passes before release-oriented merges.
- [ ] No accidental config drift from docs-only work.

## Usage guidance

- Use this checklist for PR self-review and release readiness checks.
- Keep it lightweight; detailed controls can evolve in follow-up platform/security beads.

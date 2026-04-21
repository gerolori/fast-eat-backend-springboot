# fast-2lk.6 — Security Hardening Backlog (Post-Baseline) (S1.6)

This bead captures **intentional security deferrals** after the S1 baseline (`S1.1` through `S1.5`).

It exists to keep the MVP baseline focused while preserving a clear, actionable backlog for the next hardening phase.

## Baseline vs post-baseline boundary

### Baseline requirements (already in scope for S1.1–S1.5)

- Credential login under `/auth/...` with authenticated access tokens.
- BCrypt-based password verification.
- Role-aware authorization baseline (`CUSTOMER` / `ADMIN`) and ownership checks.
- JWT usage with baseline lifecycle decisions needed for MVP operation.
- Security/error contract and baseline security checklist direction.

The above are considered **baseline-enabling** and should not be expanded in S1.6.

## Post-baseline hardening backlog (deferred intentionally)

The following items are deferred to protect baseline delivery scope, but should be scheduled as dedicated follow-up work.

### 1) Secret and key rotation program

- Define operational cadence and procedure for rotating JWT signing secrets/keys and other auth-sensitive configuration.
- Support safe dual-key/overlap window behavior during rotation to avoid forced downtime.
- Add runbook steps for emergency rotation after suspected secret exposure.

### 2) Token revocation and refresh hardening

- Introduce revocation strategy for compromised sessions/tokens (deny-list or version-based invalidation approach).
- Strengthen refresh-token handling (rotation, replay protection, expiry policy, device/session binding where appropriate).
- Define forced logout semantics for password change, account disable, and admin-driven revoke actions.

### 3) Rate limiting and abuse controls

- Add endpoint-aware throttling, starting with `/auth/login` and other high-risk auth endpoints.
- Add anti-enumeration behavior (uniform response patterns, lockout/backoff strategy).
- Define IP/user/device level controls and safe fallback behavior during burst traffic.

### 4) Audit logging and security event trail

- Log high-value security events (login success/failure, token refresh/revocation, role/privilege changes, account lock/unlock).
- Ensure logs include actor, timestamp, target, and result with correlation IDs.
- Apply retention/redaction guidance to avoid leaking sensitive payload data.

### 5) Stricter validation and input hardening

- Tighten request validation for auth and user/account flows (length, format, canonicalization, boundary checks).
- Standardize rejection of malformed/oversized headers and token inputs.
- Add negative tests for validation bypass and parser edge cases.

### 6) Secure operations handling

- Document production-safe handling of secrets (no secrets in VCS, controlled injection path, least-privilege access).
- Define environment-specific hardening checks (dev/stage/prod) for auth-related config.
- Add operational checks for secure defaults (HTTPS assumptions at edge, secure header expectations, clock skew controls).

### 7) Observability and alerting for auth/security anomalies

- Add metrics for auth failures, token refresh failures, throttle events, and access-denied trends.
- Add actionable alert thresholds for suspicious spikes and repeated abuse patterns.
- Ensure dashboards support incident triage without exposing confidential values.

## Follow-up execution guidance

- Implement backlog items as **separate beads/tasks** to keep review and risk bounded.
- Prioritize in this order unless risk analysis dictates otherwise:
  1. Secret/key rotation
  2. Token revocation/refresh hardening
  3. Rate limiting on auth endpoints
  4. Audit logging baseline
  5. Validation hardening and secure ops checks
- For each follow-up task, include explicit acceptance criteria and rollback notes.

## Out of scope for S1.6

- Changing existing baseline auth/authorization contracts.
- Introducing major security architecture redesign in this bead.
- Expanding beyond backlog definition and prioritization guidance.

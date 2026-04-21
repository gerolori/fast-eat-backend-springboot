# fast-kql.2 — Profile Validation and Error Semantics (A1.2)

This bead defines the API-contract semantics for **current-user profile updates** under `/users/me`.

It is a follow-on to:

- auth baseline in `docs/beads/fast-2lk.1-auth-flow-baseline.md`
- authorization model in `docs/beads/fast-2lk.2-authorization-model.md`
- security error contract in `docs/beads/fast-2lk.4-security-error-handling-contract.md`
- user invariants in `docs/beads/fast-2p4.4-user-domain-constraints-and-invariants.md`

This document is contract-level only. It does **not** define controller/service implementation.

## Scope and intent

- Define validation semantics for mutable profile updates in `/users/me` flows.
- Define behavior for unknown/restricted fields in request payloads.
- Make error-category boundaries explicit between validation, authentication, and authorization.
- Keep current-user update behavior compatible with principal-derived identity and domain invariants.

## Endpoint contract scope

This bead applies to update operations that mutate the authenticated caller's own profile (for example `PATCH /users/me`).

Identity source rule (normative):

- Target user identity is always resolved from authenticated principal context.
- Caller-supplied identity selectors in payload (for example `id`, `userId`, `role`) are never used to select target user.

## Mutable-field validation rules

Profile updates are **allowlist-based**.

### 1) Field mutability classes

- **Allowed mutable fields**: non-sensitive profile display/contact attributes only.
- **Restricted/immutable fields**: identity, role/security, and system-managed metadata.

Restricted/immutable examples (non-exhaustive):

- identity: `id`, `userId`
- role/authorization: `role`, `roles`, `authorities`
- security credentials: `password`, `passwordHash`
- account governance/status: `status`, `enabled`, `locked`
- system metadata: `createdAt`, `updatedAt`

### 2) Payload-level validation

- Request body must be a valid JSON object.
- Payload must include at least one allowed mutable field.
- Duplicate keys or structurally malformed JSON are invalid.

### 3) Value-level validation (for mutable fields)

- Values must match declared field type.
- String values are evaluated after trim; blank-after-trim values are invalid unless field explicitly allows empty.
- Length constraints must be enforced per mutable field contract.
- Contact/format-sensitive fields (email/phone/url-style values, where present in allowlist) must pass canonical format checks.
- Explicit `null` is only allowed where field contract marks nullability as supported.

## Unknown/restricted field behavior

To prevent silent anti-patterns and privilege confusion, update payload handling is strict:

- **Unknown fields** (not in update DTO contract) are rejected.
- **Restricted fields** (known but not mutable in self-service contract) are rejected.
- Requests mixing valid mutable fields with unknown/restricted fields are rejected as a whole (no partial apply).

Recommended stable validation categories:

- `PROFILE_UNKNOWN_FIELD`
- `PROFILE_RESTRICTED_FIELD`
- `PROFILE_INVALID_VALUE`
- `PROFILE_EMPTY_UPDATE`

## Error-category boundaries

The following separation is required for predictable client behavior.

| Category | Typical status | Category examples | Meaning |
| --- | --- | --- | --- |
| Authentication failure | `401` | `AUTH_MISSING_TOKEN`, `AUTH_INVALID_TOKEN`, `AUTH_TOKEN_EXPIRED`, `AUTH_MALFORMED_TOKEN` | Caller identity is absent/untrusted. |
| Authorization/ownership failure | `403` | `AUTHZ_INSUFFICIENT_ROLE`, `AUTHZ_OWNERSHIP_DENIED` | Caller is authenticated but policy denies operation. |
| Validation/contract failure | `400` or `422` | `PROFILE_UNKNOWN_FIELD`, `PROFILE_RESTRICTED_FIELD`, `PROFILE_INVALID_VALUE`, `PROFILE_EMPTY_UPDATE` | Caller is authenticated/authorized, but payload violates update contract. |

### Status usage direction for validation

- Use `400 Bad Request` for malformed request shape/protocol issues (invalid JSON, unsupported payload structure).
- Use `422 Unprocessable Entity` for semantically well-formed payloads that fail field-level validation rules.

If project implementation standardizes on one status for all validation failures, keep category semantics stable and document the chosen convention consistently.

## Error response shape alignment

For authentication/authorization errors, keep response envelope aligned with S1.4.

For validation errors, use the same baseline envelope and add field-level details when available:

- `error` (stable category)
- `message` (safe summary)
- `status`
- `path`
- `timestamp`
- `traceId` (optional, recommended)
- `details` (optional): list of per-field violations for client correction UX

Validation `details` direction (example):

```json
[
  { "field": "displayName", "reason": "must not be blank" },
  { "field": "phone", "reason": "invalid format" }
]
```

## Invariant-preservation requirements

Profile updates must preserve D1.4/S1.2 invariants:

- no identity mutation through self-service profile updates
- no role escalation or role mutation through client payload
- no password/security-credential mutation through generic profile update path
- principal-derived ownership is always authoritative for target user resolution

## Explicit deferrals

- Final DTO field list and exact per-field min/max values are finalized in implementation-facing API beads.
- Framework exception mapping and handler wiring are implementation details.
- Concurrency/versioning strategy remains out of scope for this contract bead.

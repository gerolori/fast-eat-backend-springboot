# fast-2lk.2 — Authorization Model for Customer/Admin Scopes (S1.2)

This bead defines the **MVP authorization contract direction** that extends the S1.1 authentication baseline in `docs/beads/fast-2lk.1-auth-flow-baseline.md`.

## Scope and intent

- Establish a minimal role model for MVP.
- Define endpoint access buckets to guide upcoming security configuration and controller-level checks.
- Define ownership and anti-escalation rules for user and order access.

This document is a policy/contract reference only. It does **not** implement Spring Security classes.

## Baseline MVP roles

- **`CUSTOMER`**: default role for end users using app-facing APIs.
- **`ADMIN`**: privileged operational role for management and backoffice behavior.

No additional roles/scopes are introduced in S1.2.

## Endpoint access buckets (baseline)

### 1) Public auth endpoints

Unauthenticated access is allowed for authentication entry points.

Examples:

- `POST /auth/login`
- other `/auth/...` endpoints needed for credential-based sign-in flow

### 2) Public / low-friction menu read endpoints

Menu discovery/read endpoints are publicly readable in MVP to reduce user friction.

Examples:

- `GET /menu`
- `GET /menu/{itemId}`

### 3) Authenticated self-service endpoints

Endpoints that return or update current-user data require an authenticated principal.

Examples:

- `GET /users/me`
- future self-profile updates under current-user context

Access rule: caller must be authenticated as at least `CUSTOMER` (or `ADMIN`).

### 4) Customer-owned order operations

Order operations are user-scoped. Authenticated callers may access only orders they own unless they have admin privileges.

Examples:

- `POST /orders` (create order as current user)
- `GET /orders/{orderId}` (only if order belongs to caller)
- `GET /orders/me` (list current user orders)
- `PATCH /orders/{orderId}` (only allowed state changes and only for owner)

### 5) Admin-only future privileged endpoints

Privileged operational endpoints are reserved for `ADMIN` only.

Examples:

- menu management (create/update/delete menu items)
- user-management views/actions
- broader order-management or override operations

## Ownership rules

- User-owned resources must be bound to the authenticated user identifier from the principal context.
- `/users/me` style endpoints must resolve target user from principal identity, not from caller-supplied user IDs.
- Order reads/updates by non-admin callers must enforce `order.ownerUserId == principal.userId`.

## Anti-escalation rules

- Clients must not self-assign or elevate role via request payloads.
- Role decisions must come from trusted authenticated identity, not arbitrary headers/body fields.
- Non-admin users cannot access admin-only endpoints even if they supply admin-like query/body values.
- User ID/path tampering cannot bypass ownership checks for user/order resources.

## Principal requirements for authorization decisions

To support this authorization model, resolved authentication principal/context must provide at least:

- stable authenticated user identifier (`userId`-equivalent)
- effective role set containing `CUSTOMER` and/or `ADMIN`
- authenticated state usable by route/security guards

These are minimum requirements for authorization checks; exact token-claim structure is out of scope here.

## Explicit deferrals

- **Token claim shape, claim naming, token lifecycle/expiration details** → deferred to **S1.3**
- **Security/auth error response contract (status codes, error body structure, messages)** → deferred to **S1.4**

## Implementation guidance for later tasks

- Treat these buckets as the baseline source when implementing security configuration and endpoint guards.
- Keep endpoint-level authorization explicit and ownership checks close to order/user access paths.
- Preserve compatibility with S1.1 identity baseline (`/auth/...`, `/users/me`, principal-derived identity).

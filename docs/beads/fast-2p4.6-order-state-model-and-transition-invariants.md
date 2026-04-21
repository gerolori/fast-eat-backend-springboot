# fast-2p4.6 — Order State Model and Transition Invariants (D1.6)

This bead defines the **order lifecycle contract** for MVP and follows these baselines:

- domain inventory and persistence direction in `fast-2p4.1` to `fast-2p4.3`
- authorization ownership model in `docs/beads/fast-2lk.2-authorization-model.md`
- existing menu/domain constraint direction where order content is derived from published menu data and ingredient availability boundaries (`fast-2p4.1`)

Scope is intentionally contract-only (no entity fields, repository code, service code, or endpoint implementation).

## Allowed lifecycle states (MVP)

`OrderStatus` for MVP is constrained to:

- `PENDING` — order is accepted by backend but not yet actively prepared.
- `CONFIRMED` — order is accepted for fulfillment after validation/availability checks.
- `PREPARING` — order is in active preparation.
- `READY_FOR_PICKUP` — order is prepared and awaiting customer pickup/handoff.
- `COMPLETED` — order lifecycle is finished successfully.
- `CANCELLED` — order lifecycle is terminated before completion.

No additional parallel/terminal states are introduced in D1.6.

## State category invariants

- **Active states:** `PENDING`, `CONFIRMED`, `PREPARING`, `READY_FOR_PICKUP`
- **Terminal states:** `COMPLETED`, `CANCELLED`

Invariant direction:

- Once an order reaches a terminal state, it must not transition to any other state.
- `COMPLETED` and `CANCELLED` are mutually exclusive outcomes for a single order.

## Valid transition model

Allowed forward transitions are:

- `PENDING -> CONFIRMED`
- `PENDING -> CANCELLED`
- `CONFIRMED -> PREPARING`
- `CONFIRMED -> CANCELLED`
- `PREPARING -> READY_FOR_PICKUP`
- `PREPARING -> CANCELLED` (only for exceptional operational abort paths)
- `READY_FOR_PICKUP -> COMPLETED`

No other transitions are valid.

## Forbidden transition invariants

The following transition classes are forbidden:

- **Backward transitions** (for example `PREPARING -> CONFIRMED`, `CONFIRMED -> PENDING`).
- **Terminal escape transitions** from `COMPLETED` or `CANCELLED`.
- **Cross-terminal transitions** (`COMPLETED -> CANCELLED`, `CANCELLED -> COMPLETED`).
- **Skip-ahead transitions** that bypass lifecycle steps (for example `PENDING -> READY_FOR_PICKUP`, `CONFIRMED -> COMPLETED`).

Rule: invalid transitions are rejected as contract violations, not silently coerced.

## Ownership and actor-direction constraints

This section aligns with S1.2 ownership policy for customer/admin scopes.

### Customer direction (owner-scoped)

- Customer operations are restricted to orders where `order.ownerUserId == principal.userId`.
- Customer-initiated state change direction is **cancellation-only**, and only while order is in a cancellable active state.
- Customers do not drive fulfillment progression states (`CONFIRMED`, `PREPARING`, `READY_FOR_PICKUP`, `COMPLETED`).

### Admin direction (operational scope)

- Admin may perform operational progression along forward lifecycle steps.
- Admin may perform cancellation under policy/governance constraints.
- Admin cannot violate invariant boundaries (no backward/terminal-escape/skip-ahead transitions).

## Cancellation and completion direction for O1

This contract sets the boundary for later O1 workflow implementation:

- **Cancellation direction:** one-way from active states to `CANCELLED`; never reversible.
- **Completion direction:** one-way `READY_FOR_PICKUP -> COMPLETED`; completion is final.
- **Mutual exclusion:** a cancelled order can never complete, and a completed order can never be cancelled.
- **Post-terminal immutability:** terminal status changes are closed; later O1 tasks may add metadata/audit recording but not terminal-state reversal.

## Menu/domain consistency invariants (conceptual)

To stay consistent with D1 baselines:

- Order lifecycle progression assumes order lines were derived from valid menu entities at placement time.
- Availability/ingredient constraints are evaluated by workflow logic before or during early progression (typically `PENDING -> CONFIRMED`), not by bypassing state model rules.
- State transitions do not redefine menu ownership/catalog rules; they operate on an already-owned order aggregate.

## Non-goals for this bead

- No Java enum/entity implementation.
- No service/controller or endpoint behavior implementation.
- No error payload/status code finalization for transition failures.
- No concurrency/locking algorithm specification beyond invariant expectations.

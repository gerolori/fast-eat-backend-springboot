# fast-2p4.1 — Domain Entity Inventory (MVP)

## First-class entities

### User
- **Purpose:** Represents account identity and actor context (customer/admin) used across authenticated backend behavior.
- **Primary flows supported:** authentication, authorization decisions, and ownership links for user-driven operations (e.g., placing orders).

### Menu
- **Purpose:** Top-level catalog aggregate for what can be ordered.
- **Primary flows supported:** browse/select available offerings and build order line decisions from published menu data.

### Ingredient
- **Purpose:** Represents inventory-facing components used by menu offerings.
- **Primary flows supported:** stock-aware reasoning for availability and future deduction/restock behavior tied to ordering.

### Order
- **Purpose:** Captures a placed purchase request and its lifecycle state.
- **Primary flows supported:** checkout/placement, status progression, and order history tracking.

## Inventory-level relationships (no schema detail)
- A **User** creates and owns one-to-many **Order** records.
- An **Order** contains menu-based selections derived from **Menu**.
- **Menu** offerings depend on one-to-many **Ingredient** items.
- **Ingredient** availability constrains whether related **Menu** items can be fulfilled.

## Supporting (non-root) types for MVP
- **UserRole** (enum/value set): role-based behavior boundaries for user actions.
- **OrderStatus** (enum/value set): lifecycle progression for order handling.
- **Audit/Base entity**: common identity + timestamps metadata shared by persisted entities.
- **Likely value objects (deferred detail):** money/price, quantity, and identifier wrappers where useful.

## Explicit boundary decisions
- **Menu remains the top-level catalog entity for now.**
- **Restaurant is explicitly deferred** until multi-restaurant scope is introduced.

## Non-goals for this bead
- No complete field-by-field entity specification.
- No JPA/table naming or persistence annotation decisions.
- No repository/query design details.

# fast-2p4.7 — DTO-to-Entity Mapping Boundaries (MVP)

## Purpose and scope
- Define explicit mapping boundaries between API contracts (request/response DTOs) and persistence models from `fast-2p4.1` and `fast-2p4.2`.
- Prevent API shape decisions from leaking into entities/repositories.
- Provide implementation-ready direction for upcoming controller/service/repository work without introducing code in this bead.

## Boundary placement

Mapping **must happen** in application/service boundary code (service methods, use-case handlers, or dedicated mapper components owned by that layer).

Mapping **must not happen** in:
- JPA entities (`domain.entity`)
- Spring Data repositories (`domain.repository`)
- Enum/value types in `domain.enums`

Direction:
- Controllers operate on DTOs.
- Services map DTOs to entities for writes and entities to DTOs for reads.
- Repositories remain entity-centric only.

## Mapping direction by operation

### Create flow (request DTO -> entity)
- Accept only API-owned create DTO fields.
- Build a new entity from allowed client-provided values.
- Server-managed fields are set by backend logic (not trusted from request payload).

### Update flow (request DTO -> existing entity)
- Load existing entity first.
- Apply only explicitly mutable fields from update DTO.
- Do not replace the whole entity from payload; perform selective patching to preserve invariants and ownership.

### Read flow (entity -> response DTO)
- Convert persisted entity state into response DTOs.
- Expose only contract-approved fields.
- Never return entities directly from controller endpoints.

## Field-level boundary rules

### IDs
- Entity primary keys (`id`, UUID per `fast-2p4.2`) are persistence identity.
- Create DTOs must not set entity IDs.
- Update/read DTOs may carry IDs for reference/response, but service logic controls identity resolution.

### Audit fields
- `created_at` / `updated_at` (or mapped Java fields) are persistence-managed.
- Request DTOs must not drive audit timestamps.
- Response DTOs may expose audit values only when the API contract intentionally includes them.

### Roles and enum-backed authority fields
- Role/status values map via explicit enum conversion/validation in service/mapper logic.
- Do not embed API-specific role strings directly into entities without validation.
- Privileged transitions (for example role escalation or restricted status transitions) require authorization checks before mapping.

### Ownership-sensitive fields
- Fields that affect ownership or actor linkage (for example `user_id` relationships) are server-resolved from authenticated context or trusted domain lookups.
- Clients must not be allowed to arbitrarily remap ownership by passing foreign IDs in generic update payloads.

### Nested collections / child objects
- Treat nested DTO collections as explicit synchronization logic, not blind object graph replacement.
- Define per-operation semantics (append/replace/remove) in service logic before applying entity mutations.
- Validate child references and ownership before attaching them to aggregate roots.

## Persistence/API decoupling guardrails
- Entity classes stay focused on persistence structure and invariants; no DTO annotations or API serialization concerns.
- Repository interfaces accept/return entities (or projection types defined for persistence use), never API DTOs.
- DTO evolution and entity evolution remain independently changeable through mapper/service adaptation.

## Non-goals for this bead
- No controller/service/repository code implementation.
- No mapper framework selection or configuration (manual vs MapStruct, etc.).
- No endpoint-level schema finalization.

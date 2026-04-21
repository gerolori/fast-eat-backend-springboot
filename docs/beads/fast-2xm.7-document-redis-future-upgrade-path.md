# fast-2xm.7 — Document Redis future upgrade path

This bead documents why Redis is deferred today and how to introduce it safely later.

## Current policy baseline

- Runtime baseline remains PostgreSQL-first.
- Docker Compose baseline includes backend + PostgreSQL only.
- Redis must not be a hard requirement for current local/CI default flows.

## Deferred-but-supported introduction path

When Redis-backed behavior is explicitly approved:

1. Add runtime contract updates first (`docker-compose.yml`, environment keys, profile docs).
2. Add focused Spring configuration for the first real use case (cache/session/etc.), not speculative scaffolding.
3. Add tests/verification for Redis-backed behavior and preserve fallback behavior where applicable.
4. Update docs in the same lane so the default path vs. optional Redis path stays explicit.

## Implementation note

- Shared policy wording is captured in `DEVELOPMENT.md` under "Redis Upgrade Path (Deferred by Policy)".

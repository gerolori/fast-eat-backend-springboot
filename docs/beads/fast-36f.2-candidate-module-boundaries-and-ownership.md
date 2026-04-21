# fast-36f.2 — Candidate Module Boundaries and Ownership (L1.2)

> Future-state planning document. No module split is implemented in this repository today.

This bead proposes candidate boundaries for later extraction.

## Candidate boundaries (future)

1. **API surface module**
   - Controllers, request/response DTOs, API error mappers.
2. **Domain/service module**
   - Business rules, workflow orchestration, mapping services.
3. **Persistence module**
   - Entities, repositories, persistence adapters.
4. **Security module**
   - AuthN/AuthZ filters, token components, security configuration.
5. **Common module**
   - Shared exceptions, constants, utility classes.

## Ownership direction

- API contracts: backend API owners
- Order workflow/business rules: domain owners
- Security/error semantics: security owners
- Persistence mapping constraints: data/domain owners

## Current-state reminder

- These boundaries are planning aids only.
- Current implementation model is still single-module Spring Boot.
- New work should keep package boundaries clean to reduce future extraction cost.

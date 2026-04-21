# fast-35k.5 — Release Quality Gate Checklist (T1.5)

This bead defines a practical pre-release checklist for backend changes.

## Quality gate checklist

### Contract and docs

- [ ] Relevant bead docs are updated for changed behavior.
- [ ] API naming consistency preserved (`/users/me`, `/menus`, `/orders`).
- [ ] Shared field naming and status enums remain contract-stable.

### Verification

- [ ] Required verification commands completed and recorded.
- [ ] `mvnw.cmd verify` passes for release candidate branches.
- [ ] No unexpected config drift in `application*.properties`.

### Security and error behavior

- [ ] `401` vs `403` semantics remain correct.
- [ ] Error envelope still includes expected stable fields.
- [ ] Correlation/trace mapping remains diagnosable (`X-Correlation-Id`, `traceId`).

### Beads lifecycle and repository hygiene

- [ ] Completed beads are closed.
- [ ] `.beads/issues.jsonl` changes are committed with corresponding work.
- [ ] Working tree is clean before handoff.

## Exit criteria

Release candidate can proceed only when all mandatory items above are green or risk-accepted explicitly.

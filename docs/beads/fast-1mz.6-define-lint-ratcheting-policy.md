# fast-1mz.6 — Define lint ratcheting policy

This bead records a practical lint/quality ratchet policy aligned with current Maven gates.

## Enforced now

- `mvnw.cmd verify` is the authoritative quality gate for code-impacting changes.
- Existing enforced checks are Spotless, Checkstyle, SpotBugs, and JaCoCo coverage check.
- Docs-only updates can use docs/beads verification commands when no production code is touched.

## Ratchet policy

- Do not relax existing gate configuration to land feature work.
- Apply a touched-file standard immediately: edited/new files must satisfy current gates.
- Increase strictness only in dedicated lint-hardening tasks (for example: threshold raise, suppression reduction, additional checks).
- Each ratchet step must keep mainline green and include docs updates describing the new baseline.

## Implementation note

- The canonical policy text is now in `DEVELOPMENT.md` under "Lint/Quality Gate Policy (Current + Ratchet Path)".

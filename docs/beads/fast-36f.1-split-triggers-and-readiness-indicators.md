# fast-36f.1 — Split Triggers and Readiness Indicators (L1.1)

> Future-state planning document. The current backend remains single-module.

This bead defines objective signals for when module extraction should be considered.

## Trigger candidates

- Sustained build/test slowdowns attributable to module-scale coupling.
- Frequent cross-area merge conflicts caused by mixed responsibilities.
- Repeated ownership ambiguity between API, domain, and security concerns.
- Need for independently versioned or reusable components.

## Readiness indicators

- Clear package boundaries already enforced in current code layout.
- Contract docs stable enough to support boundary extraction.
- CI has reliable baseline (`mvnw.cmd verify`) and low flaky-test rate.
- Team ownership map for candidate modules is agreed.

## Not a trigger by itself

- Preference for architecture purity alone.
- One-off refactoring discomfort.

Module split should be driven by measurable delivery/maintenance value.

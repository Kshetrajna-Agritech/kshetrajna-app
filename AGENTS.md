# AGENTS.md — Kshetrajna Coding-Agent Rules

## Mission
Build a reliable, offline-first Kotlin Android client for Kshetrajna.

## Mandatory reading
1. This file
2. `docs/PRODUCT.md`
3. `docs/ARCHITECTURE.md`
4. `docs/DATA_MODEL.md`
5. `docs/IMPLEMENTATION_PLAN.md`
6. Relevant API/BLE/offline/UI/safety/testing contracts

## Architecture
Use:
`UI -> ViewModel -> Use Case -> Repository -> Local/Remote/BLE`

Do not put domain logic in UI. Do not put BLE packet parsing in UI.

## Offline-first
Local persistence is the primary UI source. Network synchronization is asynchronous. Manual pH and important local records must be persisted before sync. Failed sync must never silently discard data.

## Safety
Android is not the final safety authority. Firmware/hardware retain authority over electrical fault, thermal shock, and salinity/fertigation interlocks.

Distinguish:
- command requested
- command sent
- command accepted/rejected
- actuator running/stopped
- safety locked/fault
- offline/stale

Never claim physical pump operation from command transmission alone.

## Data integrity
Distinguish measured, manual, inferred, forecast/external, device-state, and safety data. N/P/K are inference outputs; do not present them as laboratory measurements.

## Contracts
Never invent API paths, fields, UUIDs, packet formats, command codes, ACKs, calibration constants, thresholds, or agricultural accuracy claims. Use `TBD`, isolate the dependency behind an interface, and report the missing contract.

## Workflow
For every task:
1. Identify the milestone.
2. Read relevant contracts.
3. Inspect existing code.
4. Implement the smallest coherent change.
5. Add tests.
6. Run build/static analysis/tests.
7. Check offline behavior.
8. Report changes, tests, assumptions, contract impact, and unresolved issues.

Do not refactor unrelated code.

## Definition of done
Requirements implemented; architecture respected; tests added; offline/error/safety states handled; no invented contracts; documentation updated when contracts change; build/tests pass or failures are reported.

## Source-of-truth order
1. Approved project specifications
2. Approved API/BLE/data contracts
3. Architecture/safety contracts
4. Implementation plan
5. Existing code
6. Agent assumptions

If authoritative sources conflict, report the conflict; do not silently reconcile it.

## Never do
- bypass safety lockouts
- make cloud availability mandatory for core local operation
- label inferred NPK as lab measurements
- invent missing protocols
- equate UI action with physical actuation
- hide failed synchronization

## Final report
Use:
### Summary
### Files
### Tests
### Contract impact
### Assumptions/TBD
### Risks

# Android Implementation Plan

Implement milestone-by-milestone. Do not ask an agent to build the entire app in one pass.

## M0 — Contracts/baseline
Inspect existing project; confirm modules/package/application ID; identify missing API/BLE/data contracts.

## M1 — Android foundation
Kotlin/Android baseline, Jetpack architecture, navigation/theme foundation, test infrastructure.

## M2 — Domain model
Approved entities/enums and measured/manual/inferred distinctions.

## M3 — Local persistence
Offline database, manual pH persistence, migrations.

## M4 — Repositories
Local-first repository interfaces/implementations.

## M5 — Simulation
Deterministic telemetry/device simulation through production interfaces.

## M6 — Dashboard
Local-first dashboard with offline/stale/sync/safety states.

## M7 — Soil
Telemetry and supported history.

## M8 — Manual pH
Validation, local persistence, display, sync queue.

## M9 — Fertility/NPK
Approved backend/model integration. No invented inference formula.

## M10 — Weather
Approved weather integration and cache/freshness.

## M11 — Irrigation
Approved command/state UI. Distinguish request, acceptance, physical state, rejection, lockout.

## M12 — Alerts/safety
Safety and alert rendering; no bypass path.

## M13 — BLE
Only after BLE contract approval: scan/connect/discover/read/write/notify/parse/reconnect.

## M14 — Backend sync
Approved API, retries, deduplication, conflict handling.

## M15 — Offline validation
Intermittent connectivity, stale data, failed uploads, recovery.

## M16 — Hardware integration
Real ESP32/field-node validation.

## M17 — Calibration/field validation
Validated sensor calibration, crop profiles, thresholds, model validation, field testing.

## Completion checklist
- [ ] requirements identified
- [ ] relevant docs read
- [ ] implementation complete
- [ ] tests added
- [ ] build passes
- [ ] offline behavior checked
- [ ] safety boundary preserved
- [ ] no invented contracts
- [ ] docs updated if needed
- [ ] unresolved issues reported

## Agent prompt
> Read `AGENTS.md` and relevant `docs/`. Implement only milestone Mx. Inspect existing code first. Do not invent missing API, BLE, hardware, calibration, or safety specifications. Preserve offline-first behavior and the firmware safety boundary. Add tests, run build/test checks, and report files, tests, assumptions, contract changes, and unresolved issues.

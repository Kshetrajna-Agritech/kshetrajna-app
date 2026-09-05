# Testing Strategy

## Unit
Domain use cases, validation, state transitions, sync transitions, mappings, BLE parser, malformed packets, irrigation semantics, safety mapping.

## Repository
Persistence, API/BLE mapping, retries, deduplication, offline behavior, stale data.

## UI
Dashboard from local data; offline pH entry; sync status; inferred NPK labeling; irrigation rejection; safety lockout.

## Contract
When contracts exist, verify schemas, units, error codes, versions, ACK/rejection semantics, and schema evolution.

## Simulation
Deterministic synthetic telemetry/device/safety/weather data using production interfaces.

## Failure cases
No Internet, slow/failed API, duplicate sync, BLE disconnect, malformed packet, stale telemetry, command rejection, safety lockout, missing sensor data.

## Quality gate
Run formatter/linter, unit tests, UI/instrumentation tests where practical, build, and static analysis. Record failures.

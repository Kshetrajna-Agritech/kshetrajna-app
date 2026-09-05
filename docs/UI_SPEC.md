# UI Specification

## Principles
Readable, simple, explicit about offline/stale/inferred/safety state, and usable with intermittent connectivity.

## Screens
### Dashboard
Soil moisture, soil temperature, EC, weather context, irrigation state, safety state, sync/connectivity, important alerts.

### Soil
Telemetry and supported history with timestamps.

### Manual pH
Validated input, local persistence before sync, timestamp, visible sync state.

### Fertility/NPK
Show inferred N/P/K and model metadata when available. Never imply laboratory measurement.

### Weather
Current/cached forecast with source and freshness.

### Irrigation
Show command state separately from physical device state and safety lockout.

### Alerts
Severity, reason, timestamp, affected node/zone, action when defined.

### Settings
Approved configuration/diagnostics. No unsafe hardware overrides.

## Common states
Loading, populated, empty, offline, stale, syncing, sync failed, error, safety locked.

Exact navigation graph is TBD.

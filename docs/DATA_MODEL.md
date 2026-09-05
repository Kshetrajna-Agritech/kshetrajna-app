# Canonical Data Model

Every field requires a name, type, unit where applicable, required/optional status, source, meaning, and timestamp semantics.

## Entities
- Farm
- CropProfile
- Node
- SensorReading
- ManualPH
- WeatherData
- SoilAnalysis
- NpkResult
- IrrigationState
- IrrigationCommand
- SafetyState
- Alert
- SyncRecord

## Known concepts
SensorReading includes soil moisture, soil temperature, EC, air temperature, and air humidity where available.
ManualPH is app-entered.
NpkResult is inferred.
WeatherData is external/forecast.
IrrigationState represents command/device state.
SafetyState represents lockout/fault state.
SyncRecord tracks synchronization.

## Sync states
`PENDING`, `UPLOADING`, `SYNCED`, `FAILED`

## Measurement classification
| Category | Examples |
|---|---|
| Measured | EC, soil temperature, soil moisture |
| Manual | pH |
| Inferred | N/P/K |
| External/forecast | rainfall/weather |
| Device state | pump/relay |
| Safety | lockout/fault |

Exact units/schema must be confirmed from approved backend/firmware contracts.

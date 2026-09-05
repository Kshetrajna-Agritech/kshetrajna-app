# Safety and Actuation States

## Authority
Firmware/hardware retain final safety authority.

## Project safety mechanisms
1. stray-current/soil-fault protection
2. root-zone thermal-shock protection
3. inline salinity/fertigation protection

Exact thresholds/reset procedures are TBD unless separately approved.

## Recommended app states
`NORMAL`, `WARNING`, `LOCKED`, `FAULT`, `OFFLINE`

## Command lifecycle
`COMMAND_SENT`, `COMMAND_ACCEPTED`, `ACTUATOR_RUNNING`, `ACTUATOR_STOPPED`, `COMMAND_REJECTED`

Do not collapse these into one boolean.

## Rule
Safety lockout/fault overrides irrigation requests.

## UI
Show rejection/lockout reason when supplied. Do not provide a safety bypass.

Never say “pump running” unless physical/device telemetry supports that claim.

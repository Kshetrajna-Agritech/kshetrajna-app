# Android ↔ Backend API Contract

**Status: TBD until the FastAPI backend contract is approved.**

Do not invent production endpoints.

For every endpoint define:
- method/path
- authentication
- request/response schemas
- errors
- timestamps
- units
- idempotency
- retry behavior
- versioning

Logical operations may include telemetry sync, manual pH sync, farm/node configuration, soil state, weather, NPK inference, irrigation request, device state, and alerts. These are logical capabilities, not approved endpoint paths.

A successful API response is not proof of physical pump operation.

Before integration, obtain the approved FastAPI contract and fill the exact endpoint/DTO/error details.

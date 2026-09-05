# Offline-First and Synchronization

## Principle
Kshetrajna must remain useful without continuous Internet connectivity.

Local database is the primary UI source. Network synchronization is asynchronous.

## Required flow
Persist locally -> update UI -> queue sync -> sync when available -> reconcile deterministically.

## States
`PENDING`, `UPLOADING`, `SYNCED`, `FAILED`

Sync records need stable identity, timestamps, state, retry/error metadata, and deduplication/idempotency support. Exact schema is TBD.

## Retry
Retries must not create duplicate records. Do not delete local data on transient failure.

## Conflicts
Define per entity. Telemetry normally needs stable reading identity; manual pH needs entry identity; configuration needs explicit authority/versioning; stale safety-sensitive commands must not be blindly replayed.

## Weather
Cache weather with source and retrieval timestamp. Show stale data as stale.

## Safety
Offline mode never disables local hardware safety.

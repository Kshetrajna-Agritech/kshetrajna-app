# Android ↔ ESP32 BLE Protocol

**Status: TBD until the firmware BLE contract is approved.**

Never invent UUIDs, characteristic properties, packet bytes, command codes, ACKs, CRCs, or timeouts.

Android BLE responsibilities:
- scan
- discover
- connect
- read/write/notify
- parse
- reconnect
- handle malformed packets
- connection lifecycle/diagnostics

Firmware must define:
- service/characteristic UUIDs
- properties
- framing
- protocol version
- telemetry schema/units
- command schema
- ACK/error schema
- sequence/correlation IDs
- checksum/CRC
- packet limits
- timeout/retry rules

Logical message categories: identity, telemetry, status, safety, irrigation command, ACK, error, synchronization.

BLE commands are requests; firmware safety remains authoritative.

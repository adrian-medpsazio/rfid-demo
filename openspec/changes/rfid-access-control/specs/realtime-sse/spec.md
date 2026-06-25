# Realtime SSE Specification

## Purpose

Define how the system streams live tag read events to connected React clients using Server-Sent Events.

## Requirements

### Requirement: SSE Endpoint

The system MUST expose an SSE endpoint at `/api/v1/events/access` that streams `access-log` events to connected clients.

#### Scenario: Client connects and receives events

- GIVEN a client connects to the SSE endpoint
- WHEN a tag read for EPC `E280116060000209` is ingested and authorized
- THEN the client receives an SSE event with fields: `event: access-log`, `data: {"logId": "LOG-001", "epc": "...", "memberName": "Juan Pérez", "readerId": "GATE-01", "decision": "GRANTED", "timestamp": "..."}`

#### Scenario: Stale connection detected and closed

- GIVEN a client is connected to the SSE endpoint
- WHEN no data is sent or received for 60s
- THEN the server sends a heartbeat comment (`: heartbeat`)
- AND if the client does not respond within 5s, the connection is closed

### Requirement: Reconnection

The client MUST automatically reconnect on connection loss using the browser-native `EventSource` retry mechanism.

#### Scenario: Client reconnects after network interruption

- GIVEN a client was connected and lost connection
- WHEN the EventSource fires an `onerror` event
- THEN the client reconnects automatically
- AND receives any events that were buffered server-side (up to 100 events, oldest discarded)

### Requirement: Subscription by Reader

The system SHOULD allow clients to subscribe to events from specific readers via a query parameter.

#### Scenario: Filter by reader

- GIVEN a client connects to `/api/v1/events/access?readerId=GATE-01`
- WHEN a read from `GATE-02` is ingested
- THEN the client does NOT receive that event
- AND when a read from `GATE-01` is ingested, the client receives it

# RFID Ingestion Specification

## Purpose

Define how the system receives RFID tag reads from the Zebra FX9600 IoT Connector via HTTP POST, handles burst deduplication, and persists valid reads for downstream processing.

## Requirements

### Requirement: Ingestion Endpoint

The system MUST expose an HTTP POST endpoint at `/api/v1/ingestion/tag-read` that accepts IoT Connector payloads and returns HTTP 202 on successful acceptance.

#### Scenario: Valid tag read accepted

- GIVEN the IoT Connector sends a POST with body `{"epc": "E280116060000209", "readerId": "GATE-01", "antenna": 1, "timestamp": "2026-06-23T10:00:00Z"}`
- WHEN the endpoint receives the request
- THEN the system returns HTTP 202
- AND the read is persisted to the `access_log` table with the provided EPC, reader, antenna, and timestamp

#### Scenario: Malformed payload rejected

- GIVEN the IoT Connector sends a POST with missing `epc` field
- WHEN the endpoint receives the request
- THEN the system returns HTTP 400
- AND an error is logged

### Requirement: Burst Deduplication

The system MUST deduplicate identical tag reads from the same reader within a configurable dedup window (default 500ms).

#### Scenario: Duplicate burst read discarded

- GIVEN a tag read for EPC `E280116060000209` from reader `GATE-01` was accepted 200ms ago
- WHEN an identical read arrives within the 500ms dedup window
- THEN the system returns HTTP 202 (acknowledges receipt)
- BUT does NOT create a new access_log entry

#### Scenario: Read outside dedup window accepted

- GIVEN the last read for EPC `E280116060000209` from reader `GATE-01` occurred 600ms ago
- WHEN an identical read arrives
- THEN the system creates a new access_log entry

### Requirement: Reader Status Reporting

The system SHOULD track reader heartbeat and report an offline status if no reads are received from a reader within a configurable timeout (default 30s).

#### Scenario: Reader goes offline

- GIVEN reader `GATE-01` has reported reads every 5s
- WHEN no reads are received for 35s
- THEN the reader status is set to `OFFLINE`

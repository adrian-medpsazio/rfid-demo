# Multi-Gate Architecture Specification

## Purpose

Define the schema and API that support multiple RFID reader gates, enabling deployment across multiple access points from day one.

## Requirements

### Requirement: Reader Registration

The system MUST support registering readers with `readerId`, `location`, `description`, and `status`.

#### Scenario: Register new reader

- GIVEN a request with `{"readerId": "GATE-01", "location": "Main Entrance", "description": "Primary gate", "status": "ACTIVE"}`
- WHEN the POST `/api/v1/readers` endpoint is called
- THEN the system returns HTTP 201
- AND the reader is persisted and ready to accept tag reads

#### Scenario: Register duplicate readerId rejected

- GIVEN reader `GATE-01` already exists
- WHEN a second reader is registered with the same `readerId`
- THEN the system returns HTTP 409

### Requirement: Reader-Scoped Tag Reads

Every ingested tag read MUST include the `readerId` so the system can associate events with a specific reader.

#### Scenario: Read attributed to correct reader

- GIVEN two readers `GATE-01` and `GATE-02` are registered
- WHEN a tag read arrives with `readerId: "GATE-01"`
- THEN the access_log entry records `readerId: "GATE-01"`
- AND the event is attributed to Gate 01 in reporting

### Requirement: Reader Status Management

The system MUST support enabling and disabling readers. Disabled readers SHALL still receive tag reads but all decisions SHALL be `DENIED` with reason `READER_DISABLED`.

#### Scenario: Disabled reader denies access

- GIVEN reader `GATE-01` has status `DISABLED`
- WHEN a tag read arrives from `GATE-01`
- THEN the access is denied with reason `READER_DISABLED`
- AND the event is logged in the audit trail

#### Scenario: Query all readers

- GIVEN 3 readers are registered
- WHEN a GET `/api/v1/readers` request is made
- THEN the system returns all readers with their status and location

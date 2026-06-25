# Access Logging Specification

## Purpose

Define how access events are recorded in an append-only audit trail, supporting queries with filters and pagination.

## Requirements

### Requirement: Append-Only Log

The system MUST record every tag read in the `access_log` table. Log entries SHALL be immutable after creation — no update or delete operations SHALL be permitted.

#### Scenario: Read logged as access event

- GIVEN a tag read for EPC `E280116060000209` passes dedup validation
- WHEN the system persists the read
- THEN the `access_log` table contains a new row with EPC, reader ID, antenna, timestamp, and a unique log ID
- AND the row cannot be modified or deleted via the API

#### Scenario: Attempt to delete log entry blocked

- GIVEN an access log entry with ID `LOG-001` exists
- WHEN a DELETE request is made to `/api/v1/access-logs/LOG-001`
- THEN the system returns HTTP 405 Method Not Allowed

### Requirement: Query Access Logs

The system MUST support querying access logs by date range, member, reader ID, and tag EPC, with pagination.

#### Scenario: Query logs by date range

- GIVEN 100 access log entries exist, 40 within June 2026
- WHEN a GET `/api/v1/access-logs?from=2026-06-01&to=2026-06-30&page=1&size=10` is made
- THEN the system returns 10 entries within that date range
- AND the response includes total count (40) and pagination metadata

#### Scenario: Query logs by member

- GIVEN member `M-001` has 15 access log entries linked through their assigned tag
- WHEN a GET `/api/v1/access-logs?memberId=M-001` is made
- THEN the system returns only log entries where the scanned tag was assigned to `M-001` at the time of reading

#### Scenario: Query logs by reader

- GIVEN reader `GATE-01` has 25 log entries and reader `GATE-02` has 10
- WHEN a GET `/api/v1/access-logs?readerId=GATE-01` is made
- THEN the system returns only entries from `GATE-01`

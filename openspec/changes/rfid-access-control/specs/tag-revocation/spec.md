# Tag Revocation Specification

## Purpose

Define the secure disable and block flow for RFID tags, ensuring revoked tags are immediately denied access with full audit trail.

## Requirements

### Requirement: Revoke Tag

The system MUST allow revoking an active tag by EPC. Revoked tags SHALL have status `REVOKED` and MUST NOT be reassignable.

#### Scenario: Revoke active tag

- GIVEN EPC `E280116060000209` has status `ACTIVE` assigned to member `M-001`
- WHEN a POST `/api/v1/tags/revoke` with `{"epc": "E280116060000209", "reason": "LOST_TAG"}` is made
- THEN the system returns HTTP 200
- AND the tag status changes to `REVOKED`
- AND a revocation audit entry is created with EPC, reason, timestamp, and operator ID

#### Scenario: Revoke already-revoked tag

- GIVEN EPC `E280116060000209` already has status `REVOKED`
- WHEN a revoke request is made
- THEN the system returns HTTP 200 (idempotent)
- AND no additional revocation audit entry is created

### Requirement: Immediate Access Denial

A revoked tag MUST be denied access on the next scan with reason `TAG_REVOKED`.

#### Scenario: Revoked tag scanned at gate

- GIVEN EPC `E280116060000209` has status `REVOKED`
- WHEN a tag read for that EPC arrives at the ingestion endpoint
- THEN the `AuthorizationCheck` returns `DENIED` with reason `TAG_REVOKED`
- AND the access_log entry records the denial for audit

### Requirement: Revocation Audit Trail

Every revocation MUST be recorded in an audit trail with operator identity and reason.

#### Scenario: Audit trail created on revocation

- GIVEN a revocation is performed by operator `ADMIN-01` with reason `MEMBER_EXPIRED`
- WHEN the revocation is processed
- THEN an audit entry is persisted with EPC, previous status, new status (`REVOKED`), reason, operator ID, and timestamp
- AND the audit entry is immutable (append-only)

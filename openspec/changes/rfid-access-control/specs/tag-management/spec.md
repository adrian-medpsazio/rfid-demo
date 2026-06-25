# Tag Management Specification

## Purpose

Define the lifecycle of RFID tags from assignment through reassignment and return, including status tracking.

## Requirements

### Requirement: Assign Tag

The system MUST allow assigning an RFID tag (identified by EPC) to a member or vehicle. A tag SHALL have status `ACTIVE` upon assignment.

#### Scenario: Assign tag to member

- GIVEN EPC `E280116060000209` is unassigned and member `M-001` exists
- WHEN a POST `/api/v1/tags/assign` with `{"epc": "E280116060000209", "entityType": "MEMBER", "entityId": "M-001"}` is made
- THEN the system returns HTTP 201
- AND the tag status is `ACTIVE` associated with member `M-001`

#### Scenario: Reassign tag to different entity

- GIVEN EPC `E280116060000209` is currently assigned to member `M-001`
- WHEN the same EPC is assigned to member `M-002`
- THEN the system returns HTTP 409
- AND the error indicates the tag must be unassigned first

### Requirement: Unassign Tag

The system MUST allow unassigning a tag, returning it to the available pool with status `AVAILABLE`.

#### Scenario: Return tag to pool

- GIVEN EPC `E280116060000209` is assigned to member `M-001`
- WHEN a POST `/api/v1/tags/unassign` with `{"epc": "E280116060000209"}` is made
- THEN the system returns HTTP 200
- AND the tag status changes to `AVAILABLE`
- AND the tag is no longer linked to `M-001`

#### Scenario: Unassign already-available tag

- GIVEN EPC `E280116060000209` already has status `AVAILABLE`
- WHEN an unassign request is made
- THEN the system returns HTTP 200 (idempotent)
- AND the tag remains `AVAILABLE`

### Requirement: Tag Status Inquiry

The system MUST support querying a tag's current status, assignment, and history.

#### Scenario: Query tag status

- GIVEN EPC `E280116060000209` is assigned to member `M-001`
- WHEN a GET `/api/v1/tags/E280116060000209` request is made
- THEN the response includes status, assigned entity type, entity ID, and assignment date

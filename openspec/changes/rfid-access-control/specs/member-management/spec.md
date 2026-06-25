# Member Management Specification

## Purpose

Define CRUD operations for club members, including profile management, membership status tracking, and tag linking.

## Requirements

### Requirement: Create Member

The system MUST allow creating a member with `firstName`, `lastName`, `email`, `phone`, and `membershipType`. The system SHALL assign a unique member ID.

#### Scenario: Create member successfully

- GIVEN a request to create a member with `{"firstName": "Juan", "lastName": "Pérez", "email": "juan@example.com", "membershipType": "FULL"}`
- WHEN the POST `/api/v1/members` endpoint is called
- THEN the system returns HTTP 201 with the new member's ID
- AND the member status defaults to `ACTIVE`

#### Scenario: Duplicate email rejected

- GIVEN a member with email `juan@example.com` already exists
- WHEN a second member is created with the same email
- THEN the system returns HTTP 409
- AND an error message indicates the email is already registered

### Requirement: Update Member

The system MUST allow updating member profile fields. Email changes SHALL be validated for uniqueness.

#### Scenario: Update member details

- GIVEN member ID `M-001` exists with email `old@example.com`
- WHEN a PUT request updates the email to `new@example.com`
- THEN the system returns HTTP 200
- AND the member's email is updated in the database

#### Scenario: Update to existing email rejected

- GIVEN member `M-002` already has email `taken@example.com`
- WHEN member `M-001` is updated to use `taken@example.com`
- THEN the system returns HTTP 409

### Requirement: Deactivate Member

The system MUST support deactivating a member. Deactivated members SHALL be denied access.

#### Scenario: Deactivate active member

- GIVEN member `M-001` has status `ACTIVE` and an active tag assigned
- WHEN a PATCH request deactivates member `M-001`
- THEN the system returns HTTP 200
- AND the member status is set to `INACTIVE`
- AND all assigned tags are automatically revoked

### Requirement: List Members

The system MUST support listing members with pagination and optional status filter.

#### Scenario: List active members paginated

- GIVEN 50 members exist, 30 active and 20 inactive
- WHEN a GET request with `?status=ACTIVE&page=1&size=10` is made
- THEN the system returns 10 members with status `ACTIVE`
- AND the response includes total count (30) and pagination metadata

# Authorization Check Specification

## Purpose

Define the `AuthorizationCheck` interface that evaluates whether a tag read should be granted or denied access, providing a contract for a future rule engine implementation.

## Requirements

### Requirement: AuthorizationCheck Interface

The system MUST define a public interface `AuthorizationCheck` with a method `AuthorizationResult check(AuthorizationRequest request)`.

#### Scenario: Interface contract verified

- GIVEN the `AuthorizationCheck` interface
- WHEN inspected via code
- THEN it defines a method `check(AuthorizationRequest)` that returns `AuthorizationResult`
- AND `AuthorizationRequest` contains `epc`, `readerId`, `timestamp`
- AND `AuthorizationResult` contains `decision` (GRANTED or DENIED), `reason`, and `checkedAt` timestamp

### Requirement: Authorized Tag

The system SHALL grant access when the scanned tag is assigned to an active member with valid membership.

#### Scenario: Active member granted access

- GIVEN EPC `E280116060000209` is assigned to member `M-001` who is `ACTIVE`
- WHEN `check()` is called with that EPC
- THEN the result is `GRANTED` with reason `MEMBER_ACTIVE`

### Requirement: Denied Access — Inactive Member

The system SHALL deny access when the tag is assigned to an inactive member.

#### Scenario: Inactive member denied

- GIVEN EPC `E280116060000209` is assigned to member `M-001` who is `INACTIVE`
- WHEN `check()` is called with that EPC
- THEN the result is `DENIED` with reason `MEMBER_INACTIVE`

### Requirement: Denied Access — Revoked Tag

The system SHALL deny access when the scanned tag has status `REVOKED`.

#### Scenario: Revoked tag denied

- GIVEN EPC `E280116060000209` has status `REVOKED`
- WHEN `check()` is called with that EPC
- THEN the result is `DENIED` with reason `TAG_REVOKED`

### Requirement: Denied Access — Unknown Tag

The system SHALL deny access when the scanned EPC is not found in the tags table.

#### Scenario: Unknown tag denied

- GIVEN EPC `E280116060000999` does not exist in the system
- WHEN `check()` is called with that EPC
- THEN the result is `DENIED` with reason `TAG_UNKNOWN`

# Vehicle Management Specification

## Purpose

Define how vehicles are registered, linked to members, and associated with RFID tags for gate access.

## Requirements

### Requirement: Register Vehicle

The system MUST allow registering a vehicle with `licensePlate`, `make`, `model`, `color`, and an optional `memberId` for owner association.

#### Scenario: Register vehicle without owner

- GIVEN a request with `{"licensePlate": "ABC123", "make": "Toyota", "model": "Corolla", "color": "White"}`
- WHEN the POST `/api/v1/vehicles` endpoint is called
- THEN the system returns HTTP 201 with the vehicle ID
- AND the vehicle is stored without member association

#### Scenario: Register vehicle with duplicate license plate

- GIVEN a vehicle with license plate `ABC123` already exists
- WHEN a second vehicle is registered with the same plate
- THEN the system returns HTTP 409

### Requirement: Link Vehicle to Member

The system MUST allow associating a vehicle with one member. A member MAY have multiple vehicles.

#### Scenario: Link vehicle to member

- GIVEN vehicle `V-001` exists with no owner and member `M-001` exists
- WHEN a PUT request links `V-001` to `M-001`
- THEN the system returns HTTP 200
- AND `V-001` is associated with `M-001`

#### Scenario: Multiple vehicles per member

- GIVEN member `M-001` already owns vehicle `V-001`
- WHEN vehicle `V-002` is linked to the same member `M-001`
- THEN the system returns HTTP 200
- AND `V-002` is also associated with `M-001`

### Requirement: List Member Vehicles

The system MUST return all vehicles associated with a given member.

#### Scenario: Retrieve member vehicles

- GIVEN member `M-001` has two vehicles linked
- WHEN a GET `/api/v1/members/M-001/vehicles` request is made
- THEN the system returns both vehicles
- AND each vehicle entry includes its license plate and tag assignment status

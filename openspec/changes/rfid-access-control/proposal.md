# Proposal: rfid-access-control

## Intent
Build greenfield RFID access control with Zebra FX9600 (IoT Connector HTTP POST), Spring Boot, PostgreSQL, React SSE. Secure, scalable physical access management.

## Scope

### In Scope
- IoT Connector HTTP POST ingestion endpoint
- PostgreSQL schema (5 tables: readers, members, vehicles, rfid_tags, access_log)
- SSE real-time feed
- Tag revocation flow (mandatory)
- Multi-gate architecture from day 1
- AuthorizationCheck interface for future rule engine

### Out of Scope
- WebSocket migration, multi-station clustering, advanced rule engine, mobile app

## Capabilities

### New Capabilities
- `rfid-ingestion`: HTTP POST endpoint, EPC deduplication, burst read handling
- `member-management`: CRUD for personnel, tag linking
- `vehicle-management`: Vehicle registration, tag association
- `tag-management`: Lifecycle, revocation, status tracking
- `access-logging`: Audit trail with timestamps
- `realtime-sse`: SSE live feed to React
- `authorization-check`: Interface for future rule engine
- `multi-gate-architecture`: Schema/API for multiple gates
- `tag-revocation`: Secure disable and block flow

### Modified Capabilities
None (greenfield)

## Approach
FX9600 pushes reads to Spring Boot `@PostMapping`. Deduplicate burst reads (configurable window). Store in PostgreSQL. Push SSE to React. Schema supports multi-gate. Tag revocation mandatory. AuthorizationCheck interface ready.

## Affected Areas
- `backend/src/`: Spring Boot app (ingestion, API, SSE)
- `frontend/src/`: React app (SSE, components)
- `database/migrations/`: PostgreSQL schema
- `openspec/specs/`: 9 new capability specs

## Risks
| Risk | Likelihood | Mitigation |
|------|------------|------------|
| IoT Connector firmware < 3.10.X | Low | Verify firmware version |
| EPC burst deduplication | Medium | Configurable dedup window (500ms) |
| Tag revocation security gaps | High | Mandatory flow, audit logging |
| Network partition buffering | Low | IoT Connector buffers 150K events |

## Rollback Plan
Greenfield: revert git commits, drop tables, stop services. No existing system.

## Dependencies
- Zebra FX9600 with IoT Connector firmware ≥ 3.10.X
- PostgreSQL server
- Node.js 18+ for React build

## Success Criteria
- [ ] Single gate demo operational with real tag reads
- [ ] Real-time SSE feed in React
- [ ] Tag revocation blocks access within 1 second
- [ ] Schema supports multi-gate expansion
- [ ] AuthorizationCheck interface ready
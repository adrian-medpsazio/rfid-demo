# Tasks: RFID Access Control

## Review Workload Forecast

| Field | Value |
|-------|-------|
| Estimated changed lines | ~1,800–2,300 |
| 400-line budget risk | High |
| Chained PRs recommended | Yes |
| Suggested split | PR 1: Schema+Config+Domain → PR 2: Persistence+Core Services → PR 3: Controllers+Frontend → PR 4: Tests+Polish |
| Delivery strategy | ask-on-risk |
| Chain strategy | pending |

Decision needed before apply: Yes
Chained PRs recommended: Yes
Chain strategy: pending
400-line budget risk: High

### Suggested Work Units

| Unit | Goal | Likely PR | Notes |
|------|------|-----------|-------|
| 1 | Foundation: schema, config, domain models, interfaces | PR 1 | Base for all subsequent work |
| 2 | Persistence + core services: repos, auth, dedup, SSE | PR 2 | Depends on PR 1 domain types |
| 3 | API + frontend: controllers, React components | PR 3 | Depends on PR 2 services |
| 4 | Tests: unit + integration covering spec scenarios | PR 4 | Depends on PR 3 controllers |

## Phase 1: Foundation

- [ ] 1.1 Create `V1__init_schema.sql` — 5 tables (readers, members, vehicles, rfid_tags, access_log) with FKs and indexes
- [ ] 1.2 Update `pom.xml` — Spring Boot 3.x, Flyway, Testcontainers, Validation, Lombok deps
- [ ] 1.3 Create `application.yml` — DB connection, dedup window (500ms), SSE config, server port
- [ ] 1.4 Create `application-demo.yml` — demo profile with H2 or demo PG connection
- [ ] 1.5 Create domain records/entities: `Tag`, `Member`, `Vehicle`, `Reader`, `AccessLog`
- [ ] 1.6 Create `AuthorizationCheck` interface + `AuthorizationResult`/`AuthorizationRequest` records
- [ ] 1.7 Create ingress DTO (`TagReadRequest`) and SSE event DTO (`AccessEventDTO`)

## Phase 2: Persistence + Core Services

- [ ] 2.1 Create JPA entity classes for all 5 tables with Flyway-compatible mappings
- [ ] 2.2 Create Spring Data JPA repos: `TagRepository`, `MemberRepository`, `VehicleRepository`, `ReaderRepository`, `AccessLogRepository`
- [ ] 2.3 Create `DeduplicationService` — sliding window dedup (Map<ReaderEpcKey, Instant>) with configurable 500ms TTL
- [ ] 2.4 Create `DefaultAuthorizationCheck` — decide GRANTED/DENIED based on tag status, member status, reader status
- [ ] 2.5 Create `SseBroadcaster` — thread-safe SseEmitter registry with heartbeat, cleanup, per-reader filtering

## Phase 3: API + Frontend

- [ ] 3.1 Create `IngestionController` — `POST /api/ingest`, calls DeduplicationService + AuthCheck + SseBroadcaster
- [ ] 3.2 Create `SseController` — `GET /api/events`, returns SseEmitter with readerId filter support
- [ ] 3.3 Create `TagController` — CRUD + assign/unassign + revoke endpoint
- [ ] 3.4 Create `MemberController` — CRUD with email uniqueness validation
- [ ] 3.5 Create `VehicleController` — CRUD + member link/unlink
- [ ] 3.6 Create `ReaderController` — CRUD + status management (ACTIVE/DISABLED)
- [ ] 3.7 Create `AccessLogController` — paginated query with date/member/reader/EPC filters
- [ ] 3.8 Create `frontend/src/types/api.ts` — shared DTO types matching backend contracts
- [ ] 3.9 Create `frontend/src/services/sse.ts` — EventSource wrapper with auto-reconnect and heartbeat
- [ ] 3.10 Create `frontend/src/components/AccessFeed.tsx` — auto-scrolling real-time access event list
- [ ] 3.11 Create `frontend/src/components/TagManager.tsx` — tag CRUD UI with revoke button
- [ ] 3.12 Create `frontend/src/components/GateStatus.tsx` — per-reader status, last read, throughput

## Phase 4: Testing

- [ ] 4.1 Unit: `DeduplicationServiceTest` — burst duplicate drop, outside-window accept, reader-scoped dedup
- [ ] 4.2 Unit: `DefaultAuthorizationCheckTest` — active member GRANTED, inactive/TAG_REVOKED/TAG_UNKNOWN/READER_DISABLED DENIED
- [ ] 4.3 Unit: `SseBroadcasterTest` — broadcast to multiple clients, reader filter, stale emitter cleanup
- [ ] 4.4 Integration: `IngestionControllerTest` (`@SpringBootTest` + Testcontainers) — valid read 202, malformed 400
- [ ] 4.5 Integration: `TagControllerTest` — assign, unassign, revoke against real DB
- [ ] 4.6 Integration: `MemberControllerTest` — create/update/deactivate with email uniqueness
- [ ] 4.7 Integration: `AccessLogControllerTest` — pagination, date range, member/reader/EPC filters, 405 on DELETE
- [ ] 4.8 Integration: `SseControllerTest` — connect and verify event delivery after ingest

## Phase 5: Polish

- [ ] 5.1 Add MetricsCollector integration (latency, throughput, dedup rate counters)
- [ ] 5.2 Verify all endpoints return consistent error response format (`ProblemDetail` or custom)
- [ ] 5.3 Add CORS config for frontend dev server

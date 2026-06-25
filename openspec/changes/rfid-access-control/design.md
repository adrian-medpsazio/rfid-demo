# Design: rfid-access-control

## Technical Approach

Greenfield Spring Boot 3.x + React 18 system ingesting Zebra FX9600 reads via IoT Connector HTTP POST. Burst reads deduplicated (500ms window). Hexagonal architecture: domain core (AuthorizationCheck, Tag, Member, Vehicle, AccessLog) surrounded by adapters (REST, SSE, PostgreSQL, IoT Connector). PostgreSQL schema with 5 tables supporting multi-gate from day 1. React consumes SSE for real-time dashboard.

## Architecture Decisions

### Decision: IoT Connector HTTP POST vs SDK

**Choice**: IoT Connector HTTP POST to Spring Boot `@PostMapping`
**Alternatives**: Zebra SDK direct integration, MQTT broker
**Rationale**: IoT Connector handles 150K event buffer, firmware upgrades, network partitions. Zero vendor lock-in — standard HTTP. SDK adds JVM dependency, version coupling, no buffering.

### Decision: SSE vs WebSocket

**Choice**: Server-Sent Events (SSE)
**Alternatives**: WebSocket, polling
**Rationale**: Unidirectional server→client fits dashboard. Auto-reconnect, HTTP/2 multiplexing, simpler infra (no WS upgrade). WebSocket adds complexity for no benefit — client never pushes.

### Decision: Hexagonal Layering

**Choice**: Domain core + adapters (inbound: REST/SSE/IoT; outbound: PostgreSQL, future rule engine)
**Alternatives**: Layered (controller-service-repo), modular monolith
**Rationale**: AuthorizationCheck interface isolates rule engine. IoT Connector adapter swappable. Test domain logic without Spring/DB. Matches team's Clean Architecture experience.

### Decision: Burst Read Deduplication

**Choice**: Configurable 500ms sliding window per reader+EPC
**Alternatives**: Fixed window, database unique constraint, no dedup
**Rationale**: FX9600 bursts 50-100 reads/sec per tag. 500ms covers typical burst. Configurable per gate. DB constraint too late (already persisted). In-memory Map<ReaderEpcKey, Instant> with TTL eviction.

## Data Flow

```
FX9600 ──(IoT Connector HTTP POST)──→ IngestionController
    │
    ├─→ DeduplicationService (500ms window)
    │       │
    │       ├─→ TagRepository (find by EPC)
    │       │       │
    │       │       ├─→ AuthorizationCheck.decide(tag, reader, ctx)
    │       │       │       │
    │       │       │       ├─→ AccessLogRepository.save(log)
    │       │       │       │
    │       │       │       └─→ SseBroadcaster.broadcast(AccessEventDTO)
    │       │       │
    │       │       └─→ (revoked/unknown) → log DENIED + broadcast
    │       │
    │       └─→ (duplicate) → drop, metrics++
    │
    └─→ MetricsCollector (latency, throughput, dedup rate)
```

React SSE client → `EventSource("/api/events")` → renders live feed.

## File Changes

| File | Action | Description |
|------|--------|-------------|
| `backend/src/main/java/.../ingestion/IngestionController.java` | Create | `@PostMapping("/api/ingest")` receives IoT Connector JSON |
| `backend/src/main/java/.../ingestion/DeduplicationService.java` | Create | Sliding window dedup per reader+EPC |
| `backend/src/main/java/.../domain/AuthorizationCheck.java` | Create | Interface `Decision decide(Tag, Reader, Context)` |
| `backend/src/main/java/.../domain/DefaultAuthorizationCheck.java` | Create | Allow if tag active, not revoked, member/vehicle valid |
| `backend/src/main/java/.../sse/SseController.java` | Create | `GET /api/events` returns `SseEmitter` |
| `backend/src/main/java/.../sse/SseBroadcaster.java` | Create | Thread-safe broadcast to all emitters |
| `backend/src/main/java/.../api/TagController.java` | Create | CRUD + revoke endpoint |
| `backend/src/main/java/.../api/MemberController.java` | Create | CRUD + tag linking |
| `backend/src/main/java/.../api/VehicleController.java` | Create | CRUD + tag linking |
| `backend/src/main/java/.../api/ReaderController.java` | Create | CRUD for gate readers |
| `backend/src/main/java/.../api/AccessLogController.java` | Create | Paginated history, filters |
| `backend/src/main/resources/db/migration/V1__init_schema.sql` | Create | Flyway: 5 tables, indexes, FKs |
| `frontend/src/services/sse.ts` | Create | EventSource wrapper, reconnection, typed events |
| `frontend/src/components/AccessFeed.tsx` | Create | Real-time list with auto-scroll |
| `frontend/src/components/TagManager.tsx` | Create | CRUD + revoke button |
| `frontend/src/components/GateStatus.tsx` | Create | Per-gate last read, throughput |
| `frontend/src/types/api.ts` | Create | Shared DTO types (ingress, SSE event, tag, member, vehicle) |

## Interfaces / Contracts

### Ingress DTO (IoT Connector → Spring)
```json
{
  "readerId": "GATE-01",
  "epc": "E20000123456789012345678",
  "antenna": 1,
  "rssi": -45,
  "timestamp": "2026-06-23T10:15:30.123Z"
}
```

### SSE Event DTO (Spring → React)
```json
{
  "eventId": "uuid",
  "timestamp": "2026-06-23T10:15:30.123Z",
  "readerId": "GATE-01",
  "epc": "E20000123456789012345678",
  "decision": "GRANTED",
  "memberName": "Juan Pérez",
  "vehiclePlate": "ABC-123",
  "reason": null
}
```

### AuthorizationCheck Interface
```java
public interface AuthorizationCheck {
    Decision decide(Tag tag, Reader reader, AuthorizationContext ctx);
    enum Decision { GRANTED, DENIED }
    record AuthorizationContext(Instant now, String correlationId) {}
}
```

### REST Endpoints
| Method | Path | Description |
|--------|------|-------------|
| POST | `/api/ingest` | IoT Connector ingestion |
| GET | `/api/events` | SSE stream |
| GET/POST/PUT/DELETE | `/api/tags` | Tag lifecycle + revoke |
| GET/POST/PUT/DELETE | `/api/members` | Member CRUD |
| GET/POST/PUT/DELETE | `/api/vehicles` | Vehicle CRUD |
| GET/POST/PUT/DELETE | `/api/readers` | Reader CRUD |
| GET | `/api/logs` | Paginated access log |

## Testing Strategy

| Layer | What to Test | Approach |
|-------|-------------|----------|
| Unit | DeduplicationService, DefaultAuthorizationCheck, domain entities | JUnit 5 + Mockito, pure logic, no Spring context |
| Integration | IngestionController, SseController, REST controllers | `@SpringBootTest` + Testcontainers (PostgreSQL), `@AutoConfigureMockMvc` |
| E2E | Full flow: POST ingest → SSE receive → React render | Manual with physical FX9600 + Cypress for UI regression |

## Migration / Rollout

No migration required (greenfield). Flyway `V1__init_schema.sql` creates all tables. Feature flag `sse.enabled` for gradual rollout. Tag revocation immediate — no async delay.

## Open Questions

- [ ] Confirm IoT Connector JSON payload matches spec (field names, types)
- [ ] Decide correlationId propagation (IoT Connector → log → SSE)
- [ ] SSE heartbeats: 30s comment lines or rely on browser keep-alive?
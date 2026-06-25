# Exploration: RFID Access Control — Java/Spring Boot + React Architecture

## 1. Zebra FX9600 Java SDK Integration (Option A: Direct Control)

### How the SDK Connects
The Zebra Host RFID SDK for C and Java on Linux connects to FX9600 over IP using **LLRP (Low Level Reader Protocol)** on TCP port **5084** (default). You instantiate a `Reader` object passing the reader IP, call `connect()`, then register listeners for tag-read events via `setTagReadEvent()` + `addEventsListener()`.

### Event Model
Fully **event-driven** via Java listener/callback pattern. When a tag enters the read zone the SDK fires `TagReadEvent` objects on a dedicated reader thread. You register a `ReaderListener` implementation. No polling loop needed — the SDK manages the connection and fires callbacks.

### Embedded on FX9600 vs External PC
The FX9600 runs **embedded Linux** with a built-in web server but it is NOT a general-purpose Ubuntu/CentOS host. The Zebra Java SDK requires a **separate 64-bit Linux machine** (Ubuntu or CentOS). In production that would be the gate PC or an on-premise server.

For Docker-based Spring Boot demo: you'd need a Linux container (or a separate Linux VM) that hosts the SDK and makes it available to the JVM — the SDK is a **native JNI library**, NOT a plain Maven JAR. This adds deployment complexity for the demo.

### Maven/Gradle Coordinates
The SDK is delivered as a **local download from Zebra's Support portal**, not from Maven Central or any public repo. Coordinates for the Linux version:
- `com.zebra/rfid-sdk` (package name inside the JAR)
- You install the JAR + native `.so` library into your local Maven repo or a corporate Nexus, or load it via `-Djava.library.path`

This is a meaningful gotcha for CI/CD and Docker builds on non-Linux runners.

### Threading Model
The SDK uses its own internal thread pool. The `ReaderListener` callbacks are invoked on SDK-managed threads. Integration with Spring Boot means:
- Tag events arrive off the Spring event loop — you bridge them in via an `ApplicationEventPublisher` if you want Spring events.
- The SDK connection lifecycle (connect/reconnect/disconnect) must be managed in a `@Bean` with proper shutdown hooks.
- **Reconnection logic is NOT automatic by default** — you must implement `onReaderDisconnectEvent` and call `reader.reconnect()`.

### Gotchas
- **Native library path**: the JAR requires `librfid-sdk.so` on `java.library.path`; mismatched architecture = `UnsatisfiedLinkError`.
- **Licensing**: the SDK requires a Runtime License file (`license.xml`) from Zebra; missing license = limited functionality or runtime failure.
- **Reader management**: you manage ONE reader per `Reader` instance; multi-reader = multiple objects.
- **Firmware lock**: firmware must be >= 3.10.X for FX9600 FX9600 features.

---

## 2. Architecture Options

### Option A: Spring Boot Directly Controls the Reader (SDK)

```
[FX9600] ← LLRP/TCP → [Spring Boot App — Zebra Java SDK] → [DB]
                                          ↓
                                     [REST API] ← [React Frontend]
```

**Flow**: Java SDK opens socket, fires `TagReadEvent` → service publishes Spring `ApplicationEvent` → controller looks up member in DB → writes `access_log` → SSE/WebSocket sends notification to React.

| Dimension | Detail |
|-----------|--------|
| **Pros** | - Full programmatic control over reader (start/stop/inventory modes, antenna config, GPIO). Single binary — no broker. Lowest end-to-end latency. |
| **Cons** | - Native JNI dependency complicates Docker/CI. - Reconnection logic is your responsibility. - Spring Boot owns the reader lifecycle — if JVM crashes the reader stops autonomously. - Harder to scale: each Spring Boot instance can hold 1 reader well; 10 readers = 10 JVMs or a custom connector pool. |
| **Effort** | **Medium** — medium SDK integration, plus reconnection and lifecycle management. |

### Option B: IoT Connector + Spring Boot Consumer (Recommended)

```
[FX9600 + IoT Connector] — HTTP POST / MQTT → [Spring Boot] → [DB]
                                              ↓
                                         [REST API + SSE/WS] ← [React]
```

**Flow**: FX9600 configured via its web UI to push tag-read events (JSON) to `POST http://gate-pc:8080/api/rfid/ingest` (or MQTT topic). Spring Boot controller ingests → same DB + notification flow as Option A.

| Dimension | Detail |
|-----------|--------|
| **Pros** | - **No SDK/JNI dependency** in Spring Boot. - Reader self-manages: reboots, reconnects, buffers 150K tag events on its own (retention). Decoupled: Spring Boot can be down; reader buffers and replays when it comes back. - Scale trivially: many readers POST to same endpoint. - Configure via reader web UI — no code to change reader settings. |
| **Cons** | - Less dynamic control from Spring Boot (can't call `reader.startInventory()` — must use MQTT control or REST management API). - Adds an HTTP/MQTT hop (sub-millisecond latency hit — irrelevant for gate scenario). - For demo: needs at minimum a network connection and listener endpoint. |
| **Effort** | **Low** — write one Spring `@PostMapping` controller + DTO. Reader config is UI-driven. |

### Comparison Table

| | Option A (SDK) | Option B (IoT Connector) |
|---|---|---|
| SDK/JNI dependency | Yes (native `.so`) | **None** |
| Reader lifecycle | Spring manages | Reader self-manages |
| Reconnection logic | You write it | Built-in (retention buffer) |
| Multi-gate scaling | Hard (1 JVM ↔ 1 reader) | **Natural** (many readers → same endpoint) |
| Dynamic reader control | Full programmatic | MQTT/REST management API |
| Demo/setup friction | Higher (license, native lib) | **Lower** (UI config + 1 endpoint) |
| Production resilience | Lower | **Higher** (150K event buffer, batching) |

### Recommendation: **Option B (IoT Connector) for both demo and production.**

For a gate access-control system the reader's job is simple: continuously scan for tags in portal mode and report reads. You don't need to dynamically start/stop inventory from the backend — the reader should always be reading. IoT Connector was designed exactly for this.

Use **HTTP POST** for the local deployment (not MQTT) for the demo — it's one REST endpoint, no broker needed. Move to MQTT later if you need the buffering QoS guarantees or cloud integration.

Spring Boot keeps the same internal structure either way — only the **ingress layer** (controller) differs, making the codebase loosely coupled to the integration mode.

---

## 3. Database Model

### What RFID Tags Store
EPC (Electronic Product Code) — typically a **96-bit unique identifier**. The user cannot write arbitrary member data onto a UHF passive tag in this workflow (access-control scenario). The backend does the lookup: tag EPC → member → vehicle → authorization.

**Do NOT embed PII on the tag.** EPC is just a key. All authorization logic lives server-side.

### Schema

```sql
-- Reader / gate dimension (design for multi-gate from day 1)
CREATE TABLE readers (
    id          BIGSERIAL PRIMARY KEY,
    name        VARCHAR(100) NOT NULL,        -- "Gate 1 - Main Entrance"
    serial      VARCHAR(50) UNIQUE NOT NULL,  -- FX9600 serial
    ip_address  INET NOT NULL,
    location    VARCHAR(100),
    active      BOOLEAN DEFAULT true,
    created_at  TIMESTAMPTZ DEFAULT NOW()
);

-- Country club members (socios)
CREATE TABLE members (
    id          BIGSERIAL PRIMARY KEY,
    first_name  VARCHAR(100) NOT NULL,
    last_name   VARCHAR(100) NOT NULL,
    email       VARCHAR(255),
    phone       VARCHAR(20),
    photo_url   VARCHAR(500),                -- path to stored photo
    member_code VARCHAR(50) UNIQUE,          -- socio number
    active      BOOLEAN DEFAULT true,
    created_at  TIMESTAMPTZ DEFAULT NOW(),
    updated_at  TIMESTAMPTZ DEFAULT NOW()
);

-- Vehicles registered to members
CREATE TABLE vehicles (
    id              BIGSERIAL PRIMARY KEY,
    member_id       BIGINT REFERENCES members(id),
    plate           VARCHAR(20) NOT NULL,
    brand           VARCHAR(50),
    model           VARCHAR(50),
    color           VARCHAR(30),
    created_at      TIMESTAMPTZ DEFAULT NOW()
);

-- RFID tags (EPC registrations)
CREATE TABLE rfid_tags (
    id              BIGSERIAL PRIMARY KEY,
    epc             VARCHAR(64) UNIQUE NOT NULL, -- hex EPC
    member_id       BIGINT REFERENCES members(id),
    vehicle_id      BIGINT REFERENCES vehicles(id), -- nullable: tag might not be vehicle-bound
    assigned_at     TIMESTAMPTZ DEFAULT NOW(),
    revoked_at      TIMESTAMPTZ,
    active          BOOLEAN DEFAULT true,
    UNIQUE(member_id, vehicle_id)
);

-- Access events (append-only log)
CREATE TABLE access_log (
    id              BIGSERIAL PRIMARY KEY,
    tag_epc         VARCHAR(64) NOT NULL,
    reader_id       BIGINT REFERENCES readers(id),
    member_id       BIGINT REFERENCES members(id),
    vehicle_id      BIGINT REFERENCES vehicles(id),
    authorized      BOOLEAN NOT NULL,
    reason          VARCHAR(255),             -- "authorized", "tag not found", "member inactive"
    read_count      INT DEFAULT 1,           -- tags repeat-read within same scan burst
    tag_timestamp   TIMESTAMPTZ NOT NULL,    -- as reported by reader (IoT Connector timestamp)
    server_timestamp TIMESTAMPTZ DEFAULT NOW(),
    created_at      TIMESTAMPTZ DEFAULT NOW()
);

-- Indexes
CREATE INDEX idx_access_log_reader_ts ON access_log(reader_id, server_timestamp);
CREATE INDEX idx_access_log_tag_ts   ON access_log(tag_epc, server_timestamp);
CREATE INDEX idx_rfid_tags_epc       ON rfid_tags(epc);
```

### Relationship Logic
```
members 1—* vehicles
members 1—* rfid_tags (current active tag)
vehicles 0—1 rfid_tags (optional: tag is tied to a vehicle)
readers 1—* access_log
rfid_tags 1—* access_log
```

A tag EPC read at the gate resolves: `rfid_tags.epc` → `member_id` + optional `vehicle_id` → check `members.active`. Authorization = tag active AND member active AND (optionally) within allowed time window.

---

## 4. React Frontend

### Guard View (primary screen)

| Element | Detail |
|---------|--------|
| **Live scan feed** | WebSocket/SSE event stream from Spring Boot; latest read highlighted. |
| **Tag-read card** | Member photo, full name, member code, vehicle (plate + model), authorized/rejected badge. Large font, high contrast (outdoor visibility). |
| **Audible + visual alert** | Green flash = authorized, red = rejected (Spring Boot pushes event; React handles the UI). |
| **Recent access log** | Last 20 reads scrollable — timestamp, member, plate, authorized. |

### Admin Screens (smaller scope but needed)

| Screen | Purpose |
|--------|---------|
| **Member registration** | Form: name, photo upload, member code, phone, email. |
| **Assign tag** | Search member → enter/scan EPC → link rfid_tag → (optional) link vehicle. |
| **Access log history** | Filterable by reader, date range, member, authorization status. |
| **Reader management** | Register new reader (name, serial, IP, location). |

### Component Tree (Demo Scope)

```
frontend/
├── src/
│   ├── components/
│   │   ├── layout/
│   │   │   ├── AppShell.tsx            ← sidebar navigation + auth shell
│   │   │   └── Sidebar.tsx
│   │   ├── gate/
│   │   │   ├── GateMonitor.tsx         ← main live-feed screen
│   │   │   ├── TagReadCard.tsx         ← member photo + info card
│   │   │   └── AccessLogTable.tsx      ← scrollable recent reads
│   │   ├── members/
│   │   │   ├── MemberForm.tsx          ← create/edit member
│   │   │   └── TagAssignment.tsx       ← scan/bind EPC to member
│   │   └── shared/
│   │       ├── StatusBadge.tsx         ← authorized/denied pill
│   │       └── ConfirmDialog.tsx
│   ├── services/
│   │   ├── rfidApi.ts                  ← REST: tag reads, members, vehicles, log
│   │   └── rfidStream.ts               ← WebSocket/SSE client for live events
│   ├── hooks/
│   │   └── useRfidStream.ts
│   └── App.tsx
```

### Real-Time Mechanism
Spring Boot exposes a **Server-Sent Events (SSE) endpoint** (`/api/rfid/stream`) — simpler than WebSockets for unidirectional server→client feeds. React's `EventSource` API or a lightweight hook handles it. After a tag read, Spring Boot publishes an in-app event → the stream controller emits it to all connected clients.

---

## 5. Demo vs Production

| Concern | Demo (H2 + "no auth") | Production (PostgreSQL + security) |
|---------|----------------------|-------------------------------------|
| **Database** | H2 file-based or in-memory; `spring.profiles.active=demo` uses `application-demo.yml` | PostgreSQL with Flyway/Liquibase migrations |
| **Security** | No auth for Spring API; React served from same origin. Access app from LAN only. | Spring Security + JWT/OAuth2 for admin endpoints; guard PC is kiosk-locked physically |
| **Multi-gate** | Design DB with `readers` table from day 1; deploy 1 instance, 1 reader | Multiple Spring Boot instances or one instance consuming from MQTT broker |
| **IoT Connector** | HTTP POST mode, single reader → Spring Boot | MQTT with TLS; retention + batching configured |
| **React** | Vite dev server or bundled static; one browser tab open on gate PC | Build → serve from Spring Boot (static resources) or CDN; kiosk mode |
| **Photo storage** | Filesystem folder, referenced by path | S3-compatible object storage (MinIO or cloud) |

### What to Cut for Demo but Design for Later
- **Member management**: Include a minimal admin screen for demo, but structure the service layer so it can be replaced by a real user directory (LDAP/AD) later.
- **Tag deactivation/return**: Design the `rfid_tags.revoked_at` column; skip the UI flow for demo.
- **Access rules engine**: Demo uses simple `active` flag. Production adds time-window rules, per-gate authorization, membership tier. Encode this as a strategy/pluggable check service from day 1:
  ```java
  public interface AuthorizationCheck {
      boolean isAuthorized(RfidTag tag, Reader reader, Instant timestamp);
  }
  ```
- **Audit / compliance exports**: DB captures full `tag_timestamp` from reader + `server_timestamp`. Design retention policy now (e.g., keep 2 years), implement export later.

---

## Risks and Gotchas

1. **Native SDK friction (if Option A is ever needed)**: The Zebra Java SDK's native `.so` requirement breaks macOS/Windows dev machines and non-amd64 CI runners. Docker builds need `--platform=linux/amd64`. Document the Dockerfile hack early or stick to Option B and avoid SDK entirely.

2. **IoT Connector firmware version**: FX9600 must run firmware >= 3.10.X. Confirm with hardware team before assuming IoT Connector is available. If firmware is older, only Option A (Host SDK) works.

3. **EPC collision / tag reuse**: Passive UHF tags are not "owned" — if a tag is programmed and then reassigned to a different member without revoking the old record, the old member retains access. The **tag return/revoke flow** is a real security must-have; treat it as mandatory even in demo.

4. **Tag ghost reads / duplicate bursts**: LLRP and IoT Connector both can fire the same EPC multiple times within a 1-2 second window. Deduplicate: use a `(tag_epc, reader_id, 5-second window)` sliding window before showing to the user and before persisting (increment `read_count` instead).

5. **Network partition**: IoT Connector retention buffer (150K events) handles Spring Boot downtime up to a point. If the network between gate PC and Spring Boot app is down AND the reader can't reach the POST endpoint, tags queue internally. Monitor the reader's health events for queue-full warnings.

6. **Spring Boot SSE scalability**: SSE is fine for 1–50 concurrent browser connections. If you want multiple guards at multiple stations viewing the same feed, switch to WebSockets or use a short-polling fallback before it becomes a bottleneck.

---

## Ready for Proposal

**Yes** — the exploration is concrete enough for a proposal. Key decisions locked:

- **Architecture**: Option B (IoT Connector + HTTP POST) as default; Option A (SDK) as fallback if firmware is insufficient.
- **Database**: 5 tables designed; `readers` table from day 1 even for single-gate demo.
- **Frontend**: SSE-based real-time feed; component tree scoped for demo.
- **Stack**: Spring Boot 3.x + React 18+ (Vite) + PostgreSQL (via Spring profile) + IoT Connector HTTP mode.

No blockers. Proceed to `sdd-propose`.

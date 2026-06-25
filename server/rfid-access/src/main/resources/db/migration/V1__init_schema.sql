-- ============================================================
-- RFID Access Control — Initial Schema
-- V1__init_schema.sql
-- ============================================================

-- Readers / Gates dimension (multi-gate from day 1)
CREATE TABLE readers (
    id          BIGSERIAL PRIMARY KEY,
    name        VARCHAR(100) NOT NULL,           -- "Gate 1 - Main Entrance"
    serial      VARCHAR(50)  UNIQUE NOT NULL,     -- FX9600 serial
    ip_address  VARCHAR(45)  NOT NULL,
    location    VARCHAR(100),
    active      BOOLEAN DEFAULT true,
    created_at  TIMESTAMPTZ DEFAULT NOW()
);

-- Country club members (socios)
CREATE TABLE members (
    id          BIGSERIAL PRIMARY KEY,
    first_name  VARCHAR(100) NOT NULL,
    last_name   VARCHAR(100) NOT NULL,
    email       VARCHAR(255)  UNIQUE,
    phone       VARCHAR(20),
    photo_url   VARCHAR(500),
    member_code VARCHAR(50)   UNIQUE,             -- socio number
    active      BOOLEAN DEFAULT true,
    created_at  TIMESTAMPTZ DEFAULT NOW(),
    updated_at  TIMESTAMPTZ DEFAULT NOW()
);

-- Vehicles registered to members
CREATE TABLE vehicles (
    id          BIGSERIAL PRIMARY KEY,
    member_id   BIGINT  REFERENCES members(id) ON DELETE CASCADE,
    plate       VARCHAR(20) NOT NULL,
    brand       VARCHAR(50),
    model       VARCHAR(50),
    color       VARCHAR(30),
    created_at  TIMESTAMPTZ DEFAULT NOW()
);

-- RFID tags (EPC registrations)
-- A tag may be bound to a member (personal tag) or to a member+vehicle (windshield tag)
CREATE TABLE rfid_tags (
    id          BIGSERIAL PRIMARY KEY,
    epc         VARCHAR(64) UNIQUE NOT NULL,      -- hex EPC (96-bit)
    member_id   BIGINT  REFERENCES members(id)  ON DELETE CASCADE,
    vehicle_id  BIGINT  REFERENCES vehicles(id) ON DELETE SET NULL, -- nullable
    assigned_at TIMESTAMPTZ DEFAULT NOW(),
    revoked_at  TIMESTAMPTZ,                     -- NULL = active, NOT NULL = returned/revoked
    active      BOOLEAN DEFAULT true,
    CONSTRAINT  uq_tag_member_vehicle UNIQUE (member_id, vehicle_id)
);

-- Access events (append-only audit log)
CREATE TABLE access_log (
    id               BIGSERIAL PRIMARY KEY,
    tag_epc          VARCHAR(64) NOT NULL,
    reader_id        VARCHAR(50) NOT NULL,
    member_id        BIGINT  REFERENCES members(id) ON DELETE SET NULL,
    vehicle_id       BIGINT  REFERENCES vehicles(id) ON DELETE SET NULL,
    authorized       BOOLEAN NOT NULL,
    reason           VARCHAR(255),               -- "authorized", "tag not found", "member inactive"
    read_count       INT DEFAULT 1,              -- duplicate reads within burst
    tag_timestamp    TIMESTAMPTZ NOT NULL,       -- as reported by reader/IoT Connector
    server_timestamp TIMESTAMPTZ DEFAULT NOW(),
    created_at       TIMESTAMPTZ DEFAULT NOW()
);

-- ============================================================
-- Indexes
-- ============================================================

CREATE INDEX idx_access_log_reader_ts ON access_log(reader_id, server_timestamp);
CREATE INDEX idx_access_log_member_ts ON access_log(member_id, server_timestamp);
CREATE INDEX idx_access_log_tag_ts    ON access_log(tag_epc, server_timestamp);

CREATE INDEX idx_rfid_tags_epc        ON rfid_tags(epc);
CREATE INDEX idx_rfid_tags_member     ON rfid_tags(member_id);

CREATE INDEX idx_vehicles_member      ON vehicles(member_id);
CREATE INDEX idx_vehicles_plate       ON vehicles(plate);

CREATE INDEX idx_members_email        ON members(email);
CREATE INDEX idx_members_code         ON members(member_code);
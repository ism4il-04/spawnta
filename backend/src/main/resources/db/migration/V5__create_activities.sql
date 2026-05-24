-- V5: Activities & Participation tables with PostGIS spatial columns

-- Enable PostGIS extension (idempotent)
CREATE EXTENSION IF NOT EXISTS postgis;

-- Activities table
CREATE TABLE activities (
    id                  BIGSERIAL PRIMARY KEY,
    title               VARCHAR(150) NOT NULL,
    description         TEXT,
    activity_type       VARCHAR(20) NOT NULL,           -- MEETUP or TRIP
    participation_mode  VARCHAR(20) NOT NULL DEFAULT 'DIRECT', -- DIRECT or APPROVAL
    max_participants    INTEGER,                        -- NULL = unlimited
    scheduled_at        TIMESTAMP NOT NULL,
    duration_minutes    INTEGER,
    category            VARCHAR(50),

    -- Spatial columns (SRID 4326 = WGS84)
    location            GEOMETRY(Point, 4326),          -- for MEETUP
    start_location      GEOMETRY(Point, 4326),          -- for TRIP
    destination         GEOMETRY(Point, 4326),          -- for TRIP
    address             VARCHAR(255),                   -- human-readable address

    host_id             BIGINT NOT NULL REFERENCES users(id),
    created_at          TIMESTAMP NOT NULL DEFAULT now(),
    updated_at          TIMESTAMP NOT NULL DEFAULT now()
);

-- Spatial indexes for proximity queries
CREATE INDEX idx_activities_location ON activities USING GIST (location);
CREATE INDEX idx_activities_start_location ON activities USING GIST (start_location);

-- Participation join table
CREATE TABLE activity_participants (
    id              BIGSERIAL PRIMARY KEY,
    activity_id     BIGINT NOT NULL REFERENCES activities(id) ON DELETE CASCADE,
    user_id         BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    status          VARCHAR(20) NOT NULL DEFAULT 'PENDING',  -- PENDING, APPROVED, DECLINED
    intro_message   VARCHAR(150),
    joined_at       TIMESTAMP NOT NULL DEFAULT now(),
    UNIQUE (activity_id, user_id)
);

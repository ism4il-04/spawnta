-- V6: Real-Time Chat System Tables

-- Chats table
CREATE TABLE IF NOT EXISTS chats (
    id          BIGSERIAL PRIMARY KEY,
    type        VARCHAR(20) NOT NULL,              -- GROUP or PRIVATE
    activity_id BIGINT REFERENCES activities(id) ON DELETE SET NULL, -- for GROUP chat
    status      VARCHAR(20) NOT NULL DEFAULT 'ACTIVE', -- ACTIVE, ARCHIVED, BLOCKED
    created_at  TIMESTAMP NOT NULL DEFAULT now()
);

-- Index for quickly loading chats by activity
CREATE INDEX IF NOT EXISTS idx_chats_activity_id ON chats(activity_id);

-- Chat Participants table
CREATE TABLE IF NOT EXISTS chat_participants (
    id                     BIGSERIAL PRIMARY KEY,
    chat_id                BIGINT NOT NULL REFERENCES chats(id) ON DELETE CASCADE,
    user_id                BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    status                 VARCHAR(20) NOT NULL DEFAULT 'ACTIVE', -- ACTIVE, MUTED, KICKED
    notifications_enabled  BOOLEAN NOT NULL DEFAULT TRUE,
    joined_at              TIMESTAMP NOT NULL DEFAULT now(),
    UNIQUE (chat_id, user_id)
);

CREATE INDEX IF NOT EXISTS idx_chat_participants_user_id ON chat_participants(user_id);

-- Messages table
CREATE TABLE IF NOT EXISTS messages (
    id           BIGSERIAL PRIMARY KEY,
    chat_id      BIGINT NOT NULL REFERENCES chats(id) ON DELETE CASCADE,
    sender_id    BIGINT REFERENCES users(id) ON DELETE SET NULL, -- Anonymize if user deleted
    content      VARCHAR(2000) NOT NULL,
    status       VARCHAR(20) NOT NULL DEFAULT 'ACTIVE', -- ACTIVE, DELETED
    report_count INTEGER NOT NULL DEFAULT 0,
    created_at   TIMESTAMP NOT NULL DEFAULT now()
);

CREATE INDEX IF NOT EXISTS idx_messages_chat_id ON messages(chat_id);
CREATE INDEX IF NOT EXISTS idx_messages_created_at ON messages(created_at);

-- Transactional Outbox table for Kafka events
CREATE TABLE IF NOT EXISTS outbox_events (
    id          BIGSERIAL PRIMARY KEY,
    topic       VARCHAR(100) NOT NULL,
    payload     TEXT NOT NULL,
    status      VARCHAR(20) NOT NULL DEFAULT 'PENDING', -- PENDING, SENT, FAILED
    retry_count INTEGER NOT NULL DEFAULT 0,
    created_at  TIMESTAMP NOT NULL DEFAULT now()
);

CREATE INDEX IF NOT EXISTS idx_outbox_events_status ON outbox_events(status);

-- Chat Moderation Audit Logs
CREATE TABLE IF NOT EXISTS chat_audit_logs (
    id             BIGSERIAL PRIMARY KEY,
    moderator_id   BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    chat_id        BIGINT NOT NULL REFERENCES chats(id) ON DELETE CASCADE,
    action         VARCHAR(100) NOT NULL, -- KICK, MUTE, DELETE_MESSAGE
    target_user_id BIGINT REFERENCES users(id) ON DELETE SET NULL,
    details        VARCHAR(255),
    created_at     TIMESTAMP NOT NULL DEFAULT now()
);

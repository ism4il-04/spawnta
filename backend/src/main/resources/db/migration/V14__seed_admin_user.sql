-- ============================================================
-- V14: Seed default admin user
--
-- This migration inserts a bootstrap admin account the first
-- time the schema is created (DO NOTHING if already present).
--
-- The bcrypt hash below encodes the password: "ChangeMe123!"
-- It is ONLY a fallback for environments where the AdminSeeder
-- Spring component cannot run (e.g. bare SQL restore).
--
-- In all real deployments the AdminSeeder overwrites this hash
-- with the value of the ADMIN_PASSWORD env-var on first startup,
-- so the plain-text password below never reaches production.
-- ============================================================

INSERT INTO users (
    email,
    password,
    first_name,
    last_name,
    role,
    email_verified,
    profile_public,
    is_banned,
    created_at
)
VALUES (
    'admin@spawnta.com',
    -- bcrypt hash of "ChangeMe123!" (cost=10)
    '$2a$10$TqHHmvOj8tMuJETPPOFxvOVNiGbXcJLJi.gVjBMQg0qVDOHxPbNhS',
    'Admin',
    'Spawnta',
    'ADMIN',
    TRUE,
    FALSE,
    FALSE,
    NOW()
)
ON CONFLICT (email) DO NOTHING;

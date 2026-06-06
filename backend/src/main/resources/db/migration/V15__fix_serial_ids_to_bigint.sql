-- Align existing SERIAL integer ids with JPA Long ids used by the entities.
-- New databases get BIGSERIAL from the corrected original migrations; this
-- migration repairs databases where V7/V8/V9 already ran.

ALTER TABLE IF EXISTS user_level_history
    ALTER COLUMN id TYPE BIGINT;

ALTER TABLE IF EXISTS activity_attendance
    ALTER COLUMN id TYPE BIGINT;

ALTER TABLE IF EXISTS attendance_evidence
    ALTER COLUMN id TYPE BIGINT;

ALTER TABLE IF EXISTS activity_ratings
    ALTER COLUMN id TYPE BIGINT;

ALTER TABLE IF EXISTS activity_recommendations
    ALTER COLUMN id TYPE BIGINT;

ALTER TABLE IF EXISTS user_notifications
    ALTER COLUMN id TYPE BIGINT;

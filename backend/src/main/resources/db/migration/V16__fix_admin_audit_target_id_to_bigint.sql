-- Align admin audit target ids with the AdminAuditLog.targetId Long mapping.

ALTER TABLE IF EXISTS admin_audit_logs
    ALTER COLUMN target_id TYPE BIGINT;

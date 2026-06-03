-- ============================================
-- Admin & Moderation Tables (Phase 5)
-- ============================================

-- Admin Audit Log table
CREATE TABLE IF NOT EXISTS admin_audit_logs (
    id SERIAL PRIMARY KEY,
    admin_id INTEGER NOT NULL REFERENCES users(id),
    action VARCHAR(100) NOT NULL,
    target_type VARCHAR(50) NOT NULL, -- USER, ACTIVITY, REPORT, etc.
    target_id INTEGER,
    details TEXT,
    ip_address VARCHAR(45),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- User Reports table
CREATE TABLE IF NOT EXISTS user_reports (
    id SERIAL PRIMARY KEY,
    reported_by_id INTEGER NOT NULL REFERENCES users(id),
    reported_user_id INTEGER NOT NULL REFERENCES users(id),
    reason VARCHAR(100) NOT NULL,
    description TEXT NOT NULL,
    status VARCHAR(50) DEFAULT 'OPEN', -- OPEN, INVESTIGATING, RESOLVED, DISMISSED
    resolution_notes TEXT,
    resolved_by_id INTEGER REFERENCES users(id),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    resolved_at TIMESTAMP
);

-- Activity Reports table
CREATE TABLE IF NOT EXISTS activity_reports (
    id SERIAL PRIMARY KEY,
    reported_by_id INTEGER NOT NULL REFERENCES users(id),
    activity_id INTEGER NOT NULL REFERENCES activities(id),
    reason VARCHAR(100) NOT NULL,
    description TEXT NOT NULL,
    status VARCHAR(50) DEFAULT 'OPEN', -- OPEN, INVESTIGATING, RESOLVED, DISMISSED
    resolution_notes TEXT,
    resolved_by_id INTEGER REFERENCES users(id),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    resolved_at TIMESTAMP
);

-- Moderation Actions table
CREATE TABLE IF NOT EXISTS moderation_actions (
    id SERIAL PRIMARY KEY,
    action_type VARCHAR(50) NOT NULL, -- WARN, SUSPEND, BAN, RESTORE
    target_type VARCHAR(50) NOT NULL, -- USER, ACTIVITY
    target_id INTEGER NOT NULL,
    reason VARCHAR(255) NOT NULL,
    initiated_by_id INTEGER NOT NULL REFERENCES users(id),
    suspension_end_date TIMESTAMP, -- For SUSPEND actions
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Update users table to add suspension fields
ALTER TABLE users ADD COLUMN IF NOT EXISTS suspended_until TIMESTAMP;
ALTER TABLE users ADD COLUMN IF NOT EXISTS suspension_reason VARCHAR(255);
ALTER TABLE users ADD COLUMN IF NOT EXISTS is_banned BOOLEAN DEFAULT FALSE;

-- Indexes
CREATE INDEX idx_admin_audit_logs_admin_id ON admin_audit_logs(admin_id);
CREATE INDEX idx_admin_audit_logs_created_at ON admin_audit_logs(created_at);
CREATE INDEX idx_admin_audit_logs_action ON admin_audit_logs(action);
CREATE INDEX idx_user_reports_status ON user_reports(status);
CREATE INDEX idx_user_reports_created_at ON user_reports(created_at);
CREATE INDEX idx_user_reports_reported_user_id ON user_reports(reported_user_id);
CREATE INDEX idx_activity_reports_status ON activity_reports(status);
CREATE INDEX idx_activity_reports_created_at ON activity_reports(created_at);
CREATE INDEX idx_moderation_actions_target_id ON moderation_actions(target_id);
CREATE INDEX idx_moderation_actions_action_type ON moderation_actions(action_type);
CREATE INDEX idx_users_suspended_until ON users(suspended_until);
CREATE INDEX idx_users_is_banned ON users(is_banned);

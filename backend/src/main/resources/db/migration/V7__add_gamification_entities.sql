-- =============================================
-- V7: Gamification Entities (XP, Levels, Badges)
-- =============================================

-- Add gamification columns to users
ALTER TABLE users ADD COLUMN IF NOT EXISTS xp INTEGER NOT NULL DEFAULT 0;
ALTER TABLE users ADD COLUMN IF NOT EXISTS level INTEGER NOT NULL DEFAULT 1;
ALTER TABLE users ADD COLUMN IF NOT EXISTS total_xp_earned INTEGER NOT NULL DEFAULT 0;
ALTER TABLE users ADD COLUMN IF NOT EXISTS current_level_xp_required INTEGER NOT NULL DEFAULT 1000;

-- Badges catalog
CREATE TABLE IF NOT EXISTS badges (
    id SERIAL PRIMARY KEY,
    name VARCHAR(100) UNIQUE NOT NULL,
    description TEXT,
    icon_url VARCHAR(500),
    xp_reward INTEGER,
    created_at TIMESTAMP NOT NULL DEFAULT NOW()
);

-- User-Badge join table (achievements)
CREATE TABLE IF NOT EXISTS user_achievements (
    user_id BIGINT NOT NULL,
    badge_id INTEGER NOT NULL,
    earned_at TIMESTAMP NOT NULL DEFAULT NOW(),
    PRIMARY KEY (user_id, badge_id),
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (badge_id) REFERENCES badges(id) ON DELETE CASCADE
);

-- Level-up history for auditing
CREATE TABLE IF NOT EXISTS user_level_history (
    id SERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    old_level INTEGER,
    new_level INTEGER,
    xp_at_time INTEGER,
    achieved_at TIMESTAMP NOT NULL DEFAULT NOW(),
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- Seed default badges
INSERT INTO badges (name, description, icon_url, xp_reward, created_at) VALUES
    ('Explorer',         'Complete your first activity',              '/assets/badges/explorer.svg',          50, NOW()),
    ('Social Butterfly', 'Participate in 5 activities',              '/assets/badges/social-butterfly.svg', 100, NOW()),
    ('Activity Master',  'Host 5 activities',                        '/assets/badges/activity-master.svg',  200, NOW()),
    ('Reliable',         'Perfect attendance for 10 activities',     '/assets/badges/reliable.svg',         150, NOW()),
    ('Adventurer',       'Reach level 10',                           '/assets/badges/adventurer.svg',       300, NOW()),
    ('Trailblazer',      'Participate in 20 activities',             '/assets/badges/trailblazer.svg',      250, NOW()),
    ('Community Leader', 'Host 15 activities',                       '/assets/badges/community-leader.svg', 400, NOW()),
    ('Veteran',          'Reach level 25',                           '/assets/badges/veteran.svg',          500, NOW())
ON CONFLICT (name) DO NOTHING;

-- =============================================
-- V9: Add Recommendations & Notifications Schema
-- =============================================

CREATE TABLE activity_recommendations (
    id SERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    activity_id BIGINT NOT NULL,
    score DECIMAL(5,2),
    reason VARCHAR(100),
    recommended_at TIMESTAMP NOT NULL DEFAULT NOW(),
    clicked BOOLEAN NOT NULL DEFAULT FALSE,
    clicked_at TIMESTAMP,
    UNIQUE(user_id, activity_id),
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (activity_id) REFERENCES activities(id) ON DELETE CASCADE
);

CREATE TABLE user_notifications (
    id SERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    type VARCHAR(30) NOT NULL,
    title VARCHAR(200) NOT NULL,
    message TEXT,
    related_activity_id BIGINT,
    related_user_id BIGINT,
    is_read BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    read_at TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (related_activity_id) REFERENCES activities(id) ON DELETE SET NULL,
    FOREIGN KEY (related_user_id) REFERENCES users(id) ON DELETE SET NULL
);

CREATE INDEX idx_notifications_user_unread ON user_notifications(user_id, is_read);
CREATE INDEX idx_recommendations_user ON activity_recommendations(user_id);

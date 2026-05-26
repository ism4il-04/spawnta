-- =============================================
-- V8: Add Activity Attendance & Validation
-- =============================================

CREATE TABLE activity_attendance (
    id SERIAL PRIMARY KEY,
    activity_id BIGINT NOT NULL,
    participant_id BIGINT NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    check_in_time TIMESTAMP,
    check_out_time TIMESTAMP,
    confirmed_by_host BOOLEAN NOT NULL DEFAULT FALSE,
    confirmed_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    UNIQUE(activity_id, participant_id),
    FOREIGN KEY (activity_id) REFERENCES activities(id) ON DELETE CASCADE,
    FOREIGN KEY (participant_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE TABLE attendance_evidence (
    id SERIAL PRIMARY KEY,
    attendance_id BIGINT NOT NULL,
    photo_url VARCHAR(500),
    geolocation GEOMETRY(Point, 4326),
    uploaded_at TIMESTAMP NOT NULL DEFAULT NOW(),
    FOREIGN KEY (attendance_id) REFERENCES activity_attendance(id) ON DELETE CASCADE
);

CREATE TABLE activity_ratings (
    id SERIAL PRIMARY KEY,
    activity_id BIGINT NOT NULL,
    rater_id BIGINT NOT NULL,
    rating_score INTEGER NOT NULL CHECK(rating_score >= 1 AND rating_score <= 5),
    comment TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    UNIQUE(activity_id, rater_id),
    FOREIGN KEY (activity_id) REFERENCES activities(id) ON DELETE CASCADE,
    FOREIGN KEY (rater_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE INDEX idx_activity_attendance_activity ON activity_attendance(activity_id);
CREATE INDEX idx_activity_attendance_participant ON activity_attendance(participant_id);
CREATE INDEX idx_activity_ratings_activity ON activity_ratings(activity_id);

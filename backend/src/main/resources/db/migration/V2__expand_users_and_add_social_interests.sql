-- ─── Expand users table with profile fields ───
ALTER TABLE users ADD COLUMN username VARCHAR(100) UNIQUE;
ALTER TABLE users ADD COLUMN date_of_birth DATE;
ALTER TABLE users ADD COLUMN city VARCHAR(255);
ALTER TABLE users ADD COLUMN level INTEGER NOT NULL DEFAULT 1;
ALTER TABLE users ADD COLUMN xp INTEGER NOT NULL DEFAULT 0;
ALTER TABLE users ADD COLUMN profile_picture_url VARCHAR(512);
ALTER TABLE users ADD COLUMN is_premium BOOLEAN NOT NULL DEFAULT FALSE;
ALTER TABLE users ADD COLUMN is_banned BOOLEAN NOT NULL DEFAULT FALSE;
ALTER TABLE users ADD COLUMN is_email_verified BOOLEAN NOT NULL DEFAULT FALSE;
ALTER TABLE users ADD COLUMN bio VARCHAR(300);

-- ─── Create interests table ───
CREATE TABLE interests (
    id SERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE,
    icon_url VARCHAR(255)
);

-- ─── Create user_interests join table ───
CREATE TABLE user_interests (
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    interest_id INTEGER NOT NULL REFERENCES interests(id) ON DELETE CASCADE,
    PRIMARY KEY (user_id, interest_id)
);

-- ─── Create user_photos table (gallery) ───
CREATE TABLE user_photos (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    url VARCHAR(512) NOT NULL,
    caption VARCHAR(100),
    uploaded_at TIMESTAMP NOT NULL DEFAULT now()
);

-- ─── Create user_social_links table ───
CREATE TABLE user_social_links (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    platform VARCHAR(50) NOT NULL,
    value VARCHAR(255) NOT NULL,
    CONSTRAINT unique_user_platform UNIQUE (user_id, platform)
);

-- ─── Create user_countries_visited table ───
CREATE TABLE user_countries_visited (
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    country_code VARCHAR(10) NOT NULL,
    PRIMARY KEY (user_id, country_code)
);

-- ─── Seed default interests ───
INSERT INTO interests (name, icon_url) VALUES
('Hiking & Trekking', 'landscape'),
('Coffee & Cafés', 'local_cafe'),
('Road Trips', 'directions_car'),
('Beach & Swimming', 'pool'),
('Cultural Visits (Museums, Ruins)', 'museum'),
('Photography', 'photo_camera'),
('Cycling', 'directions_bike'),
('Nightlife', 'local_bar'),
('Food & Restaurants', 'restaurant'),
('Camping', 'campaign'),
('Fitness & Sports', 'sports_soccer'),
('Volunteering', 'volunteer_activism'),
('Language Exchange', 'translate'),
('Reading / Book Clubs', 'menu_book'),
('Board Games', 'casino');

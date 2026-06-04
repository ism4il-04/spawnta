-- V2: Rich user profile columns + collection tables

ALTER TABLE users
    ADD COLUMN IF NOT EXISTS bio              TEXT,
    ADD COLUMN IF NOT EXISTS avatar_url       VARCHAR(500),
    ADD COLUMN IF NOT EXISTS facebook         VARCHAR(255),
    ADD COLUMN IF NOT EXISTS instagram        VARCHAR(255),
    ADD COLUMN IF NOT EXISTS whatsapp         VARCHAR(50),
    ADD COLUMN IF NOT EXISTS profile_public   BOOLEAN NOT NULL DEFAULT TRUE;

CREATE TABLE IF NOT EXISTS user_interests (
    user_id  BIGINT      NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    interest VARCHAR(50) NOT NULL,
    PRIMARY KEY (user_id, interest)
);

CREATE TABLE IF NOT EXISTS user_gallery (
    id        BIGSERIAL    PRIMARY KEY,
    user_id   BIGINT       NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    photo_url VARCHAR(500) NOT NULL
);

CREATE TABLE IF NOT EXISTS user_visited_countries (
    id           BIGSERIAL   PRIMARY KEY,
    user_id      BIGINT      NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    country_code VARCHAR(3)  NOT NULL
);

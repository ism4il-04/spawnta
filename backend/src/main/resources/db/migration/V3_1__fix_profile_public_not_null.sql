-- V3: Fix profile_public column — set default for existing NULL rows and add NOT NULL constraint
UPDATE users SET profile_public = TRUE WHERE profile_public IS NULL;
ALTER TABLE users ALTER COLUMN profile_public SET NOT NULL;
ALTER TABLE users ALTER COLUMN profile_public SET DEFAULT TRUE;

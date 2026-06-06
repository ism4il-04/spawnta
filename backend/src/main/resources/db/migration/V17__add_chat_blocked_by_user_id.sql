-- Chat blocking support used by Chat.blockedByUserId.

ALTER TABLE IF EXISTS chats
    ADD COLUMN IF NOT EXISTS blocked_by_user_id BIGINT;

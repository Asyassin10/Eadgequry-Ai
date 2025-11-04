-- Refactor user_profiles table for profile-only data
-- Remove email (stays in auth service)
-- Add preferences for user settings

ALTER TABLE user_profiles
    DROP COLUMN email,
    DROP INDEX idx_email,
    ADD COLUMN preferences JSON NULL COMMENT 'User preferences and settings' AFTER bio;

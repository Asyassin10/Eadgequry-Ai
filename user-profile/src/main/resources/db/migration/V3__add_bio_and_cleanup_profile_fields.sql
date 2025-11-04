-- Add bio field and clean up table structure
-- Remove fields not needed for profile service

ALTER TABLE user_profiles
    ADD COLUMN bio TEXT NULL AFTER avatar_url,
    DROP COLUMN email_verified_at,
    DROP COLUMN provider,
    DROP COLUMN google_id,
    DROP INDEX idx_google_id;

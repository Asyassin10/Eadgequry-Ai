-- Drop name column from users table
-- Name is now only stored in the user_profiles table in the Profile Service
ALTER TABLE users DROP COLUMN name;

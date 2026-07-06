-- Create user role enum
CREATE TYPE user_role AS ENUM ('USER', 'ADMIN');

-- Create users table
CREATE TABLE users (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    username            VARCHAR(50)  NOT NULL UNIQUE,
    email               VARCHAR(255) NOT NULL UNIQUE,
    password_hash       VARCHAR(255) NOT NULL,
    display_name        VARCHAR(100),
    bio                 TEXT,
    profile_picture_url TEXT,
    banner_image_url    TEXT,
    role                VARCHAR(20)  NOT NULL DEFAULT 'USER',
    is_verified         BOOLEAN      NOT NULL DEFAULT FALSE,
    is_suspended        BOOLEAN      NOT NULL DEFAULT FALSE,
    email_verified      BOOLEAN      NOT NULL DEFAULT FALSE,
    follower_count      INTEGER      NOT NULL DEFAULT 0,
    following_count     INTEGER      NOT NULL DEFAULT 0,
    post_count          INTEGER      NOT NULL DEFAULT 0,
    created_at          TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at          TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    deleted_at          TIMESTAMPTZ
);

-- Indexes
CREATE INDEX idx_users_username    ON users (username);
CREATE INDEX idx_users_email       ON users (email);
CREATE INDEX idx_users_created_at  ON users (created_at DESC);
CREATE INDEX idx_users_deleted_at  ON users (deleted_at) WHERE deleted_at IS NULL;

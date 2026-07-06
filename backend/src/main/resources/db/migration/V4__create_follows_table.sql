-- Follows table (composite PK: follower_id + followee_id)
-- Populated by Follow/Unfollow API in Sprint 5.
-- Created now so home timeline queries have the table to JOIN against.
CREATE TABLE follows (
    follower_id UUID        NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    followee_id UUID        NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    created_at  TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    PRIMARY KEY (follower_id, followee_id),
    CONSTRAINT no_self_follow CHECK (follower_id != followee_id)
);

CREATE INDEX idx_follows_follower_id ON follows (follower_id);
CREATE INDEX idx_follows_followee_id ON follows (followee_id);

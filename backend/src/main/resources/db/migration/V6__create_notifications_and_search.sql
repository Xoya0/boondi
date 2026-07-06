-- Notifications: one row per (recipient, actor, type, post) event.
-- post_id is null for FOLLOW notifications (no post involved).
CREATE TABLE notifications (
    id           UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    recipient_id UUID        NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    actor_id     UUID        NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    type         VARCHAR(20) NOT NULL,
    post_id      UUID        REFERENCES posts(id) ON DELETE CASCADE,
    is_read      BOOLEAN     NOT NULL DEFAULT FALSE,
    created_at   TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_notifications_recipient_created ON notifications (recipient_id, created_at DESC);
CREATE INDEX idx_notifications_unread ON notifications (recipient_id) WHERE is_read = FALSE;

-- Hashtags: extracted from post content on create (#word patterns), stored lowercase.
CREATE TABLE hashtags (
    id         UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
    tag        VARCHAR(100) NOT NULL UNIQUE,
    created_at TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);

CREATE TABLE post_hashtags (
    post_id    UUID NOT NULL REFERENCES posts(id) ON DELETE CASCADE,
    hashtag_id UUID NOT NULL REFERENCES hashtags(id) ON DELETE CASCADE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    PRIMARY KEY (post_id, hashtag_id)
);

CREATE INDEX idx_post_hashtags_hashtag_id ON post_hashtags (hashtag_id);
CREATE INDEX idx_hashtags_tag ON hashtags (tag);

-- Full-text search on post content (English config; generated + indexed for E8-02).
ALTER TABLE posts ADD COLUMN search_vector tsvector
    GENERATED ALWAYS AS (to_tsvector('english', content)) STORED;

CREATE INDEX idx_posts_search_vector ON posts USING GIN (search_vector);

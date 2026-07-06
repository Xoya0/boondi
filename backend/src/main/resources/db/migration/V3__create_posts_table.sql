-- Create posts table
CREATE TABLE posts (
    id              UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    author_id       UUID        NOT NULL REFERENCES users(id),
    content         TEXT        NOT NULL,
    image_url       TEXT,
    like_count      INTEGER     NOT NULL DEFAULT 0,
    repost_count    INTEGER     NOT NULL DEFAULT 0,
    reply_count     INTEGER     NOT NULL DEFAULT 0,
    bookmark_count  INTEGER     NOT NULL DEFAULT 0,
    parent_post_id  UUID        REFERENCES posts(id),
    quoted_post_id  UUID        REFERENCES posts(id),
    is_edited       BOOLEAN     NOT NULL DEFAULT FALSE,
    edited_at       TIMESTAMPTZ,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    deleted_at      TIMESTAMPTZ
);

-- Indexes for common access patterns
CREATE INDEX idx_posts_author_id   ON posts (author_id);
CREATE INDEX idx_posts_created_at  ON posts (created_at DESC);
CREATE INDEX idx_posts_deleted_at  ON posts (deleted_at) WHERE deleted_at IS NULL;
CREATE INDEX idx_posts_parent_post ON posts (parent_post_id) WHERE parent_post_id IS NOT NULL;

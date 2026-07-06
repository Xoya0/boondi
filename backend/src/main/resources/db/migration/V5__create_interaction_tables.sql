-- Social interaction tables: likes, reposts, bookmarks.
-- Each is a (user_id, post_id) pair with composite PK — one action per user per post.
-- Counters live on posts (like_count / repost_count / bookmark_count) per design decision.

CREATE TABLE post_likes (
    user_id    UUID        NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    post_id    UUID        NOT NULL REFERENCES posts(id) ON DELETE CASCADE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    PRIMARY KEY (user_id, post_id)
);

CREATE TABLE post_reposts (
    user_id    UUID        NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    post_id    UUID        NOT NULL REFERENCES posts(id) ON DELETE CASCADE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    PRIMARY KEY (user_id, post_id)
);

CREATE TABLE post_bookmarks (
    user_id    UUID        NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    post_id    UUID        NOT NULL REFERENCES posts(id) ON DELETE CASCADE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    PRIMARY KEY (user_id, post_id)
);

CREATE INDEX idx_post_likes_post_id     ON post_likes (post_id);
CREATE INDEX idx_post_reposts_post_id   ON post_reposts (post_id);
CREATE INDEX idx_post_bookmarks_post_id ON post_bookmarks (post_id);

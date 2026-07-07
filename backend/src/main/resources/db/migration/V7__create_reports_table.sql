-- Reports (E9-04/E9-05): a user reports either another user or a post — exactly one target.
CREATE TABLE reports (
    id                UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    reporter_id       UUID        NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    reported_user_id  UUID        REFERENCES users(id) ON DELETE CASCADE,
    reported_post_id  UUID        REFERENCES posts(id) ON DELETE CASCADE,
    reason            TEXT        NOT NULL,
    created_at        TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT chk_reports_exactly_one_target CHECK (
        (CASE WHEN reported_user_id IS NOT NULL THEN 1 ELSE 0 END) +
        (CASE WHEN reported_post_id IS NOT NULL THEN 1 ELSE 0 END) = 1
    )
);

CREATE INDEX idx_reports_created_at ON reports (created_at DESC);

CREATE TABLE invite_links (
    token_hash  TEXT PRIMARY KEY,
    group_id    TEXT NOT NULL REFERENCES groups(id),
    created_by  TEXT NOT NULL,
    expires_at  TIMESTAMPTZ NOT NULL,
    created_at  TIMESTAMPTZ NOT NULL DEFAULT now()
);

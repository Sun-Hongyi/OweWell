CREATE TABLE groups (
    id          TEXT PRIMARY KEY,
    name        TEXT NOT NULL,
    created_at  TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE group_members (
    group_id  TEXT NOT NULL REFERENCES groups(id),
    user_id   TEXT NOT NULL,
    PRIMARY KEY (group_id, user_id)
);

CREATE TABLE ledger_events (
    group_id     TEXT NOT NULL REFERENCES groups(id),
    sequence     INT NOT NULL,
    type         TEXT NOT NULL,
    payload      JSONB NOT NULL,
    occurred_at  TIMESTAMPTZ NOT NULL DEFAULT now(),
    PRIMARY KEY (group_id, sequence)
);

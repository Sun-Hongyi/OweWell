-- A group is "deleted" when this is set. The row and its history stay in the database.
ALTER TABLE groups ADD COLUMN deleted_at TIMESTAMPTZ;

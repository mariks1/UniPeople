CREATE SCHEMA IF NOT EXISTS file;
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

CREATE TABLE IF NOT EXISTS files (
                                          id            UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
                                          owner_id      UUID        NOT NULL,
                                          owner_type    VARCHAR(50) NOT NULL,
    category      VARCHAR(50) NOT NULL,
    original_name VARCHAR(255) NOT NULL,
    content_type  VARCHAR(255) NOT NULL,
    size          BIGINT      NOT NULL,
    storage_path  TEXT        NOT NULL,
    uploaded_at   TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at    TIMESTAMPTZ NOT NULL DEFAULT NOW()
    );

CREATE INDEX IF NOT EXISTS idx_files_owner_id
    ON files (owner_id);

CREATE INDEX IF NOT EXISTS idx_files_uploaded_at
    ON files (uploaded_at DESC);

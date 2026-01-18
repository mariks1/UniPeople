CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

CREATE TABLE leave_request_attachment (
                                          id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
                                          leave_request_id UUID NOT NULL REFERENCES leave_request(id) ON DELETE CASCADE,
                                          file_id         UUID NOT NULL,
                                          uploaded_by     UUID NOT NULL,
                                          category        VARCHAR(64) NOT NULL,
                                          comment         VARCHAR(255),
                                          created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
                                          updated_at      TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_leave_att_request ON leave_request_attachment(leave_request_id);

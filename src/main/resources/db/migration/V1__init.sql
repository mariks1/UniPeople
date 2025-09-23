CREATE TABLE employee (
                          id UUID PRIMARY KEY,
                          first_name   VARCHAR(128) NOT NULL,
                          last_name    VARCHAR(128) NOT NULL,
                          middle_name  VARCHAR(128),
                          work_email   VARCHAR(256) UNIQUE,
                          phone        VARCHAR(64),
                          status       VARCHAR(16)  NOT NULL, -- ACTIVE | FIRED
                          created_at   TIMESTAMP    NOT NULL DEFAULT now(),
                          updated_at   TIMESTAMP    NOT NULL DEFAULT now(),
                          version      INT          NOT NULL DEFAULT 0
);

CREATE INDEX idx_employee_created_at ON employee(created_at DESC, id);

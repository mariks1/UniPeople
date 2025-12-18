create schema if not exists auth;

CREATE TABLE IF NOT EXISTS app_user (
                                        id               UUID PRIMARY KEY,
                                        username         VARCHAR(64)  NOT NULL,
    password_hash    VARCHAR(255) NOT NULL,
    employee_id      UUID,
    enabled          BOOLEAN      NOT NULL DEFAULT TRUE
    );

CREATE UNIQUE INDEX IF NOT EXISTS ux_app_user_username_ci
    ON app_user (LOWER(username));

CREATE TABLE IF NOT EXISTS user_roles (
                                          user_id UUID       NOT NULL REFERENCES app_user(id) ON DELETE CASCADE,
    role    VARCHAR(32) NOT NULL,
    PRIMARY KEY (user_id, role)
    );

CREATE TABLE IF NOT EXISTS user_managed_dept_ids (
                                                     user_id       UUID NOT NULL REFERENCES app_user(id) ON DELETE CASCADE,
    department_id UUID NOT NULL,
    PRIMARY KEY (user_id, department_id)
    );

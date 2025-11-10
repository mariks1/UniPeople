create schema if not exists org;

create table faculty (
                         id uuid primary key,
                         code varchar(64) not null unique,
                         name varchar(150) not null
);

create table department (
                            id uuid primary key,
                            faculty_id uuid not null,
                            code varchar(64) not null unique,
                            name varchar(150) not null,
                            head_employee_id uuid null,
                            created_at timestamptz,
                            updated_at timestamptz
);

create table position (
                          id uuid primary key,
                          name varchar(150) not null unique
);

ALTER TABLE position
  ADD COLUMN created_at timestamptz,
  ADD COLUMN updated_at timestamptz;

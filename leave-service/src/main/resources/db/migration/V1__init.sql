create schema if not exists leave;
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

create table leave_type (
                            id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
                            code varchar(64) not null unique,
                            name varchar(150) not null,
                            paid boolean not null default true,
                            max_days_per_year int
);

create table leave_request (
                               id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
                               employee_id uuid not null,
                               type_id uuid not null,
                               date_from date not null,
                               date_to date not null,
                               status varchar(20) not null default 'PENDING', -- DRAFT/PENDING/APPROVED/REJECTED/CANCELED
                               approver_id uuid,
                               comment text,
                               created_at timestamptz,
                               updated_at timestamptz
);
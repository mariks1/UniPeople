create schema if not exists leave;

create table leave_type (
                            id uuid primary key,
                            code varchar(64) not null unique,
                            name varchar(150) not null,
                            paid boolean not null default true,
                            max_days_per_year int
);

create table leave_request (
                               id uuid primary key,
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

create index idx_leave_request_emp on leave_request(employee_id, date_from);
create index idx_leave_request_status on leave_request(status);
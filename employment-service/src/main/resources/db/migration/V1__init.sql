create schema if not exists employment;

create table employment (
                            id uuid primary key,
                            employee_id uuid not null,
                            department_id uuid not null,
                            position_id uuid not null,
                            start_date date not null,
                            end_date date,
                            rate numeric(3,2) not null default 1.0,
                            salary integer,
                            status varchar(20) not null default 'ACTIVE', -- ACTIVE/CLOSED
                            created_at timestamptz,
                            updated_at timestamptz
);

create index idx_employment_emp on employment(employee_id, start_date);
create index idx_employment_dept on employment(department_id);
create index idx_employment_pos on employment(position_id);
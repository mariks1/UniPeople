create schema if not exists duty;

create table duty (
                      id uuid primary key,
                      code varchar(64) not null unique,
                      name varchar(150) not null
);

create table department_duty_assignment (
                                            id uuid primary key,
                                            department_id uuid not null,
                                            employee_id uuid not null,
                                            duty_id uuid not null,
                                            assigned_at timestamptz not null default now(),
                                            assigned_by uuid,
                                            note varchar(255)
);

create index idx_duty_assign_dept on department_duty_assignment(department_id);
create index idx_duty_assign_emp on department_duty_assignment(employee_id);
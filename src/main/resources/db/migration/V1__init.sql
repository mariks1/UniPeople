create table faculty (
                         id uuid primary key,
                         code varchar(64) not null unique,
                         name varchar(150) not null
);

create table department (
                            id uuid primary key,
                            faculty_id uuid not null references faculty(id) on delete restrict,
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


create table employee (
                          id uuid primary key,
                          version int,
                          first_name varchar(100) not null,
                          last_name  varchar(100) not null,
                          middle_name varchar(100),
                          work_email varchar(255) unique,
                          phone varchar(50),
                          status varchar(20) not null, -- ACTIVE/FIRED
                          department_id uuid null references department(id) on delete set null,
                          created_at timestamptz,
                          updated_at timestamptz
);

alter table department
    add constraint fk_department_head
        foreign key (head_employee_id) references employee(id) on delete set null;


create table employment (
                            id uuid primary key,
                            employee_id uuid not null references employee(id) on delete cascade,
                            department_id uuid not null references department(id) on delete restrict,
                            position_id uuid not null references position(id) on delete restrict,
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


create table leave_type (
                            id uuid primary key,
                            code varchar(64) not null unique,
                            name varchar(150) not null,
                            paid boolean not null default true,
                            max_days_per_year int
);

create table leave_request (
                               id uuid primary key,
                               employee_id uuid not null references employee(id) on delete cascade,
                               type_id uuid not null references leave_type(id) on delete restrict,
                               date_from date not null,
                               date_to date not null,
                               status varchar(20) not null default 'PENDING', -- DRAFT/PENDING/APPROVED/REJECTED/CANCELED
                               approver_id uuid references employee(id) on delete set null,
                               comment text,
                               created_at timestamptz,
                               updated_at timestamptz
);

create index idx_leave_request_emp on leave_request(employee_id, date_from);
create index idx_leave_request_status on leave_request(status);


create table duty (
                      id uuid primary key,
                      code varchar(64) not null unique,
                      name varchar(150) not null
);

create table department_duty_assignment (
                                            id uuid primary key,
                                            department_id uuid not null references department(id) on delete cascade,
                                            employee_id uuid not null references employee(id) on delete cascade,
                                            duty_id uuid not null references duty(id) on delete restrict,
                                            assigned_at timestamptz not null default now(),
                                            assigned_by uuid,
                                            note varchar(255)
);

create index idx_duty_assign_dept on department_duty_assignment(department_id);
create index idx_duty_assign_emp on department_duty_assignment(employee_id);
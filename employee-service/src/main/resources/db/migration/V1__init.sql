create schema if not exists employee;
create table employee (
                          id uuid primary key,
                          version int,
                          first_name varchar(100) not null,
                          last_name  varchar(100) not null,
                          middle_name varchar(100),
                          work_email varchar(255) unique,
                          phone varchar(50),
                          status varchar(20) not null, -- ACTIVE/FIRED
                          department_id uuid null,
                          created_at timestamptz,
                          updated_at timestamptz
);
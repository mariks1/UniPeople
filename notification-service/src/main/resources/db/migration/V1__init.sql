create schema if not exists notif;

create table if not exists notif.notification_event (
                                                        id uuid primary key default gen_random_uuid(),
    event_id uuid not null unique,
    created_at timestamptz not null,
    source varchar(64) not null,
    event_type varchar(64) not null,
    entity_id uuid,
    payload jsonb not null
    );

create index if not exists idx_ne_created_at on notif.notification_event(created_at desc);
create index if not exists idx_ne_event_type on notif.notification_event(event_type);
create index if not exists idx_ne_entity_id on notif.notification_event(entity_id);

create table if not exists notif.notification_inbox (
                                                        id uuid primary key default gen_random_uuid(),
    event_pk uuid not null references notif.notification_event(id) on delete cascade,
    recipient_employee_id uuid null,
    recipient_role varchar(64) null,
    delivered_at timestamptz not null default now(),
    read_at timestamptz null,
    deleted_at timestamptz null,
    constraint uk_ni_event_recipient_oneof unique(event_pk, recipient_employee_id, recipient_role)
    );

create index if not exists idx_ni_recipient_delivered
    on notif.notification_inbox(recipient_employee_id, delivered_at desc);
create index if not exists idx_ni_role_delivered
    on notif.notification_inbox(recipient_role, delivered_at desc);
create index if not exists idx_ni_read_at
    on notif.notification_inbox(read_at);
create index if not exists idx_ni_deleted_at
    on notif.notification_inbox(deleted_at);

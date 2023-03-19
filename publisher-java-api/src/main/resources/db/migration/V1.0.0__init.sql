CREATE TABLE IF NOT EXISTS permit
(
    permit_id   uuid primary key,
    permit_type varchar(20) NOT NULL,
    permit_area varchar     NOT NULL,
    created_at  timestamp default now(),
    created_by  varchar(20) NOT NULL,
    updated_at  timestamp default now(),
    updated_by  varchar(20)
);
comment on table permit is 'permit table';
comment on column permit.permit_id is 'permit id';
comment on column permit.permit_type is 'permit type';
comment on column permit.permit_area is 'permit area';

CREATE TABLE IF NOT EXISTS event
(
    id              uuid primary key,
    "type"          varchar     NOT NULL,
    source          varchar     NOT NULL,
    payload_version varchar     NOT NULL,
    "data"          jsonb       NOT NULL,
    subject         varchar     NOT NULL,
    created_at      timestamp default now(),
    created_by      varchar(20) NOT NULL,
    updated_at      timestamp default now(),
    updated_by      varchar(20)
);
comment on table event is 'event table';
comment on column event.id is 'event id the primary key of the event table';
comment on column event.type is 'event type, the type of the event, for example permit created or permit updated';
comment on column event.source is 'event source, the source of the event, for example permit service through message or http api';
comment on column event.payload_version is 'event payload version, make sure the payload is backward compatible, versioning makes it flexible to change the payload structure';
comment on column event.data is 'event data, the actual payload of the event, for example the permit data';
comment on column event.subject is 'event subject the subject ';


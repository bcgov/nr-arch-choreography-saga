CREATE TYPE permit_status_enum AS ENUM (
    'INITIATED',
    'APPROVED',
    'DECLINED'
    );
ALTER TABLE permit ADD COLUMN permit_status permit_status_enum;
comment on column permit.permit_status is 'The status of the permit';

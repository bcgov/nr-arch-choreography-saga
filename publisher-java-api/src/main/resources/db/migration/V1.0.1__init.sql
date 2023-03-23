ALTER TABLE permit ADD COLUMN permit_lat_long varchar;
comment on column permit.permit_lat_long is 'Latitude and longitude of the permit location';

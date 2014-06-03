CREATE TABLE units
(
  unit_id serial PRIMARY KEY NOT NULL,
  display_name varchar(100) NOT NULL
);

ALTER TABLE services RENAME COLUMN unit TO unit_id;
ALTER TABLE services ALTER COLUMN unit_id TYPE int USING unit_id::int;

ALTER TABLE services ADD CONSTRAINT fk_services_units FOREIGN KEY (unit_id)
REFERENCES units (unit_id)
ON DELETE CASCADE
ON UPDATE NO ACTION;

CREATE INDEX idx_services_units ON services (unit_id ASC);
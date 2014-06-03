ALTER TABLE persons DROP CONSTRAINT fk_persons_contacts;
ALTER TABLE organizations DROP CONSTRAINT fk_organizations_contacts;
ALTER TABLE requests DROP CONSTRAINT fk_requests_clients;
ALTER TABLE requests DROP CONSTRAINT fk_requests_executors;
ALTER TABLE users DROP CONSTRAINT fk_users_contacts;
DROP TABLE IF EXISTS contacts;
DROP TYPE IF EXISTS contact_type;

CREATE TABLE IF NOT EXISTS contacts (
  contact_id BIGSERIAL PRIMARY KEY ,
  contact_type SMALLINT NOT NULL ,
  num_requests INT NOT NULL ,
  created TIMESTAMP NOT NULL
);

ALTER TABLE users ADD CONSTRAINT fk_users_contacts
FOREIGN KEY (contact_id )
REFERENCES contacts (contact_id )
ON DELETE CASCADE
ON UPDATE NO ACTION;

ALTER TABLE requests ADD CONSTRAINT fk_requests_clients
FOREIGN KEY (client_id )
REFERENCES contacts (contact_id )
ON DELETE CASCADE
ON UPDATE NO ACTION;

ALTER TABLE requests ADD CONSTRAINT fk_requests_executors
FOREIGN KEY (executor_id )
REFERENCES contacts (contact_id )
ON DELETE CASCADE
ON UPDATE NO ACTION;

ALTER TABLE organizations ADD CONSTRAINT fk_organizations_contacts
FOREIGN KEY (contact_id )
REFERENCES contacts (contact_id )
ON DELETE CASCADE
ON UPDATE NO ACTION;

ALTER TABLE persons ADD CONSTRAINT fk_persons_contacts
FOREIGN KEY (contact_id )
REFERENCES contacts (contact_id )
ON DELETE CASCADE
ON UPDATE NO ACTION;
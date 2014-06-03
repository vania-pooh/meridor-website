ALTER TABLE persons DROP CONSTRAINT IF EXISTS persons_pkey;
ALTER TABLE persons ADD CONSTRAINT persons_pkey PRIMARY KEY (cell_phone);
ALTER TABLE persons ALTER COLUMN contact_id TYPE BIGINT;
DROP SEQUENCE IF EXISTS persons_contact_id_seq CASCADE;

ALTER TABLE organizations DROP CONSTRAINT IF EXISTS organizations_pkey;
ALTER TABLE organizations ADD CONSTRAINT organizations_pkey PRIMARY KEY (inn);
ALTER TABLE organizations ALTER COLUMN contact_id TYPE BIGINT;
DROP SEQUENCE IF EXISTS organizations_contact_id_seq CASCADE;
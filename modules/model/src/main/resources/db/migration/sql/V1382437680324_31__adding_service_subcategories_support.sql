ALTER TABLE service_groups ADD COLUMN parent_group_id INT NULL;
CREATE INDEX service_groups_parent_group ON service_groups (parent_group_id) WHERE parent_group_id IS NOT NULL;
ALTER TABLE service_groups ADD CONSTRAINT fk_service_groups_group_id
FOREIGN KEY (parent_group_id)
REFERENCES service_groups (group_id)
ON DELETE CASCADE
ON UPDATE CASCADE;
ALTER TABLE service_groups ADD COLUMN display_name VARCHAR(200);
CREATE INDEX idx_service_groups_group_name ON service_groups (group_name ASC);
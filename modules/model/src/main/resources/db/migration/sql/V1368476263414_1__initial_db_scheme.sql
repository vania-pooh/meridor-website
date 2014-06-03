-- Creating initial database schema

-- -----------------------------------------------------
-- Table contacts
-- -----------------------------------------------------
CREATE TYPE contact_type AS ENUM('person', 'organization');

CREATE TABLE IF NOT EXISTS contacts (
  contact_id BIGSERIAL PRIMARY KEY ,
  type contact_type NOT NULL ,
  num_requests INT NOT NULL ,
  created TIMESTAMP NOT NULL
);


-- -----------------------------------------------------
-- Table task_priorities
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS task_priorities (
  priority_id SERIAL4 PRIMARY KEY ,
  priority_name VARCHAR(200) NOT NULL ,
  icon VARCHAR(500) NULL
);


-- -----------------------------------------------------
-- Table task_categories
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS task_categories (
  category_id SERIAL4 PRIMARY KEY ,
  category_name VARCHAR(200) NOT NULL ,
  icon VARCHAR(500) NULL
);


-- -----------------------------------------------------
-- Table task_statuses
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS task_statuses (
  status_id SERIAL4 PRIMARY KEY ,
  status_name VARCHAR(200) NOT NULL ,
  icon VARCHAR(500) NULL
);


-- -----------------------------------------------------
-- Table tasks
-- -----------------------------------------------------
CREATE  TABLE IF NOT EXISTS tasks (
  task_id BIGSERIAL PRIMARY KEY ,
  start_date TIMESTAMP NOT NULL ,
  end_date TIMESTAMP NULL ,
  description VARCHAR(5000) NOT NULL ,
  category_id INT4 NOT NULL ,
  priority_id INT4 NOT NULL ,
  status_id INT4 NOT NULL ,
  paid_amount INT NOT NULL ,
  duration FLOAT NULL ,
  created TIMESTAMP NOT NULL ,
  CONSTRAINT fk_tasks_task_priorities
  FOREIGN KEY (priority_id )
  REFERENCES task_priorities (priority_id )
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT fk_tasks_task_categories
  FOREIGN KEY (category_id )
  REFERENCES task_categories (category_id )
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT fk_tasks_task_statuses
  FOREIGN KEY (status_id )
  REFERENCES task_statuses (status_id )
    ON DELETE NO ACTION
    ON UPDATE NO ACTION
);

CREATE INDEX idx_tasks_task_priorities ON tasks (priority_id ASC);
CREATE INDEX idx_tasks_task_categories ON tasks (category_id ASC);
CREATE INDEX idx_tasks_task_statuses ON tasks (status_id ASC);


-- -----------------------------------------------------
-- Table requests
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS requests (
  request_id BIGSERIAL PRIMARY KEY ,
  client_id BIGINT NOT NULL ,
  executor_id BIGINT NOT NULL ,
  task_id BIGINT NOT NULL ,
  CONSTRAINT fk_requests_clients
  FOREIGN KEY (client_id )
  REFERENCES contacts (contact_id )
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT fk_requests_executors
  FOREIGN KEY (executor_id )
  REFERENCES contacts (contact_id )
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT fk_requests_tasks
  FOREIGN KEY (task_id )
  REFERENCES tasks (task_id )
    ON DELETE NO ACTION
    ON UPDATE NO ACTION
);

CREATE INDEX idx_requests_clients ON requests (client_id ASC);
CREATE INDEX idx_requests_executors ON requests (executor_id ASC);
CREATE INDEX idx_requests_tasks ON requests (task_id ASC);

-- -----------------------------------------------------
-- Table persons
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS persons (
  contact_id BIGSERIAL PRIMARY KEY ,
  first_name VARCHAR(200) NOT NULL ,
  middle_name VARCHAR(200) NULL ,
  last_name VARCHAR(200) NULL ,
  position VARCHAR(200) NULL ,
  cell_phone BIGINT NOT NULL ,
  fixed_phone BIGINT NULL ,
  passport VARCHAR(1000) NULL ,
  address VARCHAR(1000) NOT NULL ,
  district VARCHAR(200) NULL ,
  age INT4 NULL ,
  profession VARCHAR(200) NULL ,
  average_payment INT NULL ,
  misc VARCHAR(5000) NULL ,
  CONSTRAINT fk_persons_contacts
  FOREIGN KEY (contact_id )
  REFERENCES contacts (contact_id )
    ON DELETE NO ACTION
    ON UPDATE NO ACTION
);

CREATE INDEX idx_persons_contacts ON persons (contact_id ASC);

-- -----------------------------------------------------
-- Table organizations
-- -----------------------------------------------------
CREATE  TABLE IF NOT EXISTS organizations (
  contact_id BIGSERIAL ,
  title VARCHAR(500) NOT NULL ,
  ceo_name VARCHAR(200) NULL ,
  phone VARCHAR(50) NOT NULL ,
  fax VARCHAR(50) NULL ,
  website VARCHAR(200) NULL ,
  email VARCHAR(200) NULL ,
  registration_address VARCHAR(1000) NOT NULL ,
  postal_address VARCHAR(1000) NULL ,
  ogrn BIGINT NULL ,
  inn BIGINT NOT NULL ,
  kpp INT NOT NULL ,
  bank_name VARCHAR(200) NOT NULL ,
  bik INT NOT NULL ,
  correspondent_account BIGINT NOT NULL ,
  settlement_account BIGINT NOT NULL ,
  misc VARCHAR(5000) NULL ,
  CONSTRAINT fk_organizations_contacts
  FOREIGN KEY (contact_id )
  REFERENCES contacts (contact_id )
    ON DELETE NO ACTION
    ON UPDATE NO ACTION
);

CREATE INDEX idx_organizations_contacts ON organizations (contact_id ASC);

-- -----------------------------------------------------
-- Table task_templates
-- -----------------------------------------------------
CREATE  TABLE IF NOT EXISTS task_templates (
  template_id BIGSERIAL PRIMARY KEY ,
  template_name VARCHAR(200) NOT NULL ,
  description VARCHAR(5000) NOT NULL ,
  category_id INT4 NOT NULL ,
  priority_id INT4 NOT NULL ,
  status_id INT4 NOT NULL ,
  CONSTRAINT fk_task_templates_task_priorities
  FOREIGN KEY (priority_id )
  REFERENCES task_priorities (priority_id )
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT fk_task_templates_task_categories
  FOREIGN KEY (category_id )
  REFERENCES task_categories (category_id )
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT fk_task_templates_task_statuses
  FOREIGN KEY (status_id )
  REFERENCES task_statuses (status_id )
    ON DELETE NO ACTION
    ON UPDATE NO ACTION
);

CREATE INDEX idx_task_templates_task_priorities ON task_templates (priority_id ASC);
CREATE INDEX idx_task_templates_task_categories ON task_templates (category_id ASC);
CREATE INDEX idx_task_templates_task_statuses ON task_templates (status_id ASC);

-- -----------------------------------------------------
-- Table users
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS users (
  user_id SERIAL PRIMARY KEY ,
  email VARCHAR(100) NOT NULL UNIQUE,
  password CHAR(60) NOT NULL ,
  contact_id BIGINT NULL ,
  CONSTRAINT fk_users_contacts
  FOREIGN KEY (contact_id )
  REFERENCES contacts (contact_id )
    ON DELETE NO ACTION
    ON UPDATE NO ACTION
);

CREATE INDEX idx_users_contacts ON users (contact_id ASC);


-- -----------------------------------------------------
-- Table roles
-- -----------------------------------------------------
CREATE  TABLE IF NOT EXISTS roles (
  role_id SERIAL4 PRIMARY KEY ,
  role_name VARCHAR(20) NOT NULL ,
  display_name VARCHAR(100) NULL
);


-- -----------------------------------------------------
-- Table user_roles
-- -----------------------------------------------------
CREATE  TABLE IF NOT EXISTS user_roles (
  role_id INT4 NOT NULL ,
  user_id INT NOT NULL ,
  PRIMARY KEY (role_id, user_id) ,
  CONSTRAINT fk_user_roles_roles
  FOREIGN KEY (role_id )
  REFERENCES roles (role_id )
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT fk_user_roles_users
  FOREIGN KEY (user_id )
  REFERENCES users (user_id )
    ON DELETE NO ACTION
    ON UPDATE NO ACTION
);

CREATE INDEX idx_user_roles_roles ON user_roles (role_id ASC);
CREATE INDEX idx_user_roles_users ON user_roles (user_id ASC);


-- -----------------------------------------------------
-- Table service_groups
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS service_groups (
  group_id SERIAL4 PRIMARY KEY ,
  group_name VARCHAR(200) NOT NULL ,
  sequence INT4 NOT NULL
);


-- -----------------------------------------------------
-- Table services
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS services (
  service_id BIGSERIAL PRIMARY KEY ,
  service_name VARCHAR(500) NOT NULL ,
  unit VARCHAR(600) NOT NULL ,
  price FLOAT NOT NULL ,
  group_id INT4 NULL ,
  CONSTRAINT fk_services_service_groups
  FOREIGN KEY (group_id )
  REFERENCES service_groups (group_id )
    ON DELETE NO ACTION
    ON UPDATE NO ACTION
);

CREATE INDEX idx_service_service_groups ON services (group_id ASC);


-- -----------------------------------------------------
-- Table task_services
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS task_services (
  task_id BIGINT NOT NULL,
  service_id BIGINT NOT NULL ,
  quantity FLOAT NOT NULL ,
  CONSTRAINT fk_task_services_tasks
  FOREIGN KEY (task_id )
  REFERENCES tasks (task_id )
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT fk_task_services_services
  FOREIGN KEY (service_id )
  REFERENCES services (service_id )
    ON DELETE NO ACTION
    ON UPDATE NO ACTION
);

CREATE INDEX idx_task_services_services ON task_services (service_id ASC);
CREATE INDEX idx_task_services_tasks ON task_services (task_id ASC);
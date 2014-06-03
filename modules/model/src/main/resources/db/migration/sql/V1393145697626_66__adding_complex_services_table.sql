-- Stores name and ID of complex_service
CREATE  TABLE IF NOT EXISTS complex_services (
  service_id BIGSERIAL PRIMARY KEY ,
  service_name VARCHAR(100) NOT NULL UNIQUE ,
  display_name VARCHAR(500) NOT NULL
);

CREATE  TABLE IF NOT EXISTS complex_service_stages (
  stage_id SERIAL4 PRIMARY KEY,
  sequence INT4 NOT NULL ,
  stage_name VARCHAR(100) NOT NULL,
  display_name VARCHAR(500) NOT NULL
);

CREATE  TABLE IF NOT EXISTS complex_service_contents (
  complex_service_id BIGINT NOT NULL,
  stage_id INT4 NOT NULL ,
  service_id BIGINT  NOT NULL ,
  sequence INT4 NOT NULL ,
  PRIMARY KEY (complex_service_id, stage_id, service_id, sequence),
  CONSTRAINT fk_complex_service_contents_complex_services
  FOREIGN KEY (complex_service_id )
  REFERENCES complex_services (service_id )
  ON DELETE CASCADE
  ON UPDATE NO ACTION ,
  CONSTRAINT fk_complex_service_contents_complex_service_stages
  FOREIGN KEY (stage_id )
  REFERENCES complex_service_stages (stage_id )
  ON DELETE CASCADE
  ON UPDATE NO ACTION ,
  CONSTRAINT fk_complex_service_contents_services
  FOREIGN KEY (service_id )
  REFERENCES services (service_id )
  ON DELETE CASCADE
  ON UPDATE NO ACTION
);

CREATE INDEX idx_complex_service_contents_complex_services ON complex_service_contents (complex_service_id ASC);
CREATE INDEX idx_complex_service_contents_complex_service_stages ON complex_service_contents (stage_id ASC);
CREATE INDEX idx_complex_service_contents_services ON complex_service_contents (service_id ASC);

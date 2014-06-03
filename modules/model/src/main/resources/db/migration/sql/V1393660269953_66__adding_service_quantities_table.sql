CREATE  TABLE IF NOT EXISTS service_quantities (
  complex_service_id BIGINT NOT NULL,
  service_id BIGINT  NOT NULL ,
  quantity FLOAT NOT NULL ,
  PRIMARY KEY (complex_service_id, service_id),
  CONSTRAINT fk_service_quantities_complex_services
  FOREIGN KEY (complex_service_id )
  REFERENCES complex_services (service_id )
  ON DELETE CASCADE
  ON UPDATE NO ACTION ,
  CONSTRAINT fk_service_quantities_services
  FOREIGN KEY (service_id )
  REFERENCES services (service_id )
  ON DELETE CASCADE
  ON UPDATE NO ACTION
);

CREATE INDEX idx_service_quantities_complex_services ON complex_service_contents (complex_service_id ASC);
CREATE INDEX idx_service_quantities_services ON complex_service_contents (service_id ASC);

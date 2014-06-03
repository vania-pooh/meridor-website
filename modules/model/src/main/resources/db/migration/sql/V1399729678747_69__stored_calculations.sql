CREATE  TABLE IF NOT EXISTS stored_calculations (
  stored_calculation_id BIGSERIAL PRIMARY KEY,
  display_name VARCHAR(500) NOT NULL,
  data VARCHAR(5000) NOT NULL
);
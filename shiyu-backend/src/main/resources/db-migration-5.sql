CREATE TABLE IF NOT EXISTS ai_feature_log (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  user_id BIGINT,
  feature VARCHAR(50) NOT NULL,
  title VARCHAR(200) NOT NULL DEFAULT '',
  input_text MEDIUMTEXT,
  result_text MEDIUMTEXT,
  provider VARCHAR(50),
  execution_time_ms BIGINT DEFAULT 0,
  related_id BIGINT,
  related_type VARCHAR(50),
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
  deleted INT DEFAULT 0,
  INDEX idx_user_id (user_id),
  INDEX idx_feature (feature),
  INDEX idx_create_time (create_time)
);

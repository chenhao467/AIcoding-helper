CREATE TABLE IF NOT EXISTS agent_user (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  user_no VARCHAR(64) NOT NULL UNIQUE,
  user_name VARCHAR(64) NOT NULL,
  role_code VARCHAR(32) NOT NULL,
  tenant_id BIGINT NOT NULL,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS ticket (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  ticket_no VARCHAR(32) NOT NULL UNIQUE,
  title VARCHAR(120) NOT NULL,
  description TEXT NOT NULL,
  priority VARCHAR(8) NOT NULL,
  status VARCHAR(16) NOT NULL,
  reporter_id BIGINT NOT NULL,
  assignee_id BIGINT NULL,
  source VARCHAR(16) NOT NULL DEFAULT 'AI_AGENT',
  tenant_id BIGINT NOT NULL,
  deleted TINYINT NOT NULL DEFAULT 0,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  INDEX idx_ticket_status_priority (status, priority),
  INDEX idx_ticket_reporter (reporter_id)
);

CREATE TABLE IF NOT EXISTS ticket_action_log (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  ticket_id BIGINT NOT NULL,
  action_type VARCHAR(32) NOT NULL,
  action_detail JSON NULL,
  operator_id BIGINT NOT NULL,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  INDEX idx_action_ticket (ticket_id)
);

CREATE TABLE IF NOT EXISTS kb_article (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  kb_no VARCHAR(32) NOT NULL UNIQUE,
  title VARCHAR(200) NOT NULL,
  content MEDIUMTEXT NOT NULL,
  category VARCHAR(64) NOT NULL,
  tags VARCHAR(255) NULL,
  status VARCHAR(16) NOT NULL DEFAULT 'PUBLISHED',
  tenant_id BIGINT NOT NULL,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  INDEX idx_kb_category (category)
);

CREATE TABLE IF NOT EXISTS tool_call_log (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  trace_id VARCHAR(64) NOT NULL,
  tool_name VARCHAR(64) NOT NULL,
  request_json JSON NOT NULL,
  response_json JSON NULL,
  success TINYINT NOT NULL,
  error_code VARCHAR(32) NULL,
  error_message VARCHAR(255) NULL,
  ticket_id BIGINT NULL,
  tenant_id BIGINT NOT NULL,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  INDEX idx_tool_trace (trace_id),
  INDEX idx_tool_name_time (tool_name, created_at)
);

INSERT INTO agent_user (user_no, user_name, role_code, tenant_id)
VALUES ('U10086', '默认提单人', 'REPORTER', 1)
ON DUPLICATE KEY UPDATE updated_at = CURRENT_TIMESTAMP;

INSERT INTO kb_article (kb_no, title, content, category, tags, status, tenant_id)
VALUES
('KB-PAY-001', '支付502故障处理SOP', '若支付网关出现502，先确认影响范围；超过30%用户且持续10分钟按P1处理，并立即提单给支付值班组。', 'PAYMENT', '502,SOP,P1', 'PUBLISHED', 1),
('KB-PAY-002', '支付超时排查建议', '先检查上游依赖与数据库连接池，再核对最近发布记录；必要时进行流量降级。', 'PAYMENT', 'timeout,runbook', 'PUBLISHED', 1)
ON DUPLICATE KEY UPDATE updated_at = CURRENT_TIMESTAMP;

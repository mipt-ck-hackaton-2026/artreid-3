-- Создание таблиц для тестов
CREATE TABLE IF NOT EXISTS leads (
    lead_id BIGSERIAL PRIMARY KEY,
    external_lead_id VARCHAR(50),
    manager_id VARCHAR(50),
    pipeline_id INTEGER,
    delivery_service VARCHAR(100),
    city VARCHAR(100),
    lead_created_at BIGINT,
    sale_ts BIGINT,
    lead_responsible_user_id VARCHAR(50)
);

CREATE TABLE IF NOT EXISTS lead_events (
    lead_event_id BIGSERIAL PRIMARY KEY,
    lead_id BIGINT NOT NULL,
    stage_name VARCHAR(50) NOT NULL,
    event_time TIMESTAMP NOT NULL,
    CONSTRAINT fk_lead FOREIGN KEY (lead_id) REFERENCES leads(lead_id) ON DELETE CASCADE
);

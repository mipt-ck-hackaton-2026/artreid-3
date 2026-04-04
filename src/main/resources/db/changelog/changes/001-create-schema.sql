CREATE TABLE leads (
    lead_id BIGSERIAL PRIMARY KEY,
    external_lead_id VARCHAR(50),
    manager_id VARCHAR(50),
    pipeline_id INTEGER,
    delivery_service VARCHAR(100),
    city VARCHAR(100)
);

CREATE TABLE lead_events (
    lead_event_id BIGSERIAL PRIMARY KEY,
    lead_id BIGINT NOT NULL,
    stage_name VARCHAR(50) NOT NULL,
    event_time TIMESTAMP NOT NULL,
    CONSTRAINT fk_lead FOREIGN KEY(lead_id) REFERENCES leads(lead_id) ON DELETE CASCADE,
    CONSTRAINT uq_lead_stage UNIQUE(lead_id, stage_name)
);

CREATE INDEX idx_lead_events_time ON lead_events(lead_id, event_time);
CREATE INDEX idx_events_stage ON lead_events(stage_name, event_time);
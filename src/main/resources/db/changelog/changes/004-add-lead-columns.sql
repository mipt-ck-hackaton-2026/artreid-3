ALTER TABLE leads ADD COLUMN delivery_manager_id VARCHAR(50);
ALTER TABLE leads ADD COLUMN lead_qualification VARCHAR(50);
ALTER TABLE leads ADD COLUMN outcome_unknown BOOLEAN DEFAULT FALSE;
ALTER TABLE leads ADD COLUMN lifecycle_incomplete BOOLEAN DEFAULT FALSE;

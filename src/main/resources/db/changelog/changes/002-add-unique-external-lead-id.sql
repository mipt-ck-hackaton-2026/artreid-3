ALTER TABLE leads
ADD CONSTRAINT uq_leads_external_lead_id UNIQUE (external_lead_id);

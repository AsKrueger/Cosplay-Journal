-- External Identity for Events

ALTER TABLE event ADD COLUMN external_id VARCHAR(100);

-- Unique constraint for source + external_id (idempotency)
ALTER TABLE event ADD CONSTRAINT uq_event_source_external
    UNIQUE (source, external_id);

CREATE INDEX idx_event_source_external ON event(source, external_id);

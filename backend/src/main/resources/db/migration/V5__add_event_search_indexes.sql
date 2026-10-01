-- Indexes for Event Search, Filtering, and Pagination

CREATE INDEX idx_event_city_start_date ON event(city, start_date);
CREATE INDEX idx_event_start_date_status ON event(start_date, status);
CREATE INDEX idx_event_name_trgm ON event(name);

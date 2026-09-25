-- Initial Database Schema for Cosplay Journal

-- 1. Cosplay Table
CREATE TABLE cosplay (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    description VARCHAR(500),
    character_name VARCHAR(100),
    origin_series VARCHAR(100),
    status VARCHAR(30) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE INDEX idx_cosplay_status ON cosplay(status);

-- 2. Event Table
CREATE TABLE event (
    id VARCHAR(100) PRIMARY KEY,
    name VARCHAR(150) NOT NULL,
    description TEXT,
    start_date DATE NOT NULL,
    end_date DATE NOT NULL,
    city VARCHAR(100) NOT NULL,
    venue VARCHAR(150),
    province VARCHAR(100),
    country VARCHAR(100) NOT NULL DEFAULT 'España',
    address VARCHAR(255),
    latitude DOUBLE PRECISION,
    longitude DOUBLE PRECISION,
    website VARCHAR(255),
    source VARCHAR(50) NOT NULL,
    status VARCHAR(30) NOT NULL,
    CONSTRAINT check_event_dates CHECK (start_date <= end_date)
);

CREATE INDEX idx_event_dates ON event(start_date, end_date);
CREATE INDEX idx_event_city ON event(city);

-- 3. Participation Table
CREATE TABLE participation (
    id VARCHAR(100) PRIMARY KEY,
    event_id VARCHAR(100) NOT NULL REFERENCES event(id) ON DELETE CASCADE,
    cosplay_id BIGINT NOT NULL REFERENCES cosplay(id) ON DELETE CASCADE,
    type VARCHAR(30) NOT NULL,
    status VARCHAR(30) NOT NULL,
    group_name VARCHAR(150),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE INDEX idx_participation_event ON participation(event_id);
CREATE INDEX idx_participation_cosplay ON participation(cosplay_id);

-- 4. Participant Table
CREATE TABLE participant (
    user_id VARCHAR(100) NOT NULL,
    participation_id VARCHAR(100) NOT NULL REFERENCES participation(id) ON DELETE CASCADE,
    name VARCHAR(100) NOT NULL,
    role VARCHAR(30) NOT NULL,
    assigned_character VARCHAR(100),
    joined_at TIMESTAMP WITH TIME ZONE NOT NULL,
    PRIMARY KEY (user_id, participation_id)
);

-- 5. Photo Table
CREATE TABLE photo (
    id VARCHAR(100) PRIMARY KEY,
    participation_id VARCHAR(100) NOT NULL REFERENCES participation(id) ON DELETE CASCADE,
    storage_reference VARCHAR(500) NOT NULL,
    caption VARCHAR(255),
    uploaded_by_user_id VARCHAR(100),
    uploaded_at TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE INDEX idx_photo_participation ON photo(participation_id);

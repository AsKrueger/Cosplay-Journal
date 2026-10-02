-- User Account Table for Authentication & Security

CREATE TABLE user_account (
    id VARCHAR(100) PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    email VARCHAR(100) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    role VARCHAR(30) NOT NULL,
    status VARCHAR(30) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE INDEX idx_user_username ON user_account(username);
CREATE INDEX idx_user_email ON user_account(email);

-- Default system user for initial seeds / fallback
INSERT INTO user_account (id, username, email, password_hash, role, status, created_at, updated_at)
VALUES ('system-default', 'system_default', 'system@cosplayjournal.com', '$2a$10$systemDefaultHashPlaceholder', 'USER', 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

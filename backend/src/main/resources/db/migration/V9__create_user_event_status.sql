-- Estado de participación del usuario en cada evento: ATTENDING, ATTENDED, INTERESTED o PLANNING.
-- Quitar la selección (NONE) borra la fila.
CREATE TABLE user_event_status (
    id VARCHAR(250) PRIMARY KEY,
    user_id VARCHAR(100) NOT NULL REFERENCES user_account(id),
    event_id VARCHAR(150) NOT NULL,
    status VARCHAR(20) NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT uk_user_event_status UNIQUE (user_id, event_id)
);

CREATE INDEX idx_user_event_status_user ON user_event_status(user_id);

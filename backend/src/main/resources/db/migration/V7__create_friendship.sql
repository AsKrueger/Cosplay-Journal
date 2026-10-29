-- Amistades entre usuarios. Una fila es una solicitud pendiente o una amistad aceptada.
CREATE TABLE friendship (
    id VARCHAR(100) PRIMARY KEY,
    requester_id VARCHAR(100) NOT NULL REFERENCES user_account(id),
    addressee_id VARCHAR(100) NOT NULL REFERENCES user_account(id),
    status VARCHAR(20) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    responded_at TIMESTAMP WITH TIME ZONE,
    CONSTRAINT uk_friendship_pair UNIQUE (requester_id, addressee_id),
    CONSTRAINT ck_friendship_not_self CHECK (requester_id <> addressee_id)
);

CREATE INDEX idx_friendship_addressee ON friendship(addressee_id);

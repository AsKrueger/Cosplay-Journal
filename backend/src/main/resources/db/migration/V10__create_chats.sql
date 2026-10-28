-- Chats privados y grupos. Los miembros de cada conversación se guardan en su propia tabla.
CREATE TABLE conversation (
    id VARCHAR(100) PRIMARY KEY,
    type VARCHAR(20) NOT NULL,
    name VARCHAR(60),
    created_by VARCHAR(100) NOT NULL REFERENCES user_account(id),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE TABLE conversation_member (
    conversation_id VARCHAR(100) NOT NULL REFERENCES conversation(id),
    user_id VARCHAR(100) NOT NULL REFERENCES user_account(id),
    PRIMARY KEY (conversation_id, user_id)
);

CREATE INDEX idx_conversation_member_user ON conversation_member(user_id);

CREATE TABLE message (
    id VARCHAR(100) PRIMARY KEY,
    conversation_id VARCHAR(100) NOT NULL REFERENCES conversation(id),
    sender_id VARCHAR(100) NOT NULL REFERENCES user_account(id),
    kind VARCHAR(10) NOT NULL,
    body VARCHAR(2000) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE INDEX idx_message_conversation_time ON message(conversation_id, created_at);

package com.cosplayjournal.application.port.out;

import com.cosplayjournal.domain.model.chat.Message;

import java.time.Instant;
import java.util.List;

public interface MessageRepositoryPort {
    Message save(Message message);
    /** Mensajes de una conversación posteriores a {@code since}, del más antiguo al más reciente. */
    List<Message> findByConversationSince(String conversationId, Instant since);
}

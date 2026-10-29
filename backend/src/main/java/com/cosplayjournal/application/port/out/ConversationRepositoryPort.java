package com.cosplayjournal.application.port.out;

import com.cosplayjournal.domain.model.chat.Conversation;
import com.cosplayjournal.domain.model.user.UserId;

import java.util.List;
import java.util.Optional;

public interface ConversationRepositoryPort {
    Conversation save(Conversation conversation);
    Optional<Conversation> findById(String id);
    List<Conversation> findAllByMember(UserId userId);
}

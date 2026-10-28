package com.cosplayjournal.infrastructure.adapter.out.persistence.adapter;

import com.cosplayjournal.application.port.out.ConversationRepositoryPort;
import com.cosplayjournal.application.port.out.MessageRepositoryPort;
import com.cosplayjournal.domain.model.chat.Conversation;
import com.cosplayjournal.domain.model.chat.Message;
import com.cosplayjournal.domain.model.user.UserId;
import com.cosplayjournal.infrastructure.adapter.out.persistence.mapper.ChatPersistenceMapper;
import com.cosplayjournal.infrastructure.adapter.out.persistence.repository.SpringDataConversationRepository;
import com.cosplayjournal.infrastructure.adapter.out.persistence.repository.SpringDataMessageRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

/** Adaptador JPA de conversaciones y mensajes. */
@Repository
public class JpaChatRepositoryAdapter implements ConversationRepositoryPort, MessageRepositoryPort {

    private final SpringDataConversationRepository conversations;
    private final SpringDataMessageRepository messages;

    public JpaChatRepositoryAdapter(SpringDataConversationRepository conversations, SpringDataMessageRepository messages) {
        this.conversations = conversations;
        this.messages = messages;
    }

    @Override
    @Transactional
    public Conversation save(Conversation conversation) {
        return ChatPersistenceMapper.toDomain(conversations.save(ChatPersistenceMapper.toJpaEntity(conversation)));
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Conversation> findById(String id) {
        return conversations.findById(id).map(ChatPersistenceMapper::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Conversation> findAllByMember(UserId userId) {
        return conversations.findAllByMember(userId.value()).stream().map(ChatPersistenceMapper::toDomain).toList();
    }

    @Override
    @Transactional
    public Message save(Message message) {
        return ChatPersistenceMapper.toDomain(messages.save(ChatPersistenceMapper.toJpaEntity(message)));
    }

    @Override
    @Transactional(readOnly = true)
    public List<Message> findByConversationSince(String conversationId, Instant since) {
        return messages.findSince(conversationId, since).stream().map(ChatPersistenceMapper::toDomain).toList();
    }
}

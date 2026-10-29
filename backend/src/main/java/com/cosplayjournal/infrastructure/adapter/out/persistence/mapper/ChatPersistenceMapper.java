package com.cosplayjournal.infrastructure.adapter.out.persistence.mapper;

import com.cosplayjournal.domain.model.chat.Conversation;
import com.cosplayjournal.domain.model.chat.ConversationType;
import com.cosplayjournal.domain.model.chat.Message;
import com.cosplayjournal.domain.model.chat.MessageKind;
import com.cosplayjournal.domain.model.user.UserId;
import com.cosplayjournal.infrastructure.adapter.out.persistence.entity.ConversationJpaEntity;
import com.cosplayjournal.infrastructure.adapter.out.persistence.entity.MessageJpaEntity;

import java.util.Set;
import java.util.stream.Collectors;

public class ChatPersistenceMapper {

    public static ConversationJpaEntity toJpaEntity(Conversation conversation) {
        return new ConversationJpaEntity(
                conversation.getId(),
                conversation.getType().name(),
                conversation.getName(),
                conversation.getCreatedBy().value(),
                conversation.getCreatedAt(),
                conversation.getMembers().stream().map(UserId::value).collect(Collectors.toSet())
        );
    }

    public static Conversation toDomain(ConversationJpaEntity entity) {
        Set<UserId> members = entity.getMembers().stream().map(UserId::of).collect(Collectors.toSet());
        return Conversation.restore(
                entity.getId(),
                ConversationType.valueOf(entity.getType()),
                entity.getName(),
                UserId.of(entity.getCreatedBy()),
                entity.getCreatedAt(),
                members
        );
    }

    public static MessageJpaEntity toJpaEntity(Message message) {
        return new MessageJpaEntity(
                message.id(),
                message.conversationId(),
                message.senderId().value(),
                message.kind().name(),
                message.body(),
                message.createdAt()
        );
    }

    public static Message toDomain(MessageJpaEntity entity) {
        return new Message(
                entity.getId(),
                entity.getConversationId(),
                UserId.of(entity.getSenderId()),
                MessageKind.valueOf(entity.getKind()),
                entity.getBody(),
                entity.getCreatedAt()
        );
    }
}

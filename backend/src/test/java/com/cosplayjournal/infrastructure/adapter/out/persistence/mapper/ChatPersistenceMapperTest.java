package com.cosplayjournal.infrastructure.adapter.out.persistence.mapper;

import com.cosplayjournal.domain.model.chat.Conversation;
import com.cosplayjournal.domain.model.chat.ConversationType;
import com.cosplayjournal.domain.model.chat.Message;
import com.cosplayjournal.domain.model.chat.MessageKind;
import com.cosplayjournal.domain.model.user.UserId;
import com.cosplayjournal.infrastructure.adapter.out.persistence.entity.ConversationJpaEntity;
import com.cosplayjournal.infrastructure.adapter.out.persistence.entity.MessageJpaEntity;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class ChatPersistenceMapperTest {

    private static final UserId ANA = UserId.of("user-ana");
    private static final UserId LUIS = UserId.of("user-luis");
    private static final UserId MARTA = UserId.of("user-marta");

    @Test
    @DisplayName("Debe mapear un chat privado a entidad JPA sin nombre y con sus dos miembros")
    void shouldMapPrivateConversationToEntity() {
        Conversation conversation = Conversation.privateBetween(ANA, LUIS);

        ConversationJpaEntity entity = ChatPersistenceMapper.toJpaEntity(conversation);

        assertEquals(conversation.getId(), entity.getId());
        assertEquals("PRIVATE", entity.getType());
        assertNull(entity.getName());
        assertEquals("user-ana", entity.getCreatedBy());
        assertEquals(conversation.getCreatedAt(), entity.getCreatedAt());
        assertEquals(Set.of("user-ana", "user-luis"), entity.getMembers());
    }

    @Test
    @DisplayName("Debe mapear un grupo conservando nombre, creador y miembros")
    void shouldMapGroupConversationToEntity() {
        Conversation group = Conversation.group(ANA, "Quedada Salón", Set.of(LUIS, MARTA));

        ConversationJpaEntity entity = ChatPersistenceMapper.toJpaEntity(group);

        assertEquals("GROUP", entity.getType());
        assertEquals("Quedada Salón", entity.getName());
        assertEquals("user-ana", entity.getCreatedBy());
        assertEquals(Set.of("user-ana", "user-luis", "user-marta"), entity.getMembers());
    }

    @Test
    @DisplayName("Debe reconstruir una conversación desde la entidad JPA")
    void shouldMapConversationEntityToDomain() {
        Instant created = Instant.parse("2026-05-20T17:00:00Z");
        ConversationJpaEntity entity = new ConversationJpaEntity("c-1", "GROUP", "Grupo", "user-ana", created,
                Set.of("user-ana", "user-luis"));

        Conversation conversation = ChatPersistenceMapper.toDomain(entity);

        assertEquals("c-1", conversation.getId());
        assertEquals(ConversationType.GROUP, conversation.getType());
        assertEquals("Grupo", conversation.getName());
        assertEquals(ANA, conversation.getCreatedBy());
        assertEquals(created, conversation.getCreatedAt());
        assertTrue(conversation.hasMember(ANA));
        assertTrue(conversation.hasMember(LUIS));
        assertFalse(conversation.hasMember(MARTA));
    }

    @Test
    @DisplayName("Ida y vuelta de una conversación no pierde miembros ni datos")
    void shouldRoundTripConversation() {
        Conversation original = Conversation.group(ANA, "Grupo", Set.of(LUIS, MARTA));

        Conversation restored = ChatPersistenceMapper.toDomain(ChatPersistenceMapper.toJpaEntity(original));

        assertEquals(original.getId(), restored.getId());
        assertEquals(original.getType(), restored.getType());
        assertEquals(original.getName(), restored.getName());
        assertEquals(original.getCreatedBy(), restored.getCreatedBy());
        assertEquals(original.getCreatedAt(), restored.getCreatedAt());
        assertEquals(original.getMembers(), restored.getMembers());
    }

    @Test
    @DisplayName("Debe mapear un mensaje de texto a entidad JPA")
    void shouldMapTextMessageToEntity() {
        Instant created = Instant.parse("2026-05-20T17:05:00Z");
        Message message = new Message("m-1", "c-1", ANA, MessageKind.TEXT, "¡Hola!", created);

        MessageJpaEntity entity = ChatPersistenceMapper.toJpaEntity(message);

        assertEquals("m-1", entity.getId());
        assertEquals("c-1", entity.getConversationId());
        assertEquals("user-ana", entity.getSenderId());
        assertEquals("TEXT", entity.getKind());
        assertEquals("¡Hola!", entity.getBody());
        assertEquals(created, entity.getCreatedAt());
    }

    @Test
    @DisplayName("Ida y vuelta de un mensaje de foto devuelve un mensaje igual")
    void shouldRoundTripImageMessage() {
        Message original = new Message("m-2", "c-1", LUIS, MessageKind.IMAGE, "https://example.com/foto.jpg",
                Instant.parse("2026-05-20T17:06:00Z"));

        Message restored = ChatPersistenceMapper.toDomain(ChatPersistenceMapper.toJpaEntity(original));

        assertEquals(original, restored);
    }
}

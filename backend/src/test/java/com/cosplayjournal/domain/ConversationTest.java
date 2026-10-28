package com.cosplayjournal.domain;

import com.cosplayjournal.domain.exception.InvalidChatDataException;
import com.cosplayjournal.domain.model.chat.Conversation;
import com.cosplayjournal.domain.model.chat.ConversationType;
import com.cosplayjournal.domain.model.chat.Message;
import com.cosplayjournal.domain.model.chat.MessageKind;
import com.cosplayjournal.domain.model.user.UserId;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class ConversationTest {

    private final UserId ana = UserId.of("ana");
    private final UserId luis = UserId.of("luis");

    @Test
    @DisplayName("Un chat privado tiene exactamente dos miembros y no admite el mismo usuario dos veces")
    void privateHasTwoMembers() {
        Conversation chat = Conversation.privateBetween(ana, luis);

        assertEquals(ConversationType.PRIVATE, chat.getType());
        assertEquals(Set.of(ana, luis), chat.getMembers());
        assertThrows(InvalidChatDataException.class, () -> Conversation.privateBetween(ana, ana));
    }

    @Test
    @DisplayName("Un grupo necesita un nombre y al menos otro miembro además del creador")
    void groupRules() {
        Conversation group = Conversation.group(ana, "Grupal Naruto", Set.of(luis));

        assertEquals(ConversationType.GROUP, group.getType());
        assertTrue(group.hasMember(ana));
        assertTrue(group.hasMember(luis));
        assertThrows(InvalidChatDataException.class, () -> Conversation.group(ana, "", Set.of(luis)));
        assertThrows(InvalidChatDataException.class, () -> Conversation.group(ana, "Solo yo", Set.of(ana)));
    }

    @Test
    @DisplayName("Un mensaje de texto no puede estar vacío ni superar 2000 caracteres")
    void textMessageLimits() {
        assertThrows(InvalidChatDataException.class,
                () -> new Message(null, "c1", ana, MessageKind.TEXT, "   ", null));
        assertThrows(InvalidChatDataException.class,
                () -> new Message(null, "c1", ana, MessageKind.TEXT, "x".repeat(2001), null));
    }

    @Test
    @DisplayName("Una foto solo se acepta como enlace http o https")
    void imageMessageMustBeWebUrl() {
        Message photo = new Message(null, "c1", ana, MessageKind.IMAGE, "https://img.example.com/g.jpg", null);
        assertEquals(MessageKind.IMAGE, photo.kind());
        assertThrows(InvalidChatDataException.class,
                () -> new Message(null, "c1", ana, MessageKind.IMAGE, "file:///etc/passwd", null));
    }
}

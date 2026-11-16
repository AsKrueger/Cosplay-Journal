package com.cosplayjournal.domain;

import com.cosplayjournal.domain.exception.InvalidChatDataException;
import com.cosplayjournal.domain.model.chat.Message;
import com.cosplayjournal.domain.model.chat.MessageKind;
import com.cosplayjournal.domain.model.user.UserId;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

class MessageTest {

    private final UserId ana = UserId.of("ana");

    @Test
    @DisplayName("Un mensaje nuevo recibe identificador y fecha de creación automáticamente")
    void generatesIdAndCreatedAt() {
        Message first = new Message(null, "c1", ana, MessageKind.TEXT, "Hola", null);
        Message second = new Message(null, "c1", ana, MessageKind.TEXT, "Hola", null);

        assertNotNull(first.id());
        assertNotNull(first.createdAt());
        assertNotEquals(first.id(), second.id());
    }

    @Test
    @DisplayName("Al reconstruir un mensaje se conservan su identificador y su fecha")
    void keepsGivenIdAndCreatedAt() {
        Instant createdAt = Instant.parse("2026-10-28T20:00:00Z");
        Message message = new Message("m-1", "c1", ana, MessageKind.TEXT, "Hola", createdAt);

        assertEquals("m-1", message.id());
        assertEquals(createdAt, message.createdAt());
    }

    @Test
    @DisplayName("Conversación, remitente y tipo son obligatorios")
    void requiresConversationSenderAndKind() {
        assertThrows(InvalidChatDataException.class,
                () -> new Message(null, null, ana, MessageKind.TEXT, "Hola", null));
        assertThrows(InvalidChatDataException.class,
                () -> new Message(null, "c1", null, MessageKind.TEXT, "Hola", null));
        assertThrows(InvalidChatDataException.class,
                () -> new Message(null, "c1", ana, null, "Hola", null));
    }

    @Test
    @DisplayName("Un mensaje de texto de exactamente 2000 caracteres es válido")
    void textAtLimitIsValid() {
        String body = "x".repeat(Message.MAX_TEXT_LENGTH);
        assertEquals(body, new Message(null, "c1", ana, MessageKind.TEXT, body, null).body());
    }

    @Test
    @DisplayName("Una foto acepta enlaces http y https de hasta 500 caracteres")
    void imageUrlLimits() {
        assertDoesNotThrow(() -> new Message(null, "c1", ana, MessageKind.IMAGE, "http://img.example.com/a.png", null));

        String prefix = "https://img.example.com/";
        String atLimit = prefix + "a".repeat(Message.MAX_URL_LENGTH - prefix.length());
        assertDoesNotThrow(() -> new Message(null, "c1", ana, MessageKind.IMAGE, atLimit, null));
        assertThrows(InvalidChatDataException.class,
                () -> new Message(null, "c1", ana, MessageKind.IMAGE, atLimit + "a", null));
    }

    @Test
    @DisplayName("Una foto rechaza esquemas que no sean http o https")
    void imageRejectsOtherSchemes() {
        assertThrows(InvalidChatDataException.class,
                () -> new Message(null, "c1", ana, MessageKind.IMAGE, "ftp://img.example.com/a.png", null));
        assertThrows(InvalidChatDataException.class,
                () -> new Message(null, "c1", ana, MessageKind.IMAGE, "javascript:alert(1)", null));
        assertThrows(InvalidChatDataException.class,
                () -> new Message(null, "c1", ana, MessageKind.IMAGE, "img.example.com/a.png", null));
    }
}

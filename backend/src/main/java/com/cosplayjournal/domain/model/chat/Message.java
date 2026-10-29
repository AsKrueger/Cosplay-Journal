package com.cosplayjournal.domain.model.chat;

import com.cosplayjournal.domain.exception.InvalidChatDataException;
import com.cosplayjournal.domain.model.user.UserId;

import java.time.Instant;
import java.util.UUID;

/** Mensaje de texto o de foto (la foto se guarda como enlace http o https). */
public record Message(String id, String conversationId, UserId senderId, MessageKind kind, String body, Instant createdAt) {

    public static final int MAX_TEXT_LENGTH = 2000;
    public static final int MAX_URL_LENGTH = 500;

    public Message {
        if (conversationId == null || senderId == null) {
            throw new InvalidChatDataException("El mensaje necesita conversación y remitente");
        }
        if (kind == null) {
            throw new InvalidChatDataException("Tipo de mensaje no válido");
        }
        if (body == null || body.isBlank()) {
            throw new InvalidChatDataException("El mensaje no puede estar vacío");
        }
        if (kind == MessageKind.TEXT && body.length() > MAX_TEXT_LENGTH) {
            throw new InvalidChatDataException("El mensaje no puede superar " + MAX_TEXT_LENGTH + " caracteres");
        }
        if (kind == MessageKind.IMAGE) {
            String url = body.trim();
            if (url.length() > MAX_URL_LENGTH || !(url.startsWith("https://") || url.startsWith("http://"))) {
                throw new InvalidChatDataException("La foto debe ser un enlace http o https de hasta " + MAX_URL_LENGTH + " caracteres");
            }
        }
        if (id == null) {
            id = UUID.randomUUID().toString();
        }
        if (createdAt == null) {
            createdAt = Instant.now();
        }
    }
}

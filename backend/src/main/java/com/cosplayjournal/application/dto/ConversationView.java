package com.cosplayjournal.application.dto;

import java.time.Instant;
import java.util.List;

/** Conversación para mostrar: miembros por nickname, nunca por email. */
public record ConversationView(String id, String type, String name, List<String> memberNicknames, Instant createdAt) {
}

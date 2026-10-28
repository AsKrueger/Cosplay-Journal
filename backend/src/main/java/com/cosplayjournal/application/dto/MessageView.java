package com.cosplayjournal.application.dto;

import java.time.Instant;

public record MessageView(String id, String senderNickname, String kind, String body, Instant createdAt) {
}

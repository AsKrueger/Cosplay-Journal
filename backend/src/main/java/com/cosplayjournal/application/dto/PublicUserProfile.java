package com.cosplayjournal.application.dto;

import java.time.Instant;

/** Perfil visible para otros usuarios. No incluye email ni datos de contacto. */
public record PublicUserProfile(String nickname, String avatarUrl, Instant memberSince) {
}

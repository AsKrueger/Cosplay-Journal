package com.cosplayjournal.application.dto;

import java.time.Instant;

/** Perfil del propio usuario. Incluye el email porque solo lo ve él mismo. */
public record OwnProfile(String nickname, String email, String avatarUrl, Instant memberSince) {
}

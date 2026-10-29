package com.cosplayjournal.application.dto;

import java.time.Instant;

/** Estado de la última actualización del catálogo desde ListadoManga. */
public record RefreshStatus(boolean running, Instant lastRunAt) {
}

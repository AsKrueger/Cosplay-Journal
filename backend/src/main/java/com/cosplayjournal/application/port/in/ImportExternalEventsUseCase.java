package com.cosplayjournal.application.port.in;

import com.cosplayjournal.application.dto.ImportEventsResult;
import com.cosplayjournal.application.dto.RefreshStatus;

public interface ImportExternalEventsUseCase {
    ImportEventsResult importEvents();

    /**
     * Sincroniza solo si la última sincronización es anterior al intervalo mínimo.
     * Si no, devuelve el resultado de la última sincronización sin volver a consultar la fuente.
     */
    ImportEventsResult refreshIfStale();

    /** Indica si hay una sincronización en curso y cuándo terminó la última. */
    RefreshStatus refreshStatus();
}

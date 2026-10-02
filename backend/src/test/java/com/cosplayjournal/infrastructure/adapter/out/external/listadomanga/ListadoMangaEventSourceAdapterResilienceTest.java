package com.cosplayjournal.infrastructure.adapter.out.external.listadomanga;

import com.cosplayjournal.infrastructure.adapter.out.external.listadomanga.exception.ListadoMangaUnavailableException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ListadoMangaEventSourceAdapterResilienceTest {

    @Test
    @DisplayName("Debe lanzar ListadoMangaUnavailableException tras agotar todos los reintentos cuando la URL es inaccesible")
    void shouldThrowExceptionWhenUrlIsUnreachable() {
        ListadoMangaEventSourceAdapter adapter = new ListadoMangaEventSourceAdapter(
                "http://localhost:1/salones.php", 100, 100, true, 2, 10L
        );

        assertThrows(ListadoMangaUnavailableException.class, adapter::fetchEvents);
    }
}

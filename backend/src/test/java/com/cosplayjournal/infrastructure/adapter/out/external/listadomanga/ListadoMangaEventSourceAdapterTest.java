package com.cosplayjournal.infrastructure.adapter.out.external.listadomanga;

import com.cosplayjournal.application.dto.ExternalEventData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.time.Month;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ListadoMangaEventSourceAdapterTest {

    private ListadoMangaEventSourceAdapter adapter;

    @BeforeEach
    void setUp() {
        adapter = new ListadoMangaEventSourceAdapter("https://www.listadomanga.es/salones.php", 5000);
    }

    @Test
    @DisplayName("Debe parsear correctamente el fixture HTML offline de ListadoManga sin conexión a Internet")
    void shouldParseOfflineHtmlFixture() throws IOException {
        InputStream inputStream = getClass().getResourceAsStream("/fixtures/listadomanga_salones.html");
        assertNotNull(inputStream, "El fixture HTML no fue encontrado");
        String htmlContent = new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);

        List<ExternalEventData> events = adapter.parseHtmlContent(htmlContent);

        assertNotNull(events);
        assertEquals(3, events.size());

        ExternalEventData event1 = events.get(0);
        assertEquals("lm-101", event1.externalId());
        assertEquals("Japan Weekend Madrid 2026", event1.name());
        assertEquals("Madrid", event1.city());
        assertEquals("IFEMA", event1.venue());
        assertEquals(2026, event1.startDate().getYear());
        assertEquals(Month.SEPTEMBER, event1.startDate().getMonth());
        assertEquals(19, event1.startDate().getDayOfMonth());
        assertEquals(20, event1.endDate().getDayOfMonth());

        ExternalEventData event2 = events.get(1);
        assertEquals("lm-102", event2.externalId());
        assertEquals("Manga Fest Sevilla 2026", event2.name());
        assertEquals("Sevilla", event2.city());
    }
}

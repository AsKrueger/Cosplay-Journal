package com.cosplayjournal.infrastructure.adapter.out.external.listadomanga;

import com.cosplayjournal.application.dto.ExternalEventData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.time.Month;
import java.time.YearMonth;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ListadoMangaEventSourceAdapterTest {

    private ListadoMangaEventSourceAdapter adapter;

    @BeforeEach
    void setUp() {
        adapter = new ListadoMangaEventSourceAdapter("https://www.listadomanga.es/salones.php", 5000);
    }

    @Test
    @DisplayName("El mes actual usa la URL base; los demás meses usan mes y ano como parámetros")
    void shouldBuildMonthUrlsWithoutRelyingOnRedirect() {
        String base = "https://www.listadomanga.es/salones.php";
        YearMonth current = YearMonth.of(2026, 10);

        assertEquals(base, ListadoMangaEventSourceAdapter.monthUrl(base, current, current));
        assertEquals(base + "?mes=11&ano=2026",
                ListadoMangaEventSourceAdapter.monthUrl(base, YearMonth.of(2026, 11), current));
        assertEquals("http://localhost/salones.php?x=1&mes=1&ano=2027",
                ListadoMangaEventSourceAdapter.monthUrl("http://localhost/salones.php?x=1", YearMonth.of(2027, 1), current));
    }

    @Test
    @DisplayName("Debe parsear correctamente el fixture HTML offline de ListadoManga sin conexión a Internet")
    void shouldParseOfflineHtmlFixture() throws IOException {
        InputStream inputStream = getClass().getResourceAsStream("/fixtures/listadomanga_salones.html");
        assertNotNull(inputStream, "El fixture HTML no fue encontrado");
        String htmlContent = new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);

        List<ExternalEventData> events = adapter.parseHtmlContent(htmlContent);

        assertNotNull(events);
        assertTrue(events.size() >= 30, "La captura tiene más de 30 salones, se extrajeron " + events.size());

        ExternalEventData first = events.get(0);
        assertEquals("San Diego Comic-Con Málaga 2026", first.name());
        assertEquals("Málaga", first.city());
        assertEquals("FYCMA Palacio de Ferias y Congresos de Málaga", first.venue());
        assertEquals(2026, first.startDate().getYear());
        assertEquals(Month.OCTOBER, first.startDate().getMonth());
        assertEquals(1, first.startDate().getDayOfMonth());
        assertEquals(4, first.endDate().getDayOfMonth());
        assertTrue(first.externalId().startsWith("lm-"), "El identificador externo debe usar el prefijo lm-");
    }
}

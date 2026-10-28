package com.cosplayjournal.infrastructure.adapter.out.external.listadomanga;

import com.cosplayjournal.application.dto.ExternalEventData;
import com.cosplayjournal.infrastructure.adapter.out.external.listadomanga.exception.ListadoMangaUnavailableException;
import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Prueba la importación mensual contra un servidor HTTP local, sin conexión a Internet.
 */
class ListadoMangaMonthlyFetchTest {

    private static final String FIXTURE_PATH = "/fixtures/listadomanga_salones.html";

    @Test
    @DisplayName("Un mes con error no detiene la importación y los eventos repetidos en varios meses se eliminan")
    void shouldContinueWhenOneMonthFailsAndDeduplicate() throws IOException {
        String fixture = readFixture();
        // La captura se sirve para todos los meses, así que cada evento aparece en varios;
        // marzo responde con error para comprobar la tolerancia.
        HttpServer server = startServer(query -> query.contains("mes=3&") ? 500 : 200, fixture);
        try {
            ListadoMangaEventSourceAdapter adapter = adapterFor(server);

            List<ExternalEventData> events = adapter.fetchEvents();

            Set<String> expectedIds = distinctIds(new ListadoMangaEventSourceAdapter("http://unused", 5000)
                    .parseHtmlContent(fixture));
            assertTrue(events.size() > 0, "Debe importar eventos aunque un mes falle");
            assertEquals(expectedIds.size(), events.size(), "No debe haber identificadores repetidos");
            assertEquals(expectedIds, distinctIds(events));
        } finally {
            server.stop(0);
        }
    }

    @Test
    @DisplayName("Si ningún mes se puede importar, la importación falla con excepción")
    void shouldFailWhenEveryMonthFails() throws IOException {
        HttpServer server = startServer(query -> 500, readFixture());
        try {
            ListadoMangaEventSourceAdapter adapter = adapterFor(server);

            assertThrows(ListadoMangaUnavailableException.class, adapter::fetchEvents);
        } finally {
            server.stop(0);
        }
    }

    private ListadoMangaEventSourceAdapter adapterFor(HttpServer server) {
        String url = "http://localhost:" + server.getAddress().getPort() + "/salones.php";
        // Sin reintentos: un mes con error falla una vez y pasa al siguiente
        return new ListadoMangaEventSourceAdapter(url, 5000, 5000, false, 1, 0L);
    }

    private static Set<String> distinctIds(List<ExternalEventData> events) {
        return events.stream().map(ExternalEventData::externalId).collect(Collectors.toCollection(HashSet::new));
    }

    private interface StatusForQuery {
        int statusFor(String query);
    }

    private static HttpServer startServer(StatusForQuery statusForQuery, String body) throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress("localhost", 0), 0);
        server.createContext("/salones.php", exchange -> {
            String query = exchange.getRequestURI().getRawQuery();
            int status = statusForQuery.statusFor(query == null ? "" : query + "&");
            byte[] bytes = (status == 200 ? body : "error").getBytes(StandardCharsets.UTF_8);
            exchange.sendResponseHeaders(status, bytes.length);
            try (OutputStream out = exchange.getResponseBody()) {
                out.write(bytes);
            }
        });
        server.start();
        return server;
    }

    private static String readFixture() throws IOException {
        try (InputStream in = ListadoMangaMonthlyFetchTest.class.getResourceAsStream(FIXTURE_PATH)) {
            assertTrue(in != null, "El fixture HTML no fue encontrado");
            return new String(in.readAllBytes(), StandardCharsets.UTF_8);
        }
    }
}

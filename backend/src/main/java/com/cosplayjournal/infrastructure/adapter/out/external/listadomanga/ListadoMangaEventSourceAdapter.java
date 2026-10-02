package com.cosplayjournal.infrastructure.adapter.out.external.listadomanga;

import com.cosplayjournal.application.dto.ExternalEventData;
import com.cosplayjournal.application.port.out.ExternalEventSourcePort;
import com.cosplayjournal.infrastructure.adapter.out.external.listadomanga.exception.ListadoMangaUnavailableException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.LocalDate;
import java.time.Month;
import java.time.Year;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class ListadoMangaEventSourceAdapter implements ExternalEventSourcePort {

    private static final Logger log = LoggerFactory.getLogger(ListadoMangaEventSourceAdapter.class);

    private final String baseUrl;
    private final int connectTimeoutMs;
    private final int readTimeoutMs;
    private final boolean retryEnabled;
    private final int maxAttempts;
    private final long backoffMs;

    private static final Map<String, Month> SPANISH_MONTHS = Map.ofEntries(
            Map.entry("enero", Month.JANUARY), Map.entry("ene", Month.JANUARY),
            Map.entry("febrero", Month.FEBRUARY), Map.entry("feb", Month.FEBRUARY),
            Map.entry("marzo", Month.MARCH), Map.entry("mar", Month.MARCH),
            Map.entry("abril", Month.APRIL), Map.entry("abr", Month.APRIL),
            Map.entry("mayo", Month.MAY), Map.entry("may", Month.MAY),
            Map.entry("junio", Month.JUNE), Map.entry("jun", Month.JUNE),
            Map.entry("julio", Month.JULY), Map.entry("jul", Month.JULY),
            Map.entry("agosto", Month.AUGUST), Map.entry("ago", Month.AUGUST),
            Map.entry("septiembre", Month.SEPTEMBER), Map.entry("sep", Month.SEPTEMBER),
            Map.entry("octubre", Month.OCTOBER), Map.entry("oct", Month.OCTOBER),
            Map.entry("noviembre", Month.NOVEMBER), Map.entry("nov", Month.NOVEMBER),
            Map.entry("diciembre", Month.DECEMBER), Map.entry("dic", Month.DECEMBER)
    );

    @Autowired
    public ListadoMangaEventSourceAdapter(
            @Value("${integrations.listadomanga.url:https://www.listadomanga.es/salones.php}") String baseUrl,
            @Value("${integrations.listadomanga.connect-timeout:5000}") int connectTimeoutMs,
            @Value("${integrations.listadomanga.read-timeout:10000}") int readTimeoutMs,
            @Value("${integrations.listadomanga.retry.enabled:true}") boolean retryEnabled,
            @Value("${integrations.listadomanga.retry.max-attempts:3}") int maxAttempts,
            @Value("${integrations.listadomanga.retry.backoff-ms:1000}") long backoffMs
    ) {
        this.baseUrl = baseUrl;
        this.connectTimeoutMs = connectTimeoutMs;
        this.readTimeoutMs = readTimeoutMs;
        this.retryEnabled = retryEnabled;
        this.maxAttempts = Math.max(1, maxAttempts);
        this.backoffMs = backoffMs;
    }

    public ListadoMangaEventSourceAdapter(String baseUrl, int timeoutMs) {
        this(baseUrl, timeoutMs, timeoutMs, true, 3, 100L);
    }

    @Override
    public List<ExternalEventData> fetchEvents() {
        int attempts = retryEnabled ? maxAttempts : 1;
        IOException lastException = null;

        for (int attempt = 1; attempt <= attempts; attempt++) {
            try {
                log.info("Conectando a fuente ListadoManga [Intento {}/{}]: {}", attempt, attempts, baseUrl);
                Document doc = Jsoup.connect(baseUrl)
                        .timeout(connectTimeoutMs + readTimeoutMs)
                        .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) CosplayJournalBot/1.0")
                        .get();
                return parseHtmlDocument(doc);
            } catch (IOException e) {
                lastException = e;
                log.warn("[Intento {}/{}] Falló la conexión con ListadoManga URL '{}': {}", attempt, attempts, baseUrl, e.getMessage());

                if (attempt < attempts && retryEnabled) {
                    long sleepTime = backoffMs * (1L << (attempt - 1));
                    try {
                        Thread.sleep(sleepTime);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        throw new ListadoMangaUnavailableException("Interrumpido durante la espera de reintento de ListadoManga", ie);
                    }
                }
            }
        }

        log.error("Todos los reintentos ({}/{}) fallaron para la fuente ListadoManga en URL '{}'", attempts, attempts, baseUrl);
        throw new ListadoMangaUnavailableException("No se pudo conectar a la fuente externa ListadoManga tras " + attempts + " intentos", lastException);
    }

    public List<ExternalEventData> parseHtmlContent(String htmlContent) {
        Document doc = Jsoup.parse(htmlContent, baseUrl);
        return parseHtmlDocument(doc);
    }

    private List<ExternalEventData> parseHtmlDocument(Document doc) {
        List<ExternalEventData> events = new ArrayList<>();
        Elements rows = doc.select("table tr");

        for (Element row : rows) {
            Elements cols = row.select("td");
            if (cols.size() < 3) {
                continue; // Omitir cabeceras o filas de formato
            }

            try {
                Element linkElem = cols.get(0).selectFirst("a");
                String name = linkElem != null ? linkElem.text().trim() : cols.get(0).text().trim();
                if (name.isEmpty() || name.equalsIgnoreCase("Nombre") || name.equalsIgnoreCase("Evento")) {
                    continue;
                }

                String externalId = extractExternalId(linkElem, name);
                String website = linkElem != null ? linkElem.absUrl("href") : baseUrl;

                String locationText = cols.get(1).text().trim();
                String dateText = cols.get(2).text().trim();

                String city = extractCity(locationText);
                String venue = extractVenue(locationText);

                LocalDate[] dates = parseSpanishDates(dateText);
                LocalDate startDate = dates[0];
                LocalDate endDate = dates[1];

                ExternalEventData eventData = new ExternalEventData(
                        externalId,
                        name,
                        "Evento de cosplay y manga importado desde ListadoManga (" + locationText + ")",
                        startDate,
                        endDate,
                        city,
                        venue,
                        city,
                        website
                );

                events.add(eventData);
            } catch (Exception ex) {
                log.warn("Error al procesar fila de evento de ListadoManga: {}", ex.getMessage());
            }
        }

        log.info("Extraídos {} eventos desde ListadoManga HTML", events.size());
        return events;
    }

    private String extractExternalId(Element linkElem, String name) {
        if (linkElem != null) {
            String href = linkElem.attr("href");
            if (href.contains("id=")) {
                String idParam = href.substring(href.indexOf("id=") + 3);
                if (idParam.contains("&")) {
                    idParam = idParam.substring(0, idParam.indexOf("&"));
                }
                if (!idParam.isEmpty()) {
                    return "lm-" + idParam;
                }
            }
        }
        return "lm-slug-" + Math.abs(name.toLowerCase().replaceAll("[^a-z0-9]", "").hashCode());
    }

    private String extractCity(String locationText) {
        if (locationText == null || locationText.trim().isEmpty()) return "España";
        if (locationText.contains("(")) {
            String inside = locationText.substring(locationText.indexOf("(") + 1, locationText.indexOf(")")).trim();
            if (!inside.isEmpty()) return inside;
        }
        if (locationText.contains("-")) {
            String[] parts = locationText.split("-");
            return parts[parts.length - 1].trim();
        }
        return locationText.trim();
    }

    private String extractVenue(String locationText) {
        if (locationText == null || locationText.trim().isEmpty()) return "";
        if (locationText.contains("(")) {
            return locationText.substring(0, locationText.indexOf("(")).trim();
        }
        if (locationText.contains("-")) {
            String[] parts = locationText.split("-");
            return parts[0].trim();
        }
        return locationText.trim();
    }

    private LocalDate[] parseSpanishDates(String dateText) {
        int currentYear = Year.now().getValue();
        LocalDate startDate = LocalDate.now();
        LocalDate endDate = LocalDate.now();

        try {
            Pattern yearPattern = Pattern.compile("\\b(20\\d{2})\\b");
            Matcher yearMatcher = yearPattern.matcher(dateText);
            if (yearMatcher.find()) {
                currentYear = Integer.parseInt(yearMatcher.group(1));
            }

            Pattern dayPattern = Pattern.compile("\\b(\\d{1,2})\\b");
            Matcher dayMatcher = dayPattern.matcher(dateText);
            List<Integer> days = new ArrayList<>();
            while (dayMatcher.find()) {
                int day = Integer.parseInt(dayMatcher.group(1));
                if (day >= 1 && day <= 31) {
                    days.add(day);
                }
            }

            Month month = Month.OCTOBER;
            String lower = dateText.toLowerCase();
            for (Map.Entry<String, Month> entry : SPANISH_MONTHS.entrySet()) {
                if (lower.contains(entry.getKey())) {
                    month = entry.getValue();
                    break;
                }
            }

            if (!days.isEmpty()) {
                int startDay = days.get(0);
                int endDay = days.size() > 1 ? days.get(days.size() - 1) : startDay;
                if (endDay < startDay) endDay = startDay;

                startDate = LocalDate.of(currentYear, month, Math.min(startDay, month.length(Year.isLeap(currentYear))));
                endDate = LocalDate.of(currentYear, month, Math.min(endDay, month.length(Year.isLeap(currentYear))));
            }
        } catch (Exception e) {
            log.warn("No se pudo parsear fecha '{}, usando fecha por defecto", dateText);
        }

        return new LocalDate[]{startDate, endDate};
    }
}

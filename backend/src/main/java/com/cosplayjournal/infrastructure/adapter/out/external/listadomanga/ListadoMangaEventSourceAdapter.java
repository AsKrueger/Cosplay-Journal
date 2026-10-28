package com.cosplayjournal.infrastructure.adapter.out.external.listadomanga;

import com.cosplayjournal.application.dto.ExternalEventData;
import com.cosplayjournal.application.port.out.ExternalEventSourcePort;
import com.cosplayjournal.infrastructure.adapter.out.external.listadomanga.exception.ListadoMangaUnavailableException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.LocalDate;
import java.time.Month;
import java.time.Year;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.LinkedHashMap;
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

    // "Del 1 al 4 de Octubre de 2026" o "12 de Mayo de 2026"
    private static final Pattern EVENT_DATE_PATTERN = Pattern.compile(
            "(?i)Del\\s+.{0,60}?\\d{4}|\\d{1,2}\\s+de\\s+\\p{L}+\\s+de\\s+\\d{4}");
    // "29006 Málaga"
    private static final Pattern POSTAL_CITY_PATTERN = Pattern.compile("^\\d{5}\\s+(.+)$");

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

    /**
     * Recorre todos los meses del periodo (un año atrás y un año adelante) usando
     * {@code salones.php?mes=M&ano=Y}. Un mes que falla no detiene la importación del resto;
     * solo se considera fallida la importación si fallan todos los meses.
     */
    @Override
    public List<ExternalEventData> fetchEvents() {
        Map<String, ExternalEventData> eventsById = new LinkedHashMap<>();
        YearMonth first = YearMonth.now().minusYears(1);
        YearMonth last = YearMonth.now().plusYears(1);
        int months = 0;
        int failedMonths = 0;

        for (YearMonth month = first; !month.isAfter(last); month = month.plusMonths(1)) {
            months++;
            String url = monthUrl(baseUrl, month, YearMonth.now());
            try {
                for (ExternalEventData event : parseHtmlDocument(fetchDocument(url))) {
                    eventsById.putIfAbsent(event.externalId(), event);
                }
            } catch (ListadoMangaUnavailableException e) {
                failedMonths++;
                log.warn("No se pudo importar el mes {}: {}", month, e.getMessage());
            }
        }

        if (failedMonths == months) {
            throw new ListadoMangaUnavailableException("No se pudo importar ningún mes de ListadoManga", null);
        }
        log.info("Importación de ListadoManga: {} eventos únicos en {} meses ({} con error)",
                eventsById.size(), months, failedMonths);
        return new ArrayList<>(eventsById.values());
    }

    /**
     * URL de un mes concreto. El sitio redirige {@code mes=M&ano=Y} del mes actual a la página
     * sin parámetros, así que el mes actual usa directamente la URL base.
     */
    static String monthUrl(String baseUrl, YearMonth month, YearMonth current) {
        if (month.equals(current)) {
            return baseUrl;
        }
        return baseUrl + (baseUrl.contains("?") ? "&" : "?")
                + "mes=" + month.getMonthValue() + "&ano=" + month.getYear();
    }

    private Document fetchDocument(String url) {
        int attempts = retryEnabled ? maxAttempts : 1;
        IOException lastException = null;

        for (int attempt = 1; attempt <= attempts; attempt++) {
            try {
                log.debug("Conectando a fuente ListadoManga [Intento {}/{}]: {}", attempt, attempts, url);
                return Jsoup.connect(url)
                        .timeout(connectTimeoutMs + readTimeoutMs)
                        .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) CosplayJournalBot/1.0")
                        .get();
            } catch (IOException e) {
                lastException = e;
                log.warn("[Intento {}/{}] Falló la conexión con ListadoManga URL '{}': {}", attempt, attempts, url, e.getMessage());

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

        log.error("Todos los reintentos ({}/{}) fallaron para la fuente ListadoManga en URL '{}'", attempts, attempts, url);
        throw new ListadoMangaUnavailableException("No se pudo conectar a la fuente externa ListadoManga tras " + attempts + " intentos", lastException);
    }

    public List<ExternalEventData> parseHtmlContent(String htmlContent) {
        Document doc = Jsoup.parse(htmlContent, baseUrl);
        return parseHtmlDocument(doc);
    }

    private List<ExternalEventData> parseHtmlDocument(Document doc) {
        List<ExternalEventData> events = new ArrayList<>();

        // Cada salón es una tarjeta: un <h2> con el nombre dentro de una celda <td>.
        // El enlace a la web del salón está en la celda contigua.
        for (Element heading : doc.select("h2")) {
            Element card = heading.parent();
            if (card == null || !card.tagName().equals("td")) {
                continue;
            }

            try {
                String name = heading.text().trim();
                if (name.isEmpty()) {
                    continue;
                }

                Matcher dateMatcher = EVENT_DATE_PATTERN.matcher(card.text());
                if (!dateMatcher.find()) {
                    continue; // Sin fecha no es una tarjeta de salón
                }
                LocalDate[] dates = parseSpanishDates(dateMatcher.group());

                Element linkElem = card.previousElementSibling() != null
                        ? card.previousElementSibling().selectFirst("a")
                        : null;
                String externalId = extractExternalId(linkElem, name);
                String website = linkElem != null && !linkElem.absUrl("href").isEmpty()
                        ? linkElem.absUrl("href")
                        : baseUrl;

                String[] address = extractAddressLines(card);
                String venue = address[0];
                String city = address[1];

                ExternalEventData eventData = new ExternalEventData(
                        externalId,
                        name,
                        "Evento de cosplay y manga importado desde ListadoManga",
                        dates[0],
                        dates[1],
                        city,
                        venue,
                        city,
                        website
                );

                events.add(eventData);
            } catch (Exception ex) {
                log.warn("Error al procesar tarjeta de evento de ListadoManga: {}", ex.getMessage());
            }
        }

        log.info("Extraídos {} eventos desde ListadoManga HTML", events.size());
        return events;
    }

    /**
     * Devuelve {lugar, ciudad} a partir del bloque "DIRECCIÓN" de la tarjeta.
     * Las líneas están separadas por etiquetas {@code <br>}; la última tiene la forma "CP Ciudad".
     */
    private String[] extractAddressLines(Element card) {
        String[] lines = card.html().split("(?i)<br\\s*/?>|<hr\\s*/?>");
        String venue = "";
        String city = "España";
        boolean afterHeader = false;

        for (String raw : lines) {
            String line = Jsoup.parse(raw).text().trim();
            if (line.isEmpty()) {
                continue;
            }
            if (line.toUpperCase().startsWith("DIRECCI")) {
                afterHeader = true;
                continue;
            }
            if (!afterHeader) {
                continue;
            }
            Matcher postal = POSTAL_CITY_PATTERN.matcher(line);
            if (postal.matches()) {
                city = postal.group(1).trim();
                break;
            }
            if (venue.isEmpty()) {
                venue = line;
            }
        }
        return new String[]{venue, city};
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

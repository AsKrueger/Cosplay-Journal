package com.cosplayjournal.app.data.repository

import android.content.Context
import com.cosplayjournal.app.data.model.Event
import kotlinx.serialization.json.Json
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jsoup.Jsoup
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class EventRepository(private val context: Context) {
    
    private val json = Json { ignoreUnknownKeys = true }
    private val url = "https://www.listadomanga.es/salones.php"
    private val dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")

    suspend fun getEvents(): List<Event> = withContext(Dispatchers.IO) {
        try {
            val doc = Jsoup.connect(url).get()
            // Buscamos la tabla de salones. ListadoManga usa tablas con clase 'listado'
            val eventRows = doc.select("table.listado tr").drop(1)

            if (eventRows.isEmpty()) return@withContext loadEventsFromAssets()

            eventRows.mapNotNull { row ->
                val cols = row.select("td")
                if (cols.size >= 3) {
                    val name = cols[0].text()
                    val dateText = cols[1].text()
                    val city = cols[2].text()
                    val link = cols[0].select("a").attr("abs:href")
                    
                    val (startDate, endDate) = parseEventDates(dateText)

                    Event(
                        id = name.hashCode().toString(),
                        name = name,
                        city = city,
                        venue = "",
                        startDate = startDate,
                        endDate = endDate,
                        website = link
                    )
                } else null
            }
        } catch (e: Exception) {
            e.printStackTrace()
            loadEventsFromAssets()
        }
    }

    private fun parseEventDates(dateText: String): Pair<String, String> {
        return try {
            // Caso: "12/05/2024 al 14/05/2024" o "12/05/2024"
            val parts = dateText.split(" al ")
            val start = parseSingleDate(parts[0].trim())
            val end = if (parts.size > 1) parseSingleDate(parts[1].trim()) else start
            Pair(start, end)
        } catch (e: Exception) {
            val now = LocalDate.now().toString()
            Pair(now, now)
        }
    }

    private fun parseSingleDate(dateStr: String): String {
        return try {
            // Intentar DD/MM/YYYY
            val date = LocalDate.parse(dateStr, dateFormatter)
            date.toString() // Devuelve YYYY-MM-DD
        } catch (e: Exception) {
            // Si falta el año (ej: "12/05"), asumimos el año actual
            try {
                val partialFormatter = DateTimeFormatter.ofPattern("dd/MM")
                val date = LocalDate.parse(dateStr, partialFormatter)
                    .withYear(LocalDate.now().year)
                date.toString()
            } catch (e2: Exception) {
                LocalDate.now().toString()
            }
        }
    }

    private fun loadEventsFromAssets(): List<Event> {
        return try {
            val jsonString = context.assets.open("events.json").bufferedReader().use { it.readText() }
            json.decodeFromString<List<Event>>(jsonString)
        } catch (e: Exception) {
            emptyList()
        }
    }
}

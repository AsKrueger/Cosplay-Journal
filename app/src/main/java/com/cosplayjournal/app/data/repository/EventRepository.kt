package com.cosplayjournal.app.data.repository

import com.cosplayjournal.app.data.model.Event
import com.cosplayjournal.app.data.remote.CosplayJournalApi
import com.cosplayjournal.app.data.remote.RemoteEvent
import com.cosplayjournal.app.data.remote.RemoteEventPage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import java.io.IOException

/**
 * Catálogo de eventos obtenido del backend.
 *
 * Los errores de red se propagan al llamador para que la UI pueda informar del fallo
 * en lugar de mostrar datos antiguos.
 */
class EventRepository(
    private val api: CosplayJournalApi,
    // Tiempos del sondeo del estado de sincronización (ajustables en tests)
    private val initialDelayMs: Long = 1_000,
    private val pollIntervalMs: Long = 2_000,
    private val maxPolls: Int = 30
) {

    suspend fun getEvents(): List<Event> = withContext(Dispatchers.IO) {
        val events = mutableListOf<Event>()
        var page = 0
        while (true) {
            val response: RemoteEventPage = api.getEvents(page)
            events += response.content.map { it.toEvent() }
            if (response.last) break
            page++
        }
        events
    }

    /**
     * Pide la sincronización al backend y espera a que termine, consultando su estado.
     * El backend la ejecuta en segundo plano y limita la frecuencia, así que es seguro llamarla al abrir.
     * Si se supera el tiempo máximo de espera, se devuelve sin error: la lista ya mostrada sigue siendo válida.
     */
    suspend fun refresh() = withContext(Dispatchers.IO) {
        val response = api.refreshEvents()
        if (!response.isSuccessful) throw IOException("El backend respondió ${response.code()} al actualizar")

        delay(initialDelayMs)
        repeat(maxPolls) {
            if (!api.refreshStatus().running) return@withContext
            delay(pollIntervalMs)
        }
    }

    private fun RemoteEvent.toEvent() = Event(
        id = id,
        name = name,
        city = city.orEmpty(),
        venue = venue.orEmpty(),
        startDate = startDate,
        endDate = endDate,
        website = website.orEmpty(),
        address = address.orEmpty()
    )
}

package com.cosplayjournal.app.domain

import com.cosplayjournal.app.data.events.Asistencia
import com.cosplayjournal.app.data.events.EventoConEstado
import com.cosplayjournal.app.data.gallery.FotoGaleria
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

enum class Orden { TODAS, EVENTO, COSPLAY, GRUPAL }

/** Un bloque de la galería: título (null = sin etiqueta) y sus fotos. */
data class Grupo(val clave: String, val titulo: String?, val fotos: List<FotoGaleria>)

fun fechaDe(foto: FotoGaleria, zona: ZoneId = ZoneId.systemDefault()): LocalDate =
    Instant.ofEpochMilli(foto.foto.fecha).atZone(zona).toLocalDate()

/**
 * Agrupa las fotos según el orden elegido. Las que no tienen esa etiqueta van al final,
 * para que se vea qué falta por ordenar.
 */
fun agrupar(fotos: List<FotoGaleria>, orden: Orden, nombreEvento: (Long) -> String?): List<Grupo> = when (orden) {
    Orden.TODAS -> listOf(Grupo("todas", null, fotos))
    Orden.GRUPAL -> listOf(Grupo("grupal", null, fotos.filter { it.etiquetas.grupal }))
    Orden.EVENTO -> {
        val (con, sin) = fotos.partition { it.etiquetas.eventoId != null }
        con.groupBy { it.etiquetas.eventoId!! }
            .entries.sortedByDescending { e -> e.value.maxOf { it.foto.fecha } }
            .map { (id, l) -> Grupo("e$id", nombreEvento(id) ?: "#$id", l) } +
            listOfNotNull(sin.takeIf { it.isNotEmpty() }?.let { Grupo("e-sin", null, it) })
    }
    Orden.COSPLAY -> {
        val (con, sin) = fotos.partition { it.etiquetas.cosplay != null }
        con.groupBy { it.etiquetas.cosplay!!.lowercase() }
            .entries.sortedBy { it.key }
            .map { (k, l) -> Grupo("c$k", l.first().etiquetas.cosplay, l) } +
            listOfNotNull(sin.takeIf { it.isNotEmpty() }?.let { Grupo("c-sin", null, it) })
    }
}

/**
 * Evento que encaja con la fecha de una foto. Primero los que marcaste como "fui" o "voy",
 * después cualquier evento de ese día (un día de margen por si la foto es de la noche anterior).
 */
fun sugerirEvento(fecha: LocalDate, eventos: List<EventoConEstado>): EventoConEstado? {
    val candidatos = eventos.filter { fecha >= it.evento.inicio.minusDays(1) && fecha <= it.evento.fin }
    return candidatos.firstOrNull { it.estado.asistencia == Asistencia.WENT || it.estado.asistencia == Asistencia.GOING }
        ?: candidatos.singleOrNull()
}

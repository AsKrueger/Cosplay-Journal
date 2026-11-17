package com.cosplayjournal.app.domain

import com.cosplayjournal.app.data.events.Asistencia
import com.cosplayjournal.app.data.events.EventoConEstado
import java.time.LocalDate
import kotlin.math.asin
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt

/** Color con el que se subraya cada evento en el calendario. */
enum class Tono {
    PASADO_NO_FUI,      // rojo
    PASADO_FUI,         // morado
    EN_CURSO,           // verde oscuro: estás en el evento
    FUTURO_VOY,         // verde claro
    FUTURO_SIN_MARCAR,  // azul
}

fun tono(inicio: LocalDate, fin: LocalDate, asistencia: Asistencia?, hoy: LocalDate): Tono {
    val voy = asistencia == Asistencia.GOING || asistencia == Asistencia.WENT
    return when {
        fin < hoy -> if (voy) Tono.PASADO_FUI else Tono.PASADO_NO_FUI
        inicio <= hoy -> if (voy) Tono.EN_CURSO else Tono.FUTURO_SIN_MARCAR
        else -> if (asistencia == Asistencia.GOING) Tono.FUTURO_VOY else Tono.FUTURO_SIN_MARCAR
    }
}

/** Opciones de asistencia que tienen sentido según la fecha. */
fun opcionesAsistencia(fin: LocalDate, hoy: LocalDate): List<Asistencia> =
    if (fin < hoy) listOf(Asistencia.WENT, Asistencia.MISSED) else listOf(Asistencia.GOING, Asistencia.NOT_GOING)

enum class Filtro { TODOS, FAVORITOS, CERCANOS, VOY, FUI }

data class Consulta(
    val filtro: Filtro = Filtro.TODOS,
    val texto: String = "",
    val pasados: Boolean = false,
    val miProvincia: String? = null,
    val radioKm: Int = 200,
    /** false en la vista de mes: muestra pasados y futuros. */
    val separarPasados: Boolean = true,
)

/** Aplica filtros, búsqueda y orden a la lista de eventos. */
fun filtrar(eventos: List<EventoConEstado>, c: Consulta, hoy: LocalDate): List<EventoConEstado> {
    val q = normalizar(c.texto)
    val lista = eventos.filter { e ->
        val ev = e.evento
        val estado = e.estado
        val enRango = !c.separarPasados || (if (c.pasados) ev.fin < hoy else ev.fin >= hoy)
        val coincide = q.isEmpty() || normalizar(ev.nombre).contains(q) || normalizar(ev.ciudad ?: "").contains(q) ||
            normalizar(ev.provincia?.let { Provincias.nombre(it) } ?: "").contains(q)
        val pasaFiltro = when (c.filtro) {
            Filtro.TODOS -> true
            Filtro.FAVORITOS -> estado.favorito
            Filtro.VOY -> estado.asistencia == Asistencia.GOING
            Filtro.FUI -> estado.asistencia == Asistencia.WENT
            Filtro.CERCANOS -> c.miProvincia != null && ev.provincia != null &&
                Provincias.distanciaKm(c.miProvincia, ev.provincia) <= c.radioKm
        }
        enRango && coincide && pasaFiltro
    }
    val porFecha = if (c.pasados) lista.sortedByDescending { it.evento.inicio } else lista.sortedBy { it.evento.inicio }
    return if (c.filtro == Filtro.CERCANOS && c.miProvincia != null) {
        porFecha.sortedBy { Provincias.distanciaKm(c.miProvincia, it.evento.provincia!!) }
    } else porFecha
}

/** Eventos que ocupan un día concreto (para la vista de mes). */
fun eventosDelDia(eventos: List<EventoConEstado>, dia: LocalDate) =
    eventos.filter { dia >= it.evento.inicio && dia <= it.evento.fin }

private fun normalizar(s: String) = java.text.Normalizer.normalize(s.lowercase().trim(), java.text.Normalizer.Form.NFD)
    .replace(Regex("\\p{M}"), "")

object Provincias {
    /** Código postal (2 dígitos) → nombre y centroide aproximado. Igual que en scraper/provincias.py */
    val TODAS: Map<String, Triple<String, Double, Double>> = mapOf(
        "01" to Triple("Álava", 42.85, -2.68), "02" to Triple("Albacete", 38.99, -1.86),
        "03" to Triple("Alicante", 38.35, -0.48), "04" to Triple("Almería", 36.84, -2.46),
        "05" to Triple("Ávila", 40.66, -4.70), "06" to Triple("Badajoz", 38.88, -6.97),
        "07" to Triple("Illes Balears", 39.57, 2.65), "08" to Triple("Barcelona", 41.39, 2.17),
        "09" to Triple("Burgos", 42.34, -3.70), "10" to Triple("Cáceres", 39.47, -6.37),
        "11" to Triple("Cádiz", 36.53, -6.29), "12" to Triple("Castellón", 39.99, -0.05),
        "13" to Triple("Ciudad Real", 38.99, -3.93), "14" to Triple("Córdoba", 37.89, -4.78),
        "15" to Triple("A Coruña", 43.36, -8.41), "16" to Triple("Cuenca", 40.07, -2.14),
        "17" to Triple("Girona", 41.98, 2.82), "18" to Triple("Granada", 37.18, -3.60),
        "19" to Triple("Guadalajara", 40.63, -3.17), "20" to Triple("Gipuzkoa", 43.32, -1.98),
        "21" to Triple("Huelva", 37.26, -6.95), "22" to Triple("Huesca", 42.14, -0.41),
        "23" to Triple("Jaén", 37.77, -3.79), "24" to Triple("León", 42.60, -5.57),
        "25" to Triple("Lleida", 41.62, 0.62), "26" to Triple("La Rioja", 42.47, -2.45),
        "27" to Triple("Lugo", 43.01, -7.56), "28" to Triple("Madrid", 40.42, -3.70),
        "29" to Triple("Málaga", 36.72, -4.42), "30" to Triple("Murcia", 37.99, -1.13),
        "31" to Triple("Navarra", 42.81, -1.64), "32" to Triple("Ourense", 42.34, -7.86),
        "33" to Triple("Asturias", 43.36, -5.85), "34" to Triple("Palencia", 42.01, -4.53),
        "35" to Triple("Las Palmas", 28.12, -15.43), "36" to Triple("Pontevedra", 42.43, -8.64),
        "37" to Triple("Salamanca", 40.97, -5.66), "38" to Triple("Santa Cruz de Tenerife", 28.46, -16.25),
        "39" to Triple("Cantabria", 43.46, -3.80), "40" to Triple("Segovia", 40.95, -4.12),
        "41" to Triple("Sevilla", 37.39, -5.98), "42" to Triple("Soria", 41.76, -2.46),
        "43" to Triple("Tarragona", 41.12, 1.25), "44" to Triple("Teruel", 40.34, -1.11),
        "45" to Triple("Toledo", 39.86, -4.02), "46" to Triple("Valencia", 39.47, -0.38),
        "47" to Triple("Valladolid", 41.65, -4.72), "48" to Triple("Bizkaia", 43.26, -2.93),
        "49" to Triple("Zamora", 41.50, -5.75), "50" to Triple("Zaragoza", 41.65, -0.89),
        "51" to Triple("Ceuta", 35.89, -5.32), "52" to Triple("Melilla", 35.29, -2.94),
    )

    fun nombre(codigo: String): String? = TODAS[codigo]?.first

    fun distanciaKm(a: String, b: String): Double {
        val pa = TODAS[a] ?: return Double.MAX_VALUE
        val pb = TODAS[b] ?: return Double.MAX_VALUE
        return haversine(pa.second, pa.third, pb.second, pb.third)
    }

    /** Provincia cuyo centroide está más cerca de unas coordenadas. */
    fun masCercana(lat: Double, lon: Double): String =
        TODAS.minBy { (_, p) -> haversine(lat, lon, p.second, p.third) }.key

    private fun haversine(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val r = 6371.0
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val a = sin(dLat / 2).pow(2) + cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) * sin(dLon / 2).pow(2)
        return 2 * r * asin(sqrt(a))
    }
}

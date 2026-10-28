package com.cosplayjournal.app.ui.calendar

import java.time.LocalDate

/** Estado visual de un evento en el calendario, según la fecha y la participación del usuario. */
enum class EventCalendarState {
    /** Ya pasó y el usuario no lo marcó como asistido. */
    PAST_MISSED,
    /** Ya pasó y el usuario asistió. */
    PAST_ATTENDED,
    /** Se está celebrando (o es hoy) y el usuario va a asistir. */
    ONGOING_ATTENDING,
    /** Evento futuro al que el usuario va a asistir. */
    FUTURE_ATTENDING,
    /** Evento futuro que el usuario todavía no ha seleccionado. */
    FUTURE_OPEN
}

/**
 * Calcula el estado de un evento.
 *
 * Estados de participación que se usan: "ATTENDING" (lo tengo seleccionado),
 * "ATTENDED" (asistí). Cualquier otro valor cuenta como no seleccionado.
 */
fun eventCalendarState(
    startDate: LocalDate,
    endDate: LocalDate,
    participation: String,
    today: LocalDate
): EventCalendarState {
    val attending = participation == "ATTENDING"
    val attended = participation == "ATTENDED"
    return when {
        endDate.isBefore(today) -> if (attended) EventCalendarState.PAST_ATTENDED else EventCalendarState.PAST_MISSED
        !startDate.isAfter(today) -> if (attending || attended) EventCalendarState.ONGOING_ATTENDING else EventCalendarState.FUTURE_OPEN
        attending -> EventCalendarState.FUTURE_ATTENDING
        else -> EventCalendarState.FUTURE_OPEN
    }
}

/** Convierte "YYYY-MM-DD" en fecha; si no se puede leer, devuelve null. */
fun parseEventDate(value: String): LocalDate? = try {
    LocalDate.parse(value)
} catch (e: Exception) {
    null
}

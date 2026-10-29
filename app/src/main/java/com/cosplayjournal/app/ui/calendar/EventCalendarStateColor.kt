package com.cosplayjournal.app.ui.calendar

import androidx.compose.ui.graphics.Color
import java.time.LocalDate

/** Colores del subrayado de cada evento en el calendario. */
fun EventCalendarState.underlineColor(): Color = when (this) {
    EventCalendarState.PAST_MISSED -> Color(0xFFD32F2F)
    EventCalendarState.PAST_ATTENDED -> Color(0xFF7B1FA2)
    EventCalendarState.ONGOING_ATTENDING -> Color(0xFF1B5E20)
    EventCalendarState.FUTURE_ATTENDING -> Color(0xFF81C784)
    EventCalendarState.FUTURE_OPEN -> Color(0xFF1E88E5)
}

/** Color del subrayado de un evento, a partir de sus fechas (YYYY-MM-DD) y la participación del usuario. */
fun underlineFor(
    startDate: String,
    endDate: String,
    participation: String,
    today: LocalDate = LocalDate.now()
): Color {
    val start = parseEventDate(startDate)
    val end = parseEventDate(endDate) ?: start
    val state = if (start == null || end == null) {
        EventCalendarState.FUTURE_OPEN
    } else {
        eventCalendarState(start, end, participation, today)
    }
    return state.underlineColor()
}

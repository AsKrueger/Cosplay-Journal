package com.cosplayjournal.app.ui.calendar

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.time.LocalDate

class EventCalendarStateTest {

    private val today = LocalDate.of(2026, 10, 28)

    @Test
    fun pastEventNotAttendedIsMissed() {
        val state = eventCalendarState(LocalDate.of(2026, 10, 1), LocalDate.of(2026, 10, 4), "NONE", today)
        assertEquals(EventCalendarState.PAST_MISSED, state)
    }

    @Test
    fun pastEventSelectedButNotMarkedAttendedIsStillMissed() {
        val state = eventCalendarState(LocalDate.of(2026, 10, 1), LocalDate.of(2026, 10, 4), "ATTENDING", today)
        assertEquals(EventCalendarState.PAST_MISSED, state)
    }

    @Test
    fun pastEventMarkedAttendedIsAttended() {
        val state = eventCalendarState(LocalDate.of(2026, 10, 1), LocalDate.of(2026, 10, 4), "ATTENDED", today)
        assertEquals(EventCalendarState.PAST_ATTENDED, state)
    }

    @Test
    fun ongoingEventSelectedIsDarkGreen() {
        val state = eventCalendarState(LocalDate.of(2026, 10, 27), LocalDate.of(2026, 10, 29), "ATTENDING", today)
        assertEquals(EventCalendarState.ONGOING_ATTENDING, state)
    }

    @Test
    fun eventStartingTodayCountsAsOngoing() {
        val state = eventCalendarState(today, today.plusDays(1), "ATTENDING", today)
        assertEquals(EventCalendarState.ONGOING_ATTENDING, state)
    }

    @Test
    fun futureEventSelectedIsLightGreen() {
        val state = eventCalendarState(LocalDate.of(2026, 11, 5), LocalDate.of(2026, 11, 7), "ATTENDING", today)
        assertEquals(EventCalendarState.FUTURE_ATTENDING, state)
    }

    @Test
    fun futureEventNotSelectedIsBlue() {
        val state = eventCalendarState(LocalDate.of(2026, 11, 5), LocalDate.of(2026, 11, 7), "NONE", today)
        assertEquals(EventCalendarState.FUTURE_OPEN, state)
    }

    @Test
    fun unknownDateStringIsNull() {
        assertNull(parseEventDate("no-es-fecha"))
        assertEquals(LocalDate.of(2026, 11, 5), parseEventDate("2026-11-05"))
    }
}

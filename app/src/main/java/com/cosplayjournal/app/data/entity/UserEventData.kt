package com.cosplayjournal.app.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/** Participación del usuario en un evento: ATTENDING, ATTENDED, INTERESTED, PLANNING o NONE. */
@Entity(tableName = "user_event_data")
data class UserEventData(
    @PrimaryKey val eventId: String,
    val status: String = "NONE"
)

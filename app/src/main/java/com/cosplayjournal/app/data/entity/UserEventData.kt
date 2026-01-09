package com.cosplayjournal.app.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_event_data")
data class UserEventData(
    @PrimaryKey val eventId: String,
    val status: String = "NONE", // "ATTENDING", "INTERESTED", "PLANNING", "NONE"
    val isFavorite: Boolean = false
)

@Entity(
    tableName = "event_cosplan_selection",
    primaryKeys = ["eventId", "cosplanId", "day"]
)
data class EventCosplanSelection(
    val eventId: String,
    val cosplanId: Long,
    val day: String // e.g., "2024-07-01"
)

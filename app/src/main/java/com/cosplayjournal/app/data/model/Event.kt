package com.cosplayjournal.app.data.model

import kotlinx.serialization.Serializable

@Serializable
data class Event(
    val id: String,
    val name: String,
    val city: String,
    val venue: String,
    val startDate: String,
    val endDate: String,
    val website: String,
    val ticketsLink: String,
    val image: String? = null
)

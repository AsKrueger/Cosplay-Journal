package com.cosplayjournal.app.data.model

import kotlinx.serialization.Serializable

@Serializable
data class Event(
    val id: String,
    val name: String,
    val city: String,
    val venue: String,
    val startDate: String, // format "YYYY-MM-DD"
    val endDate: String,
    val website: String,
    val image: String? = null,
    val schedule: String = "",
    val price: String = "",
    val address: String = ""
)

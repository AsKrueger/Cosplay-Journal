package com.cosplayjournal.app.data.repository

import android.content.Context
import com.cosplayjournal.app.data.model.Event
import kotlinx.serialization.json.Json
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class EventRepository(private val context: Context) {
    
    private val json = Json { ignoreUnknownKeys = true }

    suspend fun getEvents(): List<Event> = withContext(Dispatchers.IO) {
        try {
            val jsonString = context.assets.open("events.json").bufferedReader().use { it.readText() }
            json.decodeFromString<List<Event>>(jsonString)
        } catch (e: Exception) {
            emptyList()
        }
    }
}

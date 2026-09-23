package com.cosplayjournal.app

import android.app.Application
import com.cosplayjournal.app.data.AppDatabase
import com.cosplayjournal.app.data.repository.CosplayRepository
import com.cosplayjournal.app.data.repository.EventRepository
import com.cosplayjournal.app.data.repository.LocationRepository
import com.cosplayjournal.app.data.repository.SettingsRepository

class CosplayJournalApplication : Application() {
    val database by lazy { AppDatabase.getDatabase(this) }
    val repository by lazy { CosplayRepository(database.cosplayDao()) }
    val eventRepository by lazy { EventRepository(this) }
    val locationRepository by lazy { LocationRepository(database.locationDao()) }
    val settingsRepository by lazy { SettingsRepository(this) }
}

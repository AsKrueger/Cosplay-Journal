package com.cosplayjournal.app

import android.app.Application
import com.cosplayjournal.app.data.AppDatabase
import com.cosplayjournal.app.data.repository.CosplayRepository
import com.cosplayjournal.app.data.repository.EventRepository

class CosplayJournalApplication : Application() {
    val database by lazy { AppDatabase.getDatabase(this) }
    val repository by lazy { CosplayRepository(database.cosplayDao()) }
    val eventRepository by lazy { EventRepository(this) }
}

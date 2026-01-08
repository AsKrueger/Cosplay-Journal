package com.cosplayjournal.app

import android.app.Application
import com.cosplayjournal.app.data.AppDatabase
import com.cosplayjournal.app.data.repository.CosplayRepository

class CosplayJournalApplication : Application() {
    val database by lazy { AppDatabase.getDatabase(this) }
    val repository by lazy { CosplayRepository(database.cosplayDao()) }
}

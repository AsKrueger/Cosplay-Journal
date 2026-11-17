package com.cosplayjournal.app.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.cosplayjournal.app.data.events.Conversores
import com.cosplayjournal.app.data.events.EventDao
import com.cosplayjournal.app.data.events.EventoEntity
import com.cosplayjournal.app.data.events.MiEstadoEntity

@Database(
    entities = [EventoEntity::class, MiEstadoEntity::class],
    version = 1,
    exportSchema = true,
)
@TypeConverters(Conversores::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun eventos(): EventDao

    companion object {
        fun crear(context: Context): AppDatabase =
            Room.databaseBuilder(context, AppDatabase::class.java, "cosplay_journal.db").build()
    }
}

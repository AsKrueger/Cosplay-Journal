package com.cosplayjournal.app.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.cosplayjournal.app.data.dao.UserEventDao
import com.cosplayjournal.app.data.entity.UserEventData

@Database(
    entities = [UserEventData::class],
    version = 7, // Se retiran los módulos de cosplans, cosplays y localizaciones
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userEventDao(): UserEventDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "cosplay_journal_db"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}

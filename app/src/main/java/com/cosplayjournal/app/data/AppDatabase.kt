package com.cosplayjournal.app.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.cosplayjournal.app.data.dao.CosplayDao
import com.cosplayjournal.app.data.entity.*

@Database(
    entities = [
        Cosplan::class,
        Cosplay::class,
        HandmadePart::class,
        PurchasedItem::class,
        CharacterReference::class,
        Location::class,
        CosplayReferenceCrossRef::class,
        PhotoSession::class,
        CosplayPhotoSessionCrossRef::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun cosplayDao(): CosplayDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "cosplay_journal_db"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}

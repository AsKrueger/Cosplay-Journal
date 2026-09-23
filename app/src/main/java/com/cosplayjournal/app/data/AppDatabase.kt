package com.cosplayjournal.app.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.cosplayjournal.app.data.dao.CosplayDao
import com.cosplayjournal.app.data.dao.LocationDao
import com.cosplayjournal.app.data.entity.*

@Database(
    entities = [
        Cosplan::class,
        Cosplay::class,
        HandmadePart::class,
        PartResource::class,
        PurchasedItem::class,
        CharacterReference::class,
        Location::class,
        CosplayReferenceCrossRef::class,
        PhotoSession::class,
        CosplayPhotoSessionCrossRef::class,
        UserEventData::class,
        EventCosplanSelection::class,
        WigMakeup::class
    ],
    version = 6, // Incremented version for WigMakeup addition
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun cosplayDao(): CosplayDao
    abstract fun locationDao(): LocationDao

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

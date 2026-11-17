package com.cosplayjournal.app.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.cosplayjournal.app.data.gallery.FotoDao
import com.cosplayjournal.app.data.gallery.FotoEtiquetas
import com.cosplayjournal.app.data.events.Conversores
import com.cosplayjournal.app.data.events.EventDao
import com.cosplayjournal.app.data.events.EventoEntity
import com.cosplayjournal.app.data.events.MiEstadoEntity

@Database(
    entities = [EventoEntity::class, MiEstadoEntity::class, FotoEtiquetas::class],
    version = 2,
    exportSchema = true,
)
@TypeConverters(Conversores::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun eventos(): EventDao
    abstract fun fotos(): FotoDao

    companion object {
        fun crear(context: Context): AppDatabase =
            Room.databaseBuilder(context, AppDatabase::class.java, "cosplay_journal.db")
                .addMigrations(MIGRACION_1_2)
                .build()

        /** v2: etiquetas de la galería. */
        val MIGRACION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "CREATE TABLE IF NOT EXISTS `fotos` (`uri` TEXT NOT NULL, `evento_id` INTEGER, `cosplay` TEXT, " +
                        "`grupal` INTEGER NOT NULL, PRIMARY KEY(`uri`))"
                )
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_fotos_evento_id` ON `fotos` (`evento_id`)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_fotos_cosplay` ON `fotos` (`cosplay`)")
            }
        }
    }
}

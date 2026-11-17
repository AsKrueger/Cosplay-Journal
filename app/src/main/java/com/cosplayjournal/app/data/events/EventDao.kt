package com.cosplayjournal.app.data.events

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.TypeConverter
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

class Conversores {
    @TypeConverter fun deFecha(d: LocalDate?): Long? = d?.toEpochDay()
    @TypeConverter fun aFecha(v: Long?): LocalDate? = v?.let(LocalDate::ofEpochDay)
    @TypeConverter fun deAsistencia(a: Asistencia?): String? = a?.name
    @TypeConverter fun aAsistencia(v: String?): Asistencia? = v?.let(Asistencia::valueOf)
}

@Dao
interface EventDao {
    @Transaction
    @Query("SELECT * FROM eventos ORDER BY inicio, nombre")
    fun todos(): Flow<List<EventoConEstado>>

    @Transaction
    @Query("SELECT * FROM eventos WHERE id = :id")
    fun uno(id: Long): Flow<EventoConEstado?>

    @Query("SELECT * FROM mi_estado WHERE pendiente = 1")
    suspend fun pendientes(): List<MiEstadoEntity>

    @Query("SELECT * FROM mi_estado WHERE evento_id = :id")
    suspend fun estado(id: Long): MiEstadoEntity?

    @Upsert suspend fun guardarEventos(eventos: List<EventoEntity>)
    @Upsert suspend fun guardarEstado(estado: MiEstadoEntity)
    @Upsert suspend fun guardarEstados(estados: List<MiEstadoEntity>)

    @Query("DELETE FROM eventos WHERE id NOT IN (:ids)")
    suspend fun borrarEventosSalvo(ids: List<Long>)

    /** Borra los estados ya sincronizados; los pendientes se conservan para subirlos. */
    @Query("DELETE FROM mi_estado WHERE pendiente = 0")
    suspend fun borrarEstadosSincronizados()

    @Query("SELECT COUNT(*) FROM eventos") suspend fun cuantos(): Int

    @Query("DELETE FROM eventos WHERE fin < :limite")
    suspend fun borrarTerminadosAntesDe(limite: LocalDate)

    @Query("DELETE FROM eventos") suspend fun borrarEventos()
    @Query("DELETE FROM mi_estado") suspend fun borrarEstados()
}

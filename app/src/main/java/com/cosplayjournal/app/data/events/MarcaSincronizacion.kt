package com.cosplayjournal.app.data.events

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.first
import java.time.LocalDate

/**
 * Hasta dónde está al día el calendario guardado en el móvil.
 * @param cambiosDesde `updated_at` más reciente recibido del servidor (su reloj, no el del móvil).
 * @param ultimaCompleta día de la última descarga completa.
 */
data class Marca(val cambiosDesde: String?, val ultimaCompleta: LocalDate)

interface MarcaSincronizacion {
    suspend fun leer(): Marca?
    suspend fun guardar(marca: Marca)
    suspend fun borrar()
}

private val Context.sincronizacion: DataStore<Preferences> by preferencesDataStore(name = "sincronizacion")

class DataStoreMarcaSincronizacion(context: Context) : MarcaSincronizacion {
    private val store = context.applicationContext.sincronizacion

    override suspend fun leer(): Marca? {
        val p = store.data.first()
        val dia = p[COMPLETA] ?: return null
        return Marca(p[DESDE], LocalDate.ofEpochDay(dia))
    }

    override suspend fun guardar(marca: Marca) {
        store.edit {
            if (marca.cambiosDesde == null) it.remove(DESDE) else it[DESDE] = marca.cambiosDesde
            it[COMPLETA] = marca.ultimaCompleta.toEpochDay()
        }
    }

    override suspend fun borrar() {
        store.edit { it.clear() }
    }

    private companion object {
        val DESDE = stringPreferencesKey("eventos_cambios_desde")
        val COMPLETA = longPreferencesKey("eventos_ultima_completa")
    }
}

/** Para tests y para cuando no interesa recordar nada. */
class MarcaEnMemoria : MarcaSincronizacion {
    var marca: Marca? = null
    override suspend fun leer() = marca
    override suspend fun guardar(marca: Marca) { this.marca = marca }
    override suspend fun borrar() { marca = null }
}

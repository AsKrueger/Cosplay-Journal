package com.cosplayjournal.app.data.settings

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "ajustes")

data class Ajustes(
    val imagenesSinConexion: Boolean = true,
)

interface SettingsRepository {
    val ajustes: Flow<Ajustes>
    suspend fun setImagenesSinConexion(activo: Boolean)
    suspend fun borrarTodo()
}

class DataStoreSettingsRepository(context: Context) : SettingsRepository {
    private val store = context.applicationContext.dataStore

    override val ajustes: Flow<Ajustes> = store.data.map { p ->
        Ajustes(imagenesSinConexion = p[OFFLINE] ?: true)
    }

    override suspend fun setImagenesSinConexion(activo: Boolean) {
        store.edit { it[OFFLINE] = activo }
    }

    override suspend fun borrarTodo() {
        store.edit { it.clear() }
    }

    private companion object {
        val OFFLINE = booleanPreferencesKey("imagenes_sin_conexion")
    }
}

package com.cosplayjournal.app.data.settings

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "ajustes")

data class Ajustes(
    val imagenesSinConexion: Boolean = true,
    /** Código de provincia (2 dígitos) para el filtro "Cercanos". */
    val miProvincia: String? = null,
    /** Carpeta del móvil vinculada a la galería (URI del selector de documentos). */
    val carpetaGaleria: String? = null,
)

interface SettingsRepository {
    val ajustes: Flow<Ajustes>
    suspend fun setImagenesSinConexion(activo: Boolean)
    suspend fun setMiProvincia(codigo: String?)
    suspend fun setCarpetaGaleria(uri: String?)
    suspend fun borrarTodo()
}

class DataStoreSettingsRepository(context: Context) : SettingsRepository {
    private val store = context.applicationContext.dataStore

    override val ajustes: Flow<Ajustes> = store.data.map { p ->
        Ajustes(imagenesSinConexion = p[OFFLINE] ?: true, miProvincia = p[PROVINCIA], carpetaGaleria = p[CARPETA])
    }

    override suspend fun setImagenesSinConexion(activo: Boolean) {
        store.edit { it[OFFLINE] = activo }
    }

    override suspend fun setMiProvincia(codigo: String?) {
        store.edit { if (codigo == null) it.remove(PROVINCIA) else it[PROVINCIA] = codigo }
    }

    override suspend fun setCarpetaGaleria(uri: String?) {
        store.edit { if (uri == null) it.remove(CARPETA) else it[CARPETA] = uri }
    }

    override suspend fun borrarTodo() {
        store.edit { it.clear() }
    }

    private companion object {
        val OFFLINE = booleanPreferencesKey("imagenes_sin_conexion")
        val PROVINCIA = stringPreferencesKey("mi_provincia")
        val CARPETA = stringPreferencesKey("carpeta_galeria")
    }
}

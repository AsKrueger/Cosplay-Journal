package com.cosplayjournal.app.data.updates

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.net.HttpURLConnection
import java.net.URL

/** Una versión publicada en GitHub Releases: número y enlace directo al APK. */
data class VersionPublicada(val version: String, val url: String)

interface FuenteVersiones {
    suspend fun ultima(): VersionPublicada?
}

/** Recuerda qué versión dijo el usuario «Ahora no», para no insistir en cada apertura. */
interface VersionDescartada {
    suspend fun leer(): String?
    suspend fun guardar(version: String)
}

object Versiones {
    private fun partes(v: String) = v.trim().removePrefix("v").substringBefore('-').split('.').map { it.toIntOrNull() ?: 0 }

    /** true si [candidata] es mayor que [actual] comparando número a número ("2.10.0" > "2.9.3"). */
    fun esMasNueva(candidata: String, actual: String): Boolean {
        val a = partes(candidata)
        val b = partes(actual)
        for (i in 0 until maxOf(a.size, b.size)) {
            val x = a.getOrElse(i) { 0 }
            val y = b.getOrElse(i) { 0 }
            if (x != y) return x > y
        }
        return false
    }
}

/**
 * La app se distribuye gratis como APK en GitHub Releases (Google Play cuesta 25 $), así que
 * ella misma avisa cuando hay una versión nueva. Una consulta por apertura a la API pública de
 * GitHub, sin cuenta ni coste.
 */
class ActualizacionesRepository(
    private val fuente: FuenteVersiones,
    private val versionActual: String,
    private val descartada: VersionDescartada,
) {
    private val _disponible = MutableStateFlow<VersionPublicada?>(null)
    /** Versión más nueva que la instalada, o null. */
    val disponible: StateFlow<VersionPublicada?> = _disponible.asStateFlow()

    private val _aviso = MutableStateFlow<VersionPublicada?>(null)
    /** Igual que [disponible] pero solo si el usuario no la ha descartado: se muestra como diálogo. */
    val aviso: StateFlow<VersionPublicada?> = _aviso.asStateFlow()

    private var comprobada = false

    suspend fun comprobar() {
        if (comprobada) return
        comprobada = true
        val ultima = runCatching { fuente.ultima() }.getOrNull() ?: return
        if (!Versiones.esMasNueva(ultima.version, versionActual)) return
        _disponible.value = ultima
        if (runCatching { descartada.leer() }.getOrNull() != ultima.version) _aviso.value = ultima
    }

    suspend fun descartarAviso() {
        val v = _aviso.value ?: return
        _aviso.value = null
        runCatching { descartada.guardar(v.version) }
    }
}

class GitHubVersiones(private val repo: String) : FuenteVersiones {
    @Serializable
    private data class Recurso(val name: String, @SerialName("browser_download_url") val url: String)

    @Serializable
    private data class Release(
        @SerialName("tag_name") val tag: String,
        @SerialName("html_url") val pagina: String,
        val assets: List<Recurso> = emptyList(),
    )

    private val json = Json { ignoreUnknownKeys = true }

    override suspend fun ultima(): VersionPublicada? = withContext(Dispatchers.IO) {
        val c = URL("https://api.github.com/repos/$repo/releases/latest").openConnection() as HttpURLConnection
        try {
            c.connectTimeout = 10_000
            c.readTimeout = 10_000
            c.setRequestProperty("Accept", "application/vnd.github+json")
            if (c.responseCode != 200) return@withContext null // 404 = aún no hay ninguna publicada
            val r = json.decodeFromString<Release>(c.inputStream.bufferedReader().use { it.readText() })
            VersionPublicada(r.tag.removePrefix("v"), r.assets.firstOrNull { it.name.endsWith(".apk") }?.url ?: r.pagina)
        } finally {
            c.disconnect()
        }
    }
}

class PreferenciasVersionDescartada(context: Context) : VersionDescartada {
    private val prefs = context.applicationContext.getSharedPreferences("actualizaciones", Context.MODE_PRIVATE)
    override suspend fun leer(): String? = withContext(Dispatchers.IO) { prefs.getString(CLAVE, null) }

    // commit y no apply: si el usuario cierra la app justo después, la decisión ya está guardada
    override suspend fun guardar(version: String) {
        withContext(Dispatchers.IO) { prefs.edit().putString(CLAVE, version).commit() }
    }

    private companion object {
        const val CLAVE = "version_descartada"
    }
}

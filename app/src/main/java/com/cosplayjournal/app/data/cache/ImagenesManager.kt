package com.cosplayjournal.app.data.cache

import android.content.Context
import coil.Coil
import coil.ImageLoader
import coil.annotation.ExperimentalCoilApi
import coil.disk.DiskCache
import coil.memory.MemoryCache
import coil.request.CachePolicy
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.Interceptor
import okhttp3.OkHttpClient

/**
 * Cargador de imágenes de toda la app. Con "imágenes sin conexión" activo, carteles y fotos se
 * guardan en disco (hasta 250 MB); si se desactiva, solo se usan en memoria.
 */
class ImagenesManager(
    private val context: Context,
    /** Añade la sesión a las peticiones de imágenes privadas de Supabase Storage. */
    private val interceptorAuth: Interceptor? = null,
) {
    private val cacheDir get() = context.cacheDir.resolve("imagenes")

    fun crear(sinConexion: Boolean): ImageLoader = ImageLoader.Builder(context)
        .okHttpClient {
            OkHttpClient.Builder().apply { interceptorAuth?.let { addInterceptor(it) } }.build()
        }
        .memoryCache { MemoryCache.Builder(context).maxSizePercent(0.20).build() }
        .diskCache { DiskCache.Builder().directory(cacheDir).maxSizeBytes(250L * 1024 * 1024).build() }
        .diskCachePolicy(if (sinConexion) CachePolicy.ENABLED else CachePolicy.DISABLED)
        .crossfade(true)
        .respectCacheHeaders(false) // los carteles no cambian: se guardan aunque el servidor diga lo contrario
        .build()

    fun aplicar(sinConexion: Boolean) {
        Coil.setImageLoader(crear(sinConexion))
    }

    /** Bytes que ocupa la caché de la app (imágenes y temporales). */
    suspend fun tamañoCache(): Long = withContext(Dispatchers.IO) {
        context.cacheDir.walkBottomUp().filter { it.isFile }.sumOf { it.length() }
    }

    @OptIn(ExperimentalCoilApi::class)
    suspend fun borrarCache(): Unit = withContext(Dispatchers.IO) {
        Coil.imageLoader(context).apply {
            memoryCache?.clear()
            diskCache?.clear()
        }
        context.cacheDir.listFiles()?.forEach { it.deleteRecursively() }
    }
}

package com.cosplayjournal.app

import android.app.Application
import coil.ImageLoader
import coil.ImageLoaderFactory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

class CosplayJournalApp : Application(), ImageLoaderFactory {

    lateinit var container: AppContainer
        private set

    /** Corrutinas que sobreviven a las pantallas (sincronizaciones, limpieza). */
    val appScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
        // Aplica el ajuste guardado y lo vuelve a aplicar cada vez que cambia
        appScope.launch {
            container.settings.ajustes.map { it.imagenesSinConexion }.distinctUntilChanged()
                .collect { container.imagenes.aplicar(it) }
        }
    }

    // Cargador inicial mientras se lee el ajuste guardado.
    override fun newImageLoader(): ImageLoader = container.imagenes.crear(sinConexion = true)
}

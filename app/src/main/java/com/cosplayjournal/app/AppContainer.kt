package com.cosplayjournal.app

import android.content.Context
import com.cosplayjournal.app.data.auth.AuthRepository
import com.cosplayjournal.app.data.auth.SupabaseAuthRepository
import com.cosplayjournal.app.data.cache.ImagenesManager
import com.cosplayjournal.app.data.events.EventRepository
import com.cosplayjournal.app.data.events.SupabaseEventosRemotos
import com.cosplayjournal.app.data.feedback.FeedbackRepository
import com.cosplayjournal.app.data.gallery.CarpetaGuardada
import com.cosplayjournal.app.data.gallery.GalleryRepository
import com.cosplayjournal.app.data.gallery.LectorCarpetaSaf
import kotlinx.coroutines.flow.map
import com.cosplayjournal.app.data.local.AppDatabase
import com.cosplayjournal.app.reminders.Recordatorios
import com.cosplayjournal.app.ui.calendar.Avisos
import java.time.LocalDate
import com.cosplayjournal.app.data.feedback.SupabaseFeedbackRepository
import com.cosplayjournal.app.data.profile.ProfileRepository
import com.cosplayjournal.app.data.profile.SupabaseProfileRepository
import com.cosplayjournal.app.data.settings.DataStoreSettingsRepository
import com.cosplayjournal.app.data.settings.SettingsRepository
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.realtime.Realtime
import io.github.jan.supabase.storage.Storage
import okhttp3.Interceptor

/** Dependencias de la app creadas una sola vez. */
class AppContainer(context: Context) {
    private val appContext = context.applicationContext

    val configurado: Boolean = BuildConfig.SUPABASE_URL.isNotBlank() && BuildConfig.SUPABASE_ANON_KEY.isNotBlank()

    val supabase: SupabaseClient by lazy {
        check(configurado) { "Supabase no está configurado" }
        createSupabaseClient(BuildConfig.SUPABASE_URL, BuildConfig.SUPABASE_ANON_KEY) {
            install(Auth)
            install(Postgrest)
            install(Realtime)
            install(Storage)
        }
    }

    val settings: SettingsRepository = DataStoreSettingsRepository(appContext)
    val auth: AuthRepository by lazy { SupabaseAuthRepository(supabase) }
    val perfiles: ProfileRepository by lazy { SupabaseProfileRepository(supabase) }
    val comentarios: FeedbackRepository by lazy { SupabaseFeedbackRepository(supabase) }

    val db: AppDatabase by lazy { AppDatabase.crear(appContext) }
    val eventos: EventRepository by lazy {
        EventRepository(db.eventos(), SupabaseEventosRemotos(supabase), userId = { auth.userIdActual() })
    }
    val galeria: GalleryRepository by lazy {
        GalleryRepository(db.fotos(), LectorCarpetaSaf(appContext), object : CarpetaGuardada {
            override val carpeta = settings.ajustes.map { it.carpetaGaleria }
            override suspend fun guardar(uri: String?) = settings.setCarpetaGaleria(uri)
        })
    }
    val avisos: Avisos = object : Avisos {
        override fun programar(eventoId: Long, nombre: String, inicio: LocalDate) =
            Recordatorios.programar(appContext, eventoId, nombre, inicio)
        override fun cancelar(eventoId: Long) = Recordatorios.cancelar(appContext, eventoId)
    }

    /** Las imágenes privadas de Storage se piden con el token de la sesión. */
    private val interceptorStorage = Interceptor { chain ->
        val req = chain.request()
        val token = if (configurado && req.url.toString().startsWith(BuildConfig.SUPABASE_URL + "/storage/")) {
            supabase.auth.currentAccessTokenOrNull()
        } else null
        chain.proceed(
            if (token == null) req
            else req.newBuilder()
                .header("Authorization", "Bearer $token")
                .header("apikey", BuildConfig.SUPABASE_ANON_KEY)
                .build()
        )
    }
    val imagenes = ImagenesManager(appContext, interceptorStorage)

    /** Cada módulo con datos locales registra aquí cómo borrarlos (Ajustes → Borrar datos). */
    private val limpiadores = mutableListOf<suspend () -> Unit>(
        // La galería primero: necesita leer la carpeta guardada para soltar su permiso
        { galeria.borrarLocal() },
        { settings.borrarTodo() },
        { if (configurado) eventos.borrarLocal() },
        { Recordatorios.cancelarTodos(appContext) },
    )

    fun registrarLimpiador(limpiador: suspend () -> Unit) {
        limpiadores += limpiador
    }

    suspend fun borrarDatosLocales() {
        limpiadores.forEach { it() }
        imagenes.borrarCache()
    }
}

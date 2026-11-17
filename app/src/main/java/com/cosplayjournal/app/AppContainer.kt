package com.cosplayjournal.app

import android.content.Context
import com.cosplayjournal.app.data.auth.AuthRepository
import com.cosplayjournal.app.data.auth.SupabaseAuthRepository
import com.cosplayjournal.app.data.cache.ImagenesManager
import com.cosplayjournal.app.data.feedback.FeedbackRepository
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
    private val limpiadores = mutableListOf<suspend () -> Unit>({ settings.borrarTodo() })

    fun registrarLimpiador(limpiador: suspend () -> Unit) {
        limpiadores += limpiador
    }

    suspend fun borrarDatosLocales() {
        limpiadores.forEach { it() }
        imagenes.borrarCache()
    }
}

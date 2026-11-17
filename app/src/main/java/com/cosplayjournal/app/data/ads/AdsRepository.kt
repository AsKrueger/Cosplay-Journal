package com.cosplayjournal.app.data.ads

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Anuncio(
    val id: Long,
    val title: String,
    @SerialName("image_url") val imagen: String,
    @SerialName("target_url") val destino: String,
)

interface AdsRepository {
    /** Anuncios activos ahora mismo. La política RLS ya filtra por fechas y por `active`. */
    suspend fun activos(): List<Anuncio>
}

class SupabaseAdsRepository(private val client: SupabaseClient) : AdsRepository {
    override suspend fun activos() = client.from("ads").select().decodeList<Anuncio>()
}

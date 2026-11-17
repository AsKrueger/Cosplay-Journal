package com.cosplayjournal.app.data.profile

import com.cosplayjournal.app.data.llamada
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Perfil(
    val id: String,
    val nickname: String,
    @SerialName("avatar_path") val avatarPath: String? = null,
    @SerialName("friend_code") val codigoAmigo: String? = null,
)

interface ProfileRepository {
    suspend fun miPerfil(userId: String): Perfil
}

class SupabaseProfileRepository(private val client: SupabaseClient) : ProfileRepository {
    override suspend fun miPerfil(userId: String): Perfil = llamada {
        client.from("profiles").select { filter { eq("id", userId) } }.decodeSingle<Perfil>()
    }
}

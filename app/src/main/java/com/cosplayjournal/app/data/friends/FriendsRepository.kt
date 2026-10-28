package com.cosplayjournal.app.data.friends

import com.cosplayjournal.app.data.remote.FriendsApi
import com.cosplayjournal.app.data.remote.RemoteFriendRequest
import com.cosplayjournal.app.data.remote.RemoteFriendship
import com.cosplayjournal.app.data.remote.RemotePublicProfile
import com.cosplayjournal.app.data.remote.SendFriendBody
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import retrofit2.Response

/** Amigos y solicitudes. Los errores de la API llegan como HttpException con su código. */
class FriendsRepository(private val api: FriendsApi) {

    suspend fun friends(): List<RemotePublicProfile> = withContext(Dispatchers.IO) { api.friends() }

    suspend fun incomingRequests(): List<RemoteFriendRequest> = withContext(Dispatchers.IO) { api.incomingRequests() }

    suspend fun sendRequest(nickname: String): RemoteFriendship = withContext(Dispatchers.IO) {
        api.sendRequest(SendFriendBody(nickname.trim()))
    }

    suspend fun accept(id: String) = withContext(Dispatchers.IO) { ensureSuccess(api.accept(id)) }

    suspend fun reject(id: String) = withContext(Dispatchers.IO) { ensureSuccess(api.reject(id)) }

    suspend fun remove(nickname: String) = withContext(Dispatchers.IO) { ensureSuccess(api.remove(nickname)) }

    private fun ensureSuccess(response: Response<Unit>) {
        if (!response.isSuccessful) throw HttpException(response)
    }
}

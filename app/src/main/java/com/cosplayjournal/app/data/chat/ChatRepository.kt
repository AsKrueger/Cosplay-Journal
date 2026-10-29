package com.cosplayjournal.app.data.chat

import com.cosplayjournal.app.data.remote.ChatApi
import com.cosplayjournal.app.data.remote.CreateGroupBody
import com.cosplayjournal.app.data.remote.OpenPrivateBody
import com.cosplayjournal.app.data.remote.RemoteConversation
import com.cosplayjournal.app.data.remote.RemoteMessage
import com.cosplayjournal.app.data.remote.SendMessageBody
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/** Chats privados y grupos. Los errores de la API llegan como HttpException con su código. */
class ChatRepository(private val api: ChatApi) {

    suspend fun conversations(): List<RemoteConversation> = withContext(Dispatchers.IO) { api.conversations() }

    suspend fun openPrivate(nickname: String): RemoteConversation = withContext(Dispatchers.IO) {
        api.openPrivate(OpenPrivateBody(nickname.trim()))
    }

    suspend fun createGroup(name: String, nicknames: List<String>): RemoteConversation = withContext(Dispatchers.IO) {
        api.createGroup(CreateGroupBody(name.trim(), nicknames.map { it.trim() }.filter { it.isNotEmpty() }))
    }

    suspend fun messages(conversationId: String, since: String?): List<RemoteMessage> = withContext(Dispatchers.IO) {
        api.messages(conversationId, since)
    }

    suspend fun sendText(conversationId: String, text: String): RemoteMessage = withContext(Dispatchers.IO) {
        api.send(conversationId, SendMessageBody("TEXT", text.trim()))
    }

    suspend fun sendPhoto(conversationId: String, url: String): RemoteMessage = withContext(Dispatchers.IO) {
        api.send(conversationId, SendMessageBody("IMAGE", url.trim()))
    }
}

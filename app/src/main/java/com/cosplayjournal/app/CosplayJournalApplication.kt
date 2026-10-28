package com.cosplayjournal.app

import android.app.Application
import com.cosplayjournal.app.data.AppDatabase
import com.cosplayjournal.app.data.auth.AuthRepository
import com.cosplayjournal.app.data.auth.TokenStore
import com.cosplayjournal.app.data.friends.FriendsRepository
import com.cosplayjournal.app.data.remote.CosplayJournalApi
import com.cosplayjournal.app.data.repository.EventRepository
import com.cosplayjournal.app.data.repository.UserEventRepository

class CosplayJournalApplication : Application() {
    private val database by lazy { AppDatabase.getDatabase(this) }
    val tokenStore by lazy { TokenStore(this) }
    private val api by lazy { CosplayJournalApi.create(BuildConfig.API_BASE_URL) { tokenStore.token() } }
    val eventRepository by lazy { EventRepository(api) }
    val authRepository by lazy { AuthRepository(api, tokenStore) }
    val friendsRepository by lazy { FriendsRepository(api) }
    val userEventRepository by lazy { UserEventRepository(database.userEventDao()) }
}

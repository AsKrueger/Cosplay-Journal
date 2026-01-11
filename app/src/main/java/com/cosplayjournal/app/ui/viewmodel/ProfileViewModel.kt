package com.cosplayjournal.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.cosplayjournal.app.data.entity.Cosplay
import com.cosplayjournal.app.data.entity.Cosplan
import com.cosplayjournal.app.data.model.Event
import com.cosplayjournal.app.data.repository.CosplayRepository
import com.cosplayjournal.app.data.repository.EventRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class ProfileUiState(
    val totalCosplans: Int = 0,
    val totalEvents: Int = 0,
    val favoriteCosplays: List<Cosplay> = emptyList(),
    val favoriteEvents: List<Event> = emptyList(),
    val allCosplays: List<Cosplay> = emptyList(),
    val allCosplans: List<Cosplan> = emptyList()
)

class ProfileViewModel(
    private val cosplayRepository: CosplayRepository,
    private val eventRepository: EventRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    init {
        loadProfileData()
    }

    private fun loadProfileData() {
        viewModelScope.launch {
            combine(
                cosplayRepository.allCosplans,
                cosplayRepository.favoriteCosplays,
                cosplayRepository.allUserEventData,
                cosplayRepository.allCosplays,
                flow { emit(eventRepository.getEvents()) }
            ) { cosplans, favCosplays, userEventData, allCosplays, allEvents ->
                
                val favEventIds = userEventData.filter { it.isFavorite }.map { it.eventId }
                val favEvents = allEvents.filter { it.id in favEventIds }
                
                ProfileUiState(
                    totalCosplans = cosplans.size,
                    totalEvents = favEvents.size,
                    favoriteCosplays = favCosplays,
                    favoriteEvents = favEvents,
                    allCosplays = allCosplays,
                    allCosplans = cosplans
                )
            }.collect {
                _uiState.value = it
            }
        }
    }
}

class ProfileViewModelFactory(
    private val cosplayRepository: CosplayRepository,
    private val eventRepository: EventRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ProfileViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ProfileViewModel(cosplayRepository, eventRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

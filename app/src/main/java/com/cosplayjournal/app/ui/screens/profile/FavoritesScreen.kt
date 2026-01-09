package com.cosplayjournal.app.ui.screens.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.cosplayjournal.app.ui.components.EmptyState
import com.cosplayjournal.app.ui.screens.event.EventCard
import com.cosplayjournal.app.ui.viewmodel.ProfileViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FavoritesScreen(
    viewModel: ProfileViewModel,
    onEventClick: (String) -> Unit,
    onCosplayClick: (Long) -> Unit,
    onNavigateBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Favorites", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        val hasFavorites = uiState.favoriteEvents.isNotEmpty() || uiState.favoriteCosplays.isNotEmpty()

        if (!hasFavorites) {
            EmptyState(
                modifier = Modifier.padding(padding),
                icon = Icons.Default.Favorite,
                title = "No favorites yet",
                description = "Events and cosplays you mark with a heart will appear here."
            )
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .background(Color(0xFFF8F9FA)),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                if (uiState.favoriteEvents.isNotEmpty()) {
                    item {
                        Text("Favorite Events", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    }
                    items(uiState.favoriteEvents) { event ->
                        EventCard(
                            event = event,
                            status = "INTERESTED", // Or fetch real status
                            isFavorite = true,
                            onClick = { onEventClick(event.id) },
                            onFavoriteClick = { /* Toggle off logic if needed */ }
                        )
                    }
                }

                if (uiState.favoriteCosplays.isNotEmpty()) {
                    item {
                        Text("Favorite Cosplays", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    }
                    items(uiState.favoriteCosplays) { cosplay ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            onClick = { onCosplayClick(cosplay.id) }
                        ) {
                            Row(modifier = Modifier.padding(16.dp)) {
                                Text(cosplay.characterName, fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.weight(1f))
                                Text(cosplay.series, color = Color.Gray)
                            }
                        }
                    }
                }
            }
        }
    }
}

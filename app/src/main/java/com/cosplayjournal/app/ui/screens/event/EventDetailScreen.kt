package com.cosplayjournal.app.ui.screens.event

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.cosplayjournal.app.data.entity.Cosplan
import com.cosplayjournal.app.data.entity.EventCosplanSelection
import com.cosplayjournal.app.data.model.Event
import com.cosplayjournal.app.ui.viewmodel.CosplanViewModel
import com.cosplayjournal.app.ui.viewmodel.EventViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventDetailScreen(
    eventViewModel: EventViewModel,
    cosplanViewModel: CosplanViewModel,
    eventId: String,
    onNavigateBack: () -> Unit
) {
    val events by eventViewModel.events.collectAsState()
    val event = events.find { it.id == eventId }
    val userData by eventViewModel.userEventData.collectAsState()
    val eventUserData = userData.find { it.eventId == eventId }
    
    val cosplans by cosplanViewModel.allCosplans.collectAsState()
    val selections by eventViewModel.getSelectionsForEvent(eventId).collectAsState(initial = emptyList())

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(event?.name ?: "Event Detail") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { eventViewModel.toggleFavorite(eventId) }) {
                        Icon(
                            if (eventUserData?.isFavorite == true) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                            contentDescription = "Favorite",
                            tint = if (eventUserData?.isFavorite == true) Color.Red else Color.Gray
                        )
                    }
                    IconButton(onClick = { /* Share */ }) {
                        Icon(Icons.Default.Share, contentDescription = "Share")
                    }
                }
            )
        }
    ) { padding ->
        event?.let { e ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    EventHeader(e)
                }

                item {
                    StatusSelector(
                        currentStatus = eventUserData?.status ?: "NONE",
                        onStatusSelected = { eventViewModel.updateEventStatus(eventId, it) }
                    )
                }

                item {
                    Text("Daily Cosplans", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    Text("Select which cosplays you'll wear each day (max 3 per day).", style = MaterialTheme.typography.bodySmall)
                }

                // In a real app, parse startDate to endDate to get actual days
                val days = listOf("Day 1", "Day 2", "Day 3") 
                items(days) { day ->
                    DayCosplanSelector(
                        day = day,
                        availableCosplans = cosplans,
                        selectedSelections = selections.filter { it.day == day },
                        onCosplanToggle = { cosplanId, isSelected ->
                            if (isSelected) {
                                if (selections.count { it.day == day } < 3) {
                                    eventViewModel.selectCosplanForEvent(eventId, cosplanId, day)
                                }
                            } else {
                                // Logic to remove selection would be added here
                            }
                        }
                    )
                }
                
                item {
                    Button(
                        onClick = { /* Open website link */ },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00ACC1))
                    ) {
                        Text("Official Website")
                    }
                }
            }
        }
    }
}

@Composable
fun EventHeader(event: Event) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = event.venue, style = MaterialTheme.typography.titleMedium)
            Text(text = event.city, style = MaterialTheme.typography.bodyMedium)
            Text(text = "${event.startDate} - ${event.endDate}", color = Color(0xFF00ACC1), fontWeight = FontWeight.Bold)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatusSelector(currentStatus: String, onStatusSelected: (String) -> Unit) {
    val statuses = listOf("ATTENDING", "INTERESTED", "PLANNING")
    Column {
        Text("My Status", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            statuses.forEach { status ->
                val isSelected = currentStatus == status
                FilterChip(
                    selected = isSelected,
                    onClick = { onStatusSelected(if (isSelected) "NONE" else status) },
                    label = { Text(status) }
                )
            }
        }
    }
}

@Composable
fun DayCosplanSelector(
    day: String,
    availableCosplans: List<Cosplan>,
    selectedSelections: List<EventCosplanSelection>,
    onCosplanToggle: (Long, Boolean) -> Unit
) {
    OutlinedCard(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(day, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))
            if (availableCosplans.isEmpty()) {
                Text("No cosplans available. Create one first!", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
            } else {
                availableCosplans.forEach { cosplan ->
                    val isSelected = selectedSelections.any { it.cosplanId == cosplan.id }
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth().clickable { onCosplanToggle(cosplan.id, !isSelected) }
                    ) {
                        Checkbox(checked = isSelected, onCheckedChange = { onCosplanToggle(cosplan.id, it) })
                        Text(cosplan.name)
                    }
                }
            }
        }
    }
}

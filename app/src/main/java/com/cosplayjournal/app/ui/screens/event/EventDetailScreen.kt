package com.cosplayjournal.app.ui.screens.event

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
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
    val context = LocalContext.current
    val events by eventViewModel.events.collectAsState()
    val event = events.find { it.id == eventId }
    val userData by eventViewModel.userEventData.collectAsState()
    val eventUserData = userData.find { it.eventId == eventId }
    
    val cosplans by cosplanViewModel.allCosplans.collectAsState()
    val selections by eventViewModel.getSelectionsForEvent(eventId).collectAsState(initial = emptyList())

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Detalle del Evento") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { eventViewModel.toggleFavorite(eventId) }) {
                        Icon(
                            if (eventUserData?.isFavorite == true) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                            contentDescription = "Favorite",
                            tint = if (eventUserData?.isFavorite == true) Color.Red else MaterialTheme.colorScheme.onSurfaceVariant
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
                    .background(MaterialTheme.colorScheme.background)
            ) {
                item {
                    if (e.image != null) {
                        AsyncImage(
                            model = e.image,
                            contentDescription = null,
                            modifier = Modifier.fillMaxWidth().height(250.dp),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Box(modifier = Modifier.fillMaxWidth().height(250.dp).background(MaterialTheme.colorScheme.surfaceVariant))
                    }
                }

                item {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(e.name, style = MaterialTheme.typography.headlineMedium, color = MaterialTheme.colorScheme.onBackground, fontWeight = FontWeight.Bold)
                        Text("Del ${e.startDate} al ${e.endDate}", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(e.venue, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onBackground)
                        
                        Spacer(modifier = Modifier.height(24.dp))
                        DetailSection("HORARIO", e.schedule)
                        DetailSection("PRECIO", e.price)
                        DetailSection("DIRECCIÓN", e.address)
                        
                        Spacer(modifier = Modifier.height(24.dp))
                        
                        OutlinedButton(
                            onClick = { 
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(e.website))
                                context.startActivity(intent)
                            },
                            modifier = Modifier.fillMaxWidth(),
                            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.primary)
                        ) {
                            Text("Visitar Página Web", color = MaterialTheme.colorScheme.primary)
                        }
                    }
                }

                item {
                    Column(modifier = Modifier.padding(16.dp).background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f), RoundedCornerShape(16.dp)).padding(16.dp)) {
                        StatusSelector(
                            currentStatus = eventUserData?.status ?: "NONE",
                            onStatusSelected = { eventViewModel.updateEventStatus(eventId, it) }
                        )
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("Mis Cosplans", style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold)
                        
                        val days = listOf("Día 1", "Día 2")
                        days.forEach { day ->
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
                                        // TODO: Remove selection
                                    }
                                }
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DetailSection(title: String, content: String) {
    if (content.isNotBlank()) {
        Column(modifier = Modifier.padding(vertical = 8.dp)) {
            Text(title, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
            Text(content, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onBackground)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatusSelector(currentStatus: String, onStatusSelected: (String) -> Unit) {
    val statuses = listOf("ATTENDING", "INTERESTED", "PLANNING")
    Column {
        Text("Mi Estado", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold)
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            statuses.forEach { status ->
                val isSelected = currentStatus == status
                FilterChip(
                    selected = isSelected,
                    onClick = { onStatusSelected(if (isSelected) "NONE" else status) },
                    label = { Text(status) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primary,
                        selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
                        labelColor = MaterialTheme.colorScheme.onSurfaceVariant
                    )
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
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(day, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
            Spacer(modifier = Modifier.height(8.dp))
            availableCosplans.forEach { cosplan ->
                val isSelected = selectedSelections.any { it.cosplanId == cosplan.id }
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth().clickable { onCosplanToggle(cosplan.id, !isSelected) }
                ) {
                    Checkbox(
                        checked = isSelected,
                        onCheckedChange = { onCosplanToggle(cosplan.id, it) },
                        colors = CheckboxDefaults.colors(checkedColor = MaterialTheme.colorScheme.primary)
                    )
                    Text(cosplan.name, color = MaterialTheme.colorScheme.onSurface)
                }
            }
        }
    }
}

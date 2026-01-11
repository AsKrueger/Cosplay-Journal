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
                    .background(Color(0xFF003366)) // Dark blue background as per design
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
                        // Placeholder image from your screenshot
                        Box(modifier = Modifier.fillMaxWidth().height(250.dp).background(Color.Gray))
                    }
                }

                item {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(e.name, style = MaterialTheme.typography.headlineMedium, color = Color.White, fontWeight = FontWeight.Bold)
                        Text("Del ${e.startDate} al ${e.endDate}", style = MaterialTheme.typography.bodyMedium, color = Color.LightGray)
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(e.venue, style = MaterialTheme.typography.bodyLarge, color = Color.White)
                        
                        Spacer(modifier = Modifier.height(24.dp))
                        DetailSection("HORARIO", e.schedule)
                        DetailSection("PRECIO", e.price)
                        DetailSection("DIRECCIÓN", e.address)
                        
                        Spacer(modifier = Modifier.height(24.dp))
                        
                        Button(
                            onClick = { 
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(e.website))
                                context.startActivity(intent)
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent, contentColor = Color.White),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color.White)
                        ) {
                            Text("Visitar Página Web")
                        }
                    }
                }

                item {
                    Column(modifier = Modifier.padding(16.dp).background(Color.White.copy(alpha = 0.1f), RoundedCornerShape(16.dp)).padding(16.dp)) {
                        StatusSelector(
                            currentStatus = eventUserData?.status ?: "NONE",
                            onStatusSelected = { eventViewModel.updateEventStatus(eventId, it) }
                        )
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("Mis Cosplans", style = MaterialTheme.typography.titleLarge, color = Color.White, fontWeight = FontWeight.Bold)
                        
                        // Parse logic for days would go here
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
            Text(title, style = MaterialTheme.typography.labelLarge, color = Color.LightGray, fontWeight = FontWeight.Bold)
            Text(content, style = MaterialTheme.typography.bodyLarge, color = Color.White)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatusSelector(currentStatus: String, onStatusSelected: (String) -> Unit) {
    val statuses = listOf("ATTENDING", "INTERESTED", "PLANNING")
    Column {
        Text("Mi Estado", style = MaterialTheme.typography.titleMedium, color = Color.White, fontWeight = FontWeight.Bold)
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            statuses.forEach { status ->
                val isSelected = currentStatus == status
                FilterChip(
                    selected = isSelected,
                    onClick = { onStatusSelected(if (isSelected) "NONE" else status) },
                    label = { Text(status) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = Color(0xFF00ACC1),
                        selectedLabelColor = Color.White,
                        labelColor = Color.LightGray
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
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.05f))
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(day, fontWeight = FontWeight.Bold, color = Color.White)
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
                        colors = CheckboxDefaults.colors(checkedColor = Color(0xFF00ACC1), uncheckedColor = Color.White)
                    )
                    Text(cosplan.name, color = Color.White)
                }
            }
        }
    }
}

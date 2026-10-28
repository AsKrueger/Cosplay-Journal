package com.cosplayjournal.app.ui.screens.event

import android.content.Intent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.cosplayjournal.app.ui.viewmodel.EventViewModel

// Estados de participación que puede elegir el usuario
private val participationOptions = listOf(
    "ATTENDING" to "Voy",
    "ATTENDED" to "Fui",
    "INTERESTED" to "Me interesa",
    "PLANNING" to "Planeando"
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventDetailScreen(
    eventViewModel: EventViewModel,
    eventId: String,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val events by eventViewModel.events.collectAsState()
    val event = events.find { it.id == eventId }
    val userData by eventViewModel.userEventData.collectAsState()
    val currentStatus = userData.find { it.eventId == eventId }?.status ?: "NONE"

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Evento") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                },
                actions = {
                    if (event != null) {
                        IconButton(onClick = {
                            val text = "${event.name}\n${event.startDate} - ${event.endDate}\n${event.city}"
                            val intent = Intent(Intent.ACTION_SEND).apply {
                                type = "text/plain"
                                putExtra(Intent.EXTRA_TEXT, text)
                            }
                            context.startActivity(Intent.createChooser(intent, "Compartir evento"))
                        }) {
                            Icon(Icons.Default.Share, contentDescription = "Compartir")
                        }
                    }
                }
            )
        }
    ) { padding ->
        if (event == null) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = androidx.compose.ui.Alignment.Center) {
                Text("Este evento ya no está en el calendario")
            }
            return@Scaffold
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(event.name, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            Text("${event.startDate} - ${event.endDate}", style = MaterialTheme.typography.bodyLarge)
            Text(listOf(event.venue, event.city).filter { it.isNotBlank() }.joinToString(" · "),
                style = MaterialTheme.typography.bodyMedium)

            Text("Mi plan", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                participationOptions.forEach { (status, label) ->
                    val selected = currentStatus == status
                    FilterChip(
                        selected = selected,
                        onClick = {
                            // Pulsar la opción activa la quita
                            eventViewModel.updateEventStatus(eventId, if (selected) "NONE" else status)
                        },
                        label = { Text(label) }
                    )
                }
            }
        }
    }
}

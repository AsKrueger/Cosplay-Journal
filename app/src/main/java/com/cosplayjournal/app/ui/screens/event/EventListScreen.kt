package com.cosplayjournal.app.ui.screens.event

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cosplayjournal.app.data.model.Event
import com.cosplayjournal.app.ui.viewmodel.EventViewModel
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.TextStyle
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventListScreen(viewModel: EventViewModel, onEventClick: (String) -> Unit) {
    val events by viewModel.events.collectAsState()
    val userEventData by viewModel.userEventData.collectAsState()

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Journal", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(start = 16.dp)) {
                        Box(modifier = Modifier.size(12.dp).background(Color(0xFF4DB6AC), CircleShape))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("OFFLINE", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                },
                actions = {
                    IconButton(onClick = { /* TODO */ }) {
                        Icon(Icons.Default.Search, contentDescription = "Search")
                    }
                }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize().background(MaterialTheme.colorScheme.background)) {
            CalendarSection()
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = "Upcoming Conventions",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 16.dp),
                color = MaterialTheme.colorScheme.onBackground
            )

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(events) { event ->
                    val data = userEventData.find { it.eventId == event.id }
                    EventCard(
                        event = event, 
                        status = data?.status ?: "PLANNING",
                        isFavorite = data?.isFavorite ?: false,
                        onClick = { onEventClick(event.id) },
                        onFavoriteClick = { viewModel.toggleFavorite(event.id) }
                    )
                }
            }
        }
    }
}

@Composable
fun CalendarSection() {
    val today = remember { LocalDate.now() }
    var currentMonth by remember { mutableStateOf(YearMonth.now()) }
    val maxMonth = remember { YearMonth.now().plusMonths(20) }
    val minMonth = remember { YearMonth.now() }

    Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = { if (currentMonth > minMonth) currentMonth = currentMonth.minusMonths(1) },
                enabled = currentMonth > minMonth
            ) { 
                Icon(Icons.Default.ChevronLeft, contentDescription = "Previous") 
            }
            
            Text(
                text = "${currentMonth.month.getDisplayName(TextStyle.FULL, Locale.getDefault())} ${currentMonth.year}",
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            
            IconButton(
                onClick = { if (currentMonth < maxMonth) currentMonth = currentMonth.plusMonths(1) },
                enabled = currentMonth < maxMonth
            ) { 
                Icon(Icons.Default.ChevronRight, contentDescription = "Next") 
            }
        }
        
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            listOf("S", "M", "T", "W", "T", "F", "S").forEach { day ->
                Text(day, modifier = Modifier.weight(1f), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center)
            }
        }
        
        val daysInMonth = currentMonth.lengthOfMonth()
        val firstDayOfMonth = currentMonth.atDay(1).dayOfWeek.value % 7 // Adjusted for Sunday start
        
        val days = mutableListOf<String>()
        repeat(firstDayOfMonth) { days.add("") }
        for (i in 1..daysInMonth) { days.add(i.toString()) }
        
        val weeks = days.chunked(7)
        
        weeks.forEach { week ->
            Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                week.forEach { day ->
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .aspectRatio(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        if (day.isNotEmpty()) {
                            val isToday = currentMonth.year == today.year && 
                                          currentMonth.month == today.month && 
                                          day.toInt() == today.dayOfMonth
                            
                            if (isToday) {
                                Box(
                                    modifier = Modifier.size(32.dp).background(MaterialTheme.colorScheme.primary, CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(day, color = MaterialTheme.colorScheme.onPrimary, fontWeight = FontWeight.Bold)
                                }
                            } else {
                                Text(day, color = MaterialTheme.colorScheme.onSurface)
                            }
                        }
                    }
                }
                if (week.size < 7) {
                    repeat(7 - week.size) { Spacer(modifier = Modifier.weight(1f)) }
                }
            }
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        Box(modifier = Modifier.width(40.dp).height(4.dp).background(MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(2.dp)).align(Alignment.CenterHorizontally))
    }
}

@Composable
fun EventCard(
    event: Event, 
    status: String, 
    isFavorite: Boolean,
    onClick: () -> Unit,
    onFavoriteClick: () -> Unit
) {
    val statusColor = when(status) {
        "ATTENDING" -> MaterialTheme.colorScheme.primaryContainer
        "INTERESTED" -> MaterialTheme.colorScheme.secondaryContainer
        else -> MaterialTheme.colorScheme.tertiaryContainer
    }
    val statusTextColor = when(status) {
        "ATTENDING" -> MaterialTheme.colorScheme.onPrimaryContainer
        "INTERESTED" -> MaterialTheme.colorScheme.onSecondaryContainer
        else -> MaterialTheme.colorScheme.onTertiaryContainer
    }

    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
            )
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Surface(
                    color = statusColor,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = status,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = statusTextColor,
                        fontWeight = FontWeight.Bold
                    )
                }
                Text(text = event.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                Text(text = event.city, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(
                    text = "${event.startDate} - ${event.endDate}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
            }
            
            Column(verticalArrangement = Arrangement.SpaceBetween, horizontalAlignment = Alignment.End) {
                IconButton(onClick = onFavoriteClick) {
                    Icon(
                        if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder, 
                        contentDescription = "Favorite", 
                        tint = if (isFavorite) Color.Red else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                IconButton(onClick = { /* Share */ }) {
                    Icon(Icons.Default.Share, contentDescription = "Share", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}

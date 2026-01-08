package com.cosplayjournal.app.ui.screens.event

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cosplayjournal.app.data.model.Event
import com.cosplayjournal.app.ui.viewmodel.EventViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventListScreen(viewModel: EventViewModel) {
    val events by viewModel.events.collectAsState()

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Journal", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(start = 16.dp)) {
                        Box(modifier = Modifier.size(12.dp).background(Color(0xFF4DB6AC), CircleShape))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("OFFLINE", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
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
        Column(modifier = Modifier.padding(padding).fillMaxSize()) {
            CalendarSection()
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = "Upcoming Conventions",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(events) { event ->
                    EventCard(event)
                }
            }
        }
    }
}

@Composable
fun CalendarSection() {
    Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { /* Prev */ }) { Text("<") }
            Text("July 2024", fontWeight = FontWeight.Bold)
            IconButton(onClick = { /* Next */ }) { Text(">") }
        }
        
        // Days of week header
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            listOf("S", "M", "T", "W", "T", "F", "S").forEach { day ->
                Text(day, modifier = Modifier.weight(1f), style = MaterialTheme.typography.labelSmall, color = Color.Gray, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
            }
        }
        
        // Mock days
        val days = listOf("", "1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12", "13", "14")
        val chunkedDays = days.chunked(7)
        
        chunkedDays.forEach { week ->
            Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                week.forEach { day ->
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .aspectRatio(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        if (day == "5") {
                            Box(modifier = Modifier.size(32.dp).background(Color(0xFF00ACC1), CircleShape), contentAlignment = Alignment.Center) {
                                Text(day, color = Color.White)
                            }
                        } else if (day == "1") {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(day)
                                Box(modifier = Modifier.size(4.dp).background(Color(0xFF00ACC1), CircleShape))
                            }
                        } else {
                            Text(day)
                        }
                    }
                }
                // Fill empty slots if week is not full
                repeat(7 - week.size) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        Box(modifier = Modifier.width(40.dp).height(4.dp).background(Color.LightGray, RoundedCornerShape(2.dp)).align(Alignment.CenterHorizontally))
    }
}

@Composable
fun EventCard(event: Event) {
    // Determine status based on ID for demo purposes
    val status = when(event.id) {
        "1" -> "ATTENDING"
        "2" -> "INTERESTED"
        else -> "PLANNING"
    }
    val statusColor = when(status) {
        "ATTENDING" -> Color(0xFFE0F2F1)
        "INTERESTED" -> Color(0xFFF5F5F5)
        else -> Color(0xFFFFF3E0)
    }
    val statusTextColor = when(status) {
        "ATTENDING" -> Color(0xFF00897B)
        "INTERESTED" -> Color(0xFF757575)
        else -> Color(0xFFFB8C00)
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            // Placeholder Image
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color.LightGray)
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
                Text(text = event.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text(text = event.city, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                Text(
                    text = "${event.startDate} - ${event.endDate}",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFF00ACC1),
                    fontWeight = FontWeight.Bold
                )
            }
            
            Column(verticalArrangement = Arrangement.SpaceBetween, horizontalAlignment = Alignment.End) {
                IconButton(onClick = { /* Fav */ }) {
                    Icon(Icons.Default.FavoriteBorder, contentDescription = "Favorite", tint = Color.LightGray)
                }
                IconButton(onClick = { /* Share */ }) {
                    Icon(Icons.Default.Share, contentDescription = "Share", tint = Color.LightGray)
                }
            }
        }
    }
}

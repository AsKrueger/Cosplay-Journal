package com.cosplayjournal.app.ui.screens.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.cosplayjournal.app.data.entity.Cosplay
import com.cosplayjournal.app.data.model.Event
import com.cosplayjournal.app.ui.viewmodel.EventViewModel
import com.cosplayjournal.app.ui.viewmodel.ProfileViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    profileViewModel: ProfileViewModel,
    eventViewModel: EventViewModel,
    onCosplayClick: (Long) -> Unit,
    onEventClick: (String) -> Unit,
    onAddCosplanClick: () -> Unit,
    onCalendarClick: () -> Unit
) {
    val uiState by profileViewModel.uiState.collectAsState()
    val events by eventViewModel.events.collectAsState()

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Column(horizontalAlignment = Alignment.Start, modifier = Modifier.fillMaxWidth().padding(start = 16.dp)) {
                        Text("Hello, Maker", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(modifier = Modifier.size(8.dp).background(Color(0xFF4DB6AC), CircleShape))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("OFFLINE SYNCED", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                        }
                    }
                },
                actions = {
                    IconButton(onClick = { /* Share */ }) {
                        Icon(Icons.Default.Share, contentDescription = "Share")
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(Color(0xFFF8F9FA)),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Active Projects Section
            item {
                SectionHeader(title = "Active Projects", actionText = "View All", onActionClick = { /* Navigate to Stash */ })
                Spacer(modifier = Modifier.height(16.dp))
                if (uiState.allCosplays.isEmpty()) {
                    Text("No active projects. Start one today!", color = Color.Gray, modifier = Modifier.padding(vertical = 16.dp))
                } else {
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        items(uiState.allCosplays.take(5)) { cosplay ->
                            ActiveProjectCard(cosplay, onClick = { onCosplayClick(cosplay.id) })
                        }
                    }
                }
            }

            // Quick Tools Section
            item {
                Text("Quick Tools", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(16.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    ToolButton(
                        icon = Icons.Default.AddCircle,
                        label = "NEW\nCOSPLAN",
                        containerColor = Color(0xFF00ACC1),
                        contentColor = Color.White,
                        onClick = onAddCosplanClick,
                        modifier = Modifier.weight(1f)
                    )
                    ToolButton(
                        icon = Icons.Default.PhotoCamera,
                        label = "ADD\nSHOOT",
                        containerColor = Color(0xFFF5F5F5),
                        contentColor = Color.Black,
                        onClick = { /* TODO */ },
                        modifier = Modifier.weight(1f)
                    )
                    ToolButton(
                        icon = Icons.Default.LocationOn,
                        label = "LOG\nSPOT",
                        containerColor = Color(0xFFF5F5F5),
                        contentColor = Color.Black,
                        onClick = { /* TODO */ },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // Upcoming Events Section
            item {
                SectionHeader(title = "Upcoming Events", actionText = "Calendar", onActionClick = onCalendarClick)
                Spacer(modifier = Modifier.height(16.dp))
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    events.take(2).forEach { event ->
                        EventListItem(event, onClick = { onEventClick(event.id) })
                    }
                }
            }
        }
    }
}

@Composable
fun SectionHeader(title: String, actionText: String, onActionClick: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        TextButton(onClick = onActionClick) {
            Text(text = actionText, color = Color(0xFF00ACC1))
        }
    }
}

@Composable
fun ActiveProjectCard(cosplay: Cosplay, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .width(200.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color.LightGray)
            ) {
                if (cosplay.mainImageUri != null) {
                    AsyncImage(
                        model = cosplay.mainImageUri,
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                }
                Surface(
                    modifier = Modifier.align(Alignment.BottomStart).padding(8.dp),
                    color = Color.Black.copy(alpha = 0.6f),
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text(
                        "IN PROGRESS",
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White,
                        fontSize = 8.sp
                    )
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(cosplay.series.uppercase(), color = Color(0xFF00ACC1), style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
            Text(cosplay.characterName, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("COMPLETION", style = MaterialTheme.typography.labelSmall, color = Color.Gray, modifier = Modifier.weight(1f))
                Text("65%", style = MaterialTheme.typography.labelSmall, color = Color(0xFF00ACC1), fontWeight = FontWeight.Bold)
            }
            LinearProgressIndicator(
                progress = 0.65f,
                modifier = Modifier.fillMaxWidth().height(4.dp).clip(CircleShape),
                color = Color(0xFF00ACC1),
                trackColor = Color(0xFFE0E0E0)
            )
        }
    }
}

@Composable
fun ToolButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    containerColor: Color,
    contentColor: Color,
    onClick: () -> Unit,
    modifier: Modifier
) {
    Surface(
        modifier = modifier.height(100.dp).clickable(onClick = onClick),
        shape = RoundedCornerShape(24.dp),
        color = containerColor
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(8.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (contentColor == Color.White) Color.White else Color(0xFF00ACC1)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = label,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = contentColor,
                lineHeight = 12.sp
            )
        }
    }
}

@Composable
fun EventListItem(event: Event, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F5F5))
    ) {
        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Surface(
                modifier = Modifier.size(50.dp),
                shape = RoundedCornerShape(12.dp),
                color = Color.White
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                    Text("JUL", fontSize = 10.sp, color = Color.Gray)
                    Text("01", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(event.name, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.width(8.dp))
                    Surface(color = Color(0xFFFFE0E0), shape = RoundedCornerShape(8.dp)) {
                        Text("12 DAYS LEFT", modifier = Modifier.padding(horizontal = 6.dp), fontSize = 8.sp, color = Color.Red, fontWeight = FontWeight.Bold)
                    }
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = null,
                        modifier = Modifier.size(12.dp),
                        tint = Color.Gray
                    )
                    Text(event.city, fontSize = 12.sp, color = Color.Gray)
                }
            }
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = Color.LightGray
            )
        }
    }
}

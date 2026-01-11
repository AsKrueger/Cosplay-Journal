package com.cosplayjournal.app.ui.screens.home

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.cosplayjournal.app.data.entity.Cosplay
import com.cosplayjournal.app.data.entity.Cosplan
import com.cosplayjournal.app.data.model.Event
import com.cosplayjournal.app.ui.viewmodel.EventViewModel
import com.cosplayjournal.app.ui.viewmodel.ProfileViewModel
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    profileViewModel: ProfileViewModel,
    eventViewModel: EventViewModel,
    onCosplayClick: (Long) -> Unit,
    onEventClick: (String) -> Unit,
    onAddCosplanClick: () -> Unit,
    onAddCosplayClick: () -> Unit,
    onAddSessionClick: () -> Unit,
    onCalendarClick: () -> Unit
) {
    val uiState by profileViewModel.uiState.collectAsState()
    val events by eventViewModel.events.collectAsState()

    val recentItems = remember(uiState.allCosplays, uiState.allCosplans) {
        val list = mutableListOf<Pair<Any, String>>()
        list.addAll(uiState.allCosplays.map { it to "cosplay" })
        list.addAll(uiState.allCosplans.map { it to "cosplan" })
        
        list.sortedByDescending { (item, _) ->
            when (item) {
                is Cosplay -> item.id
                is Cosplan -> item.id
                else -> 0L
            }
        }.take(10)
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Column(horizontalAlignment = Alignment.Start, modifier = Modifier.fillMaxWidth().padding(start = 16.dp)) {
                        Text("Hola Cosplayer!!", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
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
            item {
                SectionHeader(title = "Vistas recientes", actionText = "Ver todos", onActionClick = { /* Navigate to Stash */ })
                Spacer(modifier = Modifier.height(16.dp))
                if (recentItems.isEmpty()) {
                    Text("No hay nada reciente. ¡Empieza un proyecto hoy!", color = Color.Gray, modifier = Modifier.padding(vertical = 16.dp))
                } else {
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        contentPadding = PaddingValues(horizontal = 4.dp)
                    ) {
                        items(recentItems) { (item, type) ->
                            RecentItemCard(
                                item = item,
                                type = type,
                                onClick = {
                                    if (type == "cosplay") onCosplayClick((item as Cosplay).id)
                                }
                            )
                        }
                    }
                }
            }

            item {
                Text("Herramientas rápidas", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(16.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    ToolButton(
                        icon = Icons.Default.AddCircle,
                        label = "NUEVO\nCOSPLAN",
                        containerColor = Color(0xFF00ACC1),
                        contentColor = Color.White,
                        onClick = onAddCosplanClick,
                        modifier = Modifier.weight(1f)
                    )
                    ToolButton(
                        icon = Icons.Default.CheckCircle,
                        label = "NUEVO\nCOSPLAY",
                        containerColor = Color(0xFFF5F5F5),
                        contentColor = Color.Black,
                        onClick = onAddCosplayClick,
                        modifier = Modifier.weight(1f)
                    )
                    ToolButton(
                        icon = Icons.Default.PhotoCamera,
                        label = "AÑADIR\nSESIÓN",
                        containerColor = Color(0xFFF5F5F5),
                        contentColor = Color.Black,
                        onClick = onAddSessionClick,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            item {
                SectionHeader(title = "Próximos Eventos", actionText = "Calendario", onActionClick = onCalendarClick)
                Spacer(modifier = Modifier.height(16.dp))
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    events.take(3).forEach { event ->
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
fun RecentItemCard(item: Any, type: String, onClick: () -> Unit) {
    val isCosplan = type == "cosplan"
    val borderColor = if (isCosplan) Color(0xFFFFEBEE) else Color(0xFFE8F5E9)
    val accentColor = if (isCosplan) Color(0xFFFF5252) else Color(0xFF4CAF50)
    
    val name = when (item) {
        is Cosplay -> item.characterName
        is Cosplan -> item.name
        else -> ""
    }
    
    val imageUri = when (item) {
        is Cosplay -> item.mainImageUri
        else -> null
    }

    Card(
        modifier = Modifier
            .width(160.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(3.dp, borderColor)
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(110.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFFF5F5F5))
            ) {
                if (imageUri != null) {
                    AsyncImage(
                        model = imageUri,
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Icon(
                        imageVector = if (isCosplan) Icons.Default.Inventory2 else Icons.Default.CheckCircle,
                        contentDescription = null,
                        modifier = Modifier.align(Alignment.Center).size(32.dp),
                        tint = accentColor.copy(alpha = 0.3f)
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = name,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
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
    val date = remember(event.startDate) {
        try {
            LocalDate.parse(event.startDate)
        } catch (e: Exception) {
            LocalDate.now()
        }
    }
    
    val month = remember(date) { date.format(DateTimeFormatter.ofPattern("MMM", Locale.getDefault())).uppercase() }
    val day = remember(date) { date.format(DateTimeFormatter.ofPattern("dd")) }
    
    val daysUntil = remember(date) {
        ChronoUnit.DAYS.between(LocalDate.now(), date)
    }

    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Recuadro de fecha dinámico
            Surface(
                modifier = Modifier.size(52.dp),
                shape = RoundedCornerShape(12.dp),
                color = Color(0xFFF5F5F5)
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(month, fontSize = 10.sp, color = Color(0xFF00ACC1), fontWeight = FontWeight.Bold)
                    Text(day, fontSize = 18.sp, fontWeight = FontWeight.ExtraBold)
                }
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = event.name,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.bodyLarge,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false)
                    )
                    
                    if (daysUntil >= 0) {
                        Surface(
                            color = if (daysUntil <= 7) Color(0xFFFFEBEE) else Color(0xFFE0F7FA),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                text = if (daysUntil == 0L) "¡HOY!" else "FALTAN $daysUntil DÍAS",
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                fontSize = 8.sp,
                                color = if (daysUntil <= 7) Color.Red else Color(0xFF00838F),
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp),
                        tint = Color.Gray
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "${event.city}, ${event.venue}",
                        fontSize = 12.sp,
                        color = Color.Gray,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
            
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = Color.LightGray,
                modifier = Modifier.padding(start = 8.dp)
            )
        }
    }
}

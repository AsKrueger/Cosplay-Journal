package com.cosplayjournal.app.ui.screens.home

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.cosplayjournal.app.data.entity.Cosplay
import com.cosplayjournal.app.data.entity.Cosplan
import com.cosplayjournal.app.data.model.Event
import com.cosplayjournal.app.ui.theme.NeutralGray
import com.cosplayjournal.app.ui.theme.SecondaryBlue
import com.cosplayjournal.app.ui.viewmodel.EventViewModel
import com.cosplayjournal.app.ui.viewmodel.ProfileViewModel
import java.time.LocalDate
import java.time.format.DateTimeFormatter
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

    val upcomingEvents = remember(events) {
        val today = LocalDate.now()
        events.filter {
            try {
                val eventDate = LocalDate.parse(it.startDate)
                !eventDate.isBefore(today)
            } catch (e: Exception) {
                false
            }
        }.sortedBy { it.startDate }
    }

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
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            modifier = Modifier.size(40.dp),
                            shape = CircleShape,
                            border = BorderStroke(2.dp, MaterialTheme.colorScheme.outline),
                            color = MaterialTheme.colorScheme.surface
                        ) {
                            Icon(
                                Icons.Default.Person, 
                                contentDescription = null, 
                                modifier = Modifier.padding(4.dp),
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            "HOLA COSPLAYER!!",
                            style = MaterialTheme.typography.headlineSmall,
                            fontStyle = FontStyle.Italic,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { /* Notificaciones */ }) {
                        Icon(
                            Icons.Default.NotificationsNone, 
                            contentDescription = null, 
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(MaterialTheme.colorScheme.background),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "VISTAS RECIENTES",
                        style = MaterialTheme.typography.titleLarge,
                        fontStyle = FontStyle.Italic,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Surface(
                        color = MaterialTheme.colorScheme.primary,
                        shape = RoundedCornerShape(4.dp),
                        border = BorderStroke(2.dp, MaterialTheme.colorScheme.outline)
                    ) {
                        Text(
                            "LIVE",
                            color = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    items(recentItems) { (item, type) ->
                        RecentItemCard(
                            item = item,
                            type = type,
                            onClick = { if (type == "cosplay") onCosplayClick((item as Cosplay).id) }
                        )
                    }
                }
            }

            item {
                Text(
                    "HERRAMIENTAS RÁPIDAS",
                    style = MaterialTheme.typography.titleLarge,
                    fontStyle = FontStyle.Italic,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Spacer(modifier = Modifier.height(16.dp))
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    ToolButton(
                        icon = Icons.Default.EditNote,
                        label = "NUEVO COSPLAN",
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary,
                        onClick = onAddCosplanClick
                    )
                    ToolButton(
                        icon = Icons.Default.Checkroom,
                        label = "NUEVO COSPLAY",
                        containerColor = if (isSystemInDarkTheme()) Color(0xFF3A4A7A) else Color(0xFFB5C7F7),
                        contentColor = if (isSystemInDarkTheme()) Color.White else SecondaryBlue,
                        onClick = onAddCosplayClick
                    )
                    ToolButton(
                        icon = Icons.Default.PhotoCamera,
                        label = "AÑADIR SESIÓN",
                        containerColor = if (isSystemInDarkTheme()) Color(0xFF333333) else Color(0xFFE0E0E0),
                        contentColor = if (isSystemInDarkTheme()) Color.LightGray else NeutralGray,
                        onClick = onAddSessionClick
                    )
                }
            }

            item {
                Text(
                    "PRÓXIMOS EVENTOS",
                    style = MaterialTheme.typography.titleLarge,
                    fontStyle = FontStyle.Italic,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Spacer(modifier = Modifier.height(16.dp))
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    upcomingEvents.take(3).forEach { event ->
                        EventListItem(event, onClick = { onEventClick(event.id) })
                    }
                }
            }
        }
    }
}

@Composable
fun RecentItemCard(item: Any, type: String, onClick: () -> Unit) {
    val name = when (item) {
        is Cosplay -> item.characterName
        is Cosplan -> item.name
        else -> ""
    }
    val imageUri = (item as? Cosplay)?.mainImageUri

    Box(
        modifier = Modifier
            .width(150.dp)
            .height(200.dp)
            .border(2.dp, MaterialTheme.colorScheme.outline)
            .clickable(onClick = onClick)
    ) {
        if (imageUri != null) {
            AsyncImage(
                model = imageUri,
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        } else {
            Box(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.surfaceVariant))
        }
        
        Box(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .fillMaxWidth()
                .background(Color.Black.copy(alpha = 0.6f))
                .padding(8.dp)
        ) {
            Text(
                name.uppercase(),
                color = Color.White,
                style = MaterialTheme.typography.labelLarge,
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
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(60.dp)
            .shadow(4.dp, shape = RoundedCornerShape(4.dp))
            .background(containerColor, RoundedCornerShape(4.dp))
            .border(2.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(4.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, contentDescription = null, tint = contentColor)
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                label,
                color = contentColor,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.ExtraBold,
                fontStyle = FontStyle.Italic,
                modifier = Modifier.weight(1f)
            )
            Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null, tint = contentColor)
        }
    }
}

@Composable
fun EventListItem(event: Event, onClick: () -> Unit) {
    val date = try { LocalDate.parse(event.startDate) } catch (e: Exception) { LocalDate.now() }
    val month = date.format(DateTimeFormatter.ofPattern("MMM", Locale.getDefault())).uppercase()
    val day = date.format(DateTimeFormatter.ofPattern("dd"))

    val accentColor = MaterialTheme.colorScheme.primary

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(100.dp)
            .border(2.dp, MaterialTheme.colorScheme.outline)
            .background(MaterialTheme.colorScheme.surface)
            .clickable(onClick = onClick)
    ) {
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .width(80.dp)
                .background(accentColor),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(day, color = MaterialTheme.colorScheme.onPrimary, style = MaterialTheme.typography.headlineSmall)
                Text(month, color = MaterialTheme.colorScheme.onPrimary, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
            }
        }

        // Separador vertical brutalista
        Box(modifier = Modifier.fillMaxHeight().width(2.dp).background(MaterialTheme.colorScheme.outline))

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                event.name.uppercase(),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.ExtraBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                event.venue,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.LocationOn, 
                    contentDescription = null, 
                    modifier = Modifier.size(14.dp), 
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    event.city, 
                    style = MaterialTheme.typography.bodySmall, 
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

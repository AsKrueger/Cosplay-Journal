package com.cosplayjournal.app.ui.screens.cosplan

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.cosplayjournal.app.data.entity.Cosplan
import com.cosplayjournal.app.ui.viewmodel.CosplanViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CosplanListScreen(
    viewModel: CosplanViewModel,
    onCosplanClick: (Long) -> Unit,
    onAddCosplanClick: () -> Unit,
    onNavigateToHome: () -> Unit = {},
    onNavigateToSelection: () -> Unit = {},
    onNavigateToProfile: () -> Unit = {},
    onMenuClick: () -> Unit = {},
    onSettingsClick: () -> Unit = {},
    onNavigateBack: (() -> Unit)? = null
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedFilter by remember { mutableStateOf("All") }
    val filters = listOf("All", "Planned", "In Progress", "Paused", "Finished")

    val cosplans by viewModel.allCosplans.collectAsState()
    
    val filteredCosplans = cosplans.filter {
        it.name.contains(searchQuery, ignoreCase = true) || 
        it.description.contains(searchQuery, ignoreCase = true)
    }

    Scaffold(
        topBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
                    .padding(top = 8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 4.dp),
                    contentAlignment = Alignment.Center
                ) {
                    IconButton(
                        onClick = onMenuClick,
                        modifier = Modifier.align(Alignment.CenterStart)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Menu,
                            contentDescription = "Menú",
                            tint = Color(0xFF00ACC1),
                            modifier = Modifier.size(28.dp)
                        )
                    }

                    Text(
                        text = "Cosplans",
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp
                        )
                    )

                    IconButton(
                        onClick = onSettingsClick,
                        modifier = Modifier.align(Alignment.CenterEnd)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Ajustes",
                            modifier = Modifier.size(28.dp),
                            tint = Color.Gray
                        )
                    }
                }

                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    placeholder = { Text("Search projects...", color = Color.Gray) },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = Color.LightGray) },
                    trailingIcon = { Icon(Icons.Default.Tune, contentDescription = null, tint = Color.LightGray) },
                    shape = RoundedCornerShape(24.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Color(0xFFF9F9F9),
                        unfocusedContainerColor = Color(0xFFF9F9F9),
                        focusedBorderColor = Color.Transparent,
                        unfocusedBorderColor = Color.Transparent
                    ),
                    singleLine = true
                )

                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.padding(bottom = 12.dp)
                ) {
                    items(filters) { filter ->
                        FilterChip(
                            selected = selectedFilter == filter,
                            onClick = { selectedFilter = filter },
                            label = { Text(filter) },
                            enabled = true,
                            shape = RoundedCornerShape(12.dp),
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Color(0xFFFF8A80),
                                selectedLabelColor = Color.White
                            ),
                            border = FilterChipDefaults.filterChipBorder(
                                enabled = true,
                                selected = selectedFilter == filter,
                                borderColor = Color(0xFFEEEEEE),
                                selectedBorderColor = Color.Transparent
                            )
                        )
                    }
                }
            }
        },
        bottomBar = {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(70.dp),
                color = Color(0xFFE0F7FA).copy(alpha = 0.5f),
                tonalElevation = 0.dp
            ) {
                Row(
                    modifier = Modifier.fillMaxSize(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    IconButton(onClick = onNavigateToHome, modifier = Modifier.padding(horizontal = 20.dp)) {
                        Icon(Icons.Default.Home, contentDescription = "Home", tint = Color.Gray, modifier = Modifier.size(28.dp))
                    }
                    IconButton(onClick = onNavigateToSelection, modifier = Modifier.padding(horizontal = 20.dp)) {
                        Icon(Icons.Default.Checkroom, contentDescription = "Cosplays", tint = Color(0xFF00ACC1), modifier = Modifier.size(30.dp))
                    }
                    IconButton(onClick = onNavigateToProfile, modifier = Modifier.padding(horizontal = 20.dp)) {
                        Icon(Icons.Default.Person, contentDescription = "Profile", tint = Color.Gray, modifier = Modifier.size(28.dp))
                    }
                    Spacer(modifier = Modifier.width(60.dp)) 
                }
            }
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddCosplanClick,
                containerColor = Color(0xFF00ACC1),
                contentColor = Color.White,
                shape = CircleShape,
                modifier = Modifier
                    .size(60.dp)
                    .offset(y = 30.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Cosplan", modifier = Modifier.size(30.dp))
            }
        },
        floatingActionButtonPosition = FabPosition.End
    ) { padding ->
        if (filteredCosplans.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Text("No cosplans found.", color = Color.Gray)
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(bottom = 20.dp)
            ) {
                items(filteredCosplans) { cosplan ->
                    CosplanCard(
                        cosplan = cosplan,
                        onClick = { onCosplanClick(cosplan.id) }
                    )
                }
            }
        }
    }
}

@Composable
fun CosplanCard(cosplan: Cosplan, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 10.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .clip(RoundedCornerShape(28.dp))
                    .background(Color(0xFFF5F5F5))
            ) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Icon(
                        Icons.Default.Assignment, 
                        contentDescription = null, 
                        modifier = Modifier.size(64.dp),
                        tint = Color.LightGray
                    )
                }
                
                Surface(
                    color = when(cosplan.status) {
                        "Finished" -> Color(0xFF00BFA5)
                        "In Progress" -> Color(0xFFFF8A80)
                        "Paused" -> Color(0xFFFFA726)
                        else -> Color(0xFFBDBDBD)
                    },
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = cosplan.status.uppercase(),
                        color = Color.White,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)
                    )
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = cosplan.name,
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 22.sp
                        )
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.Tag,
                            contentDescription = null,
                            tint = Color.Gray,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = cosplan.tags,
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.Gray,
                            maxLines = 1
                        )
                    }
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        cosplan.difficulty.uppercase(),
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = Color.Gray,
                            fontWeight = FontWeight.Bold
                        )
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "$${cosplan.estimatedBudget}",
                        style = MaterialTheme.typography.titleMedium.copy(
                            color = Color(0xFF00ACC1),
                            fontWeight = FontWeight.Bold
                        )
                    )
                }
            }
        }
    }
}

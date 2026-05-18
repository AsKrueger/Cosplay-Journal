package com.cosplayjournal.app.ui.screens.cosplan

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.cosplayjournal.app.data.entity.Cosplan
import com.cosplayjournal.app.ui.viewmodel.CosplanViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CosplanDetailScreen(
    viewModel: CosplanViewModel,
    cosplanId: Long,
    onEditClick: (Long) -> Unit,
    onDeleteClick: () -> Unit,
    onNavigateBack: () -> Unit,
    onManageCosplaysClick: (Long) -> Unit
) {
    val cosplans by viewModel.allCosplans.collectAsState()
    val cosplan = cosplans.find { it.id == cosplanId }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(cosplan?.name ?: "Cosplan Detail", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { onEditClick(cosplanId) }) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit")
                    }
                    IconButton(onClick = onDeleteClick) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface,
                    navigationIconContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        }
    ) { padding ->
        cosplan?.let {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .background(MaterialTheme.colorScheme.background)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        InfoRow(label = "Status", value = it.status)
                        InfoRow(label = "Season", value = it.season)
                        InfoRow(label = "Difficulty", value = it.difficulty)
                        InfoRow(label = "Estimated Budget", value = "${it.estimatedBudget}€")
                    }
                }
                
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                
                Text(
                    text = "Description", 
                    style = MaterialTheme.typography.titleMedium, 
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = it.description.ifBlank { "No description provided." }, 
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onBackground
                )
                
                Text(
                    text = "Notes", 
                    style = MaterialTheme.typography.titleMedium, 
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = it.notes.ifBlank { "No notes added yet." }, 
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onBackground
                )

                Spacer(modifier = Modifier.weight(1f))
                
                Button(
                    onClick = { onManageCosplaysClick(it.id) },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    )
                ) {
                    Text("Manage Cosplays", fontWeight = FontWeight.Bold)
                }
            }
        } ?: run {
            Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background), contentAlignment = androidx.compose.ui.Alignment.Center) {
                CircularProgressIndicator()
            }
        }
    }
}

@Composable
fun InfoRow(label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(text = label, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(text = value, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Medium)
    }
}

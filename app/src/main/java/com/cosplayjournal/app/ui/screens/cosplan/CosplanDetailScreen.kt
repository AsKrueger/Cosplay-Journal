package com.cosplayjournal.app.ui.screens.cosplan

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
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
                title = { Text(cosplan?.name ?: "Cosplan Detail") },
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
                }
            )
        }
    ) { padding ->
        cosplan?.let {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                InfoRow(label = "Status", value = it.status)
                InfoRow(label = "Season", value = it.season)
                InfoRow(label = "Difficulty", value = it.difficulty)
                InfoRow(label = "Estimated Budget", value = "$${it.estimatedBudget}")
                
                Divider()
                
                Text(text = "Description", style = MaterialTheme.typography.titleMedium)
                Text(text = it.description, style = MaterialTheme.typography.bodyLarge)
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(text = "Notes", style = MaterialTheme.typography.titleMedium)
                Text(text = it.notes, style = MaterialTheme.typography.bodyLarge)

                Spacer(modifier = Modifier.weight(1f))
                
                Button(
                    onClick = { onManageCosplaysClick(it.id) },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Manage Cosplays")
                }
            }
        } ?: run {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = androidx.compose.ui.Alignment.Center) {
                CircularProgressIndicator()
            }
        }
    }
}

@Composable
fun InfoRow(label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(text = label, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
        Text(text = value, style = MaterialTheme.typography.bodyLarge)
    }
}

package com.cosplayjournal.app.ui.screens.cosplan

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.cosplayjournal.app.data.entity.Cosplan
import com.cosplayjournal.app.ui.viewmodel.CosplanViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CosplanListScreen(
    viewModel: CosplanViewModel,
    onCosplanClick: (Long) -> Unit,
    onAddCosplanClick: () -> Unit
) {
    val cosplans by viewModel.allCosplans.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("My Cosplans") })
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onAddCosplanClick) {
                Icon(Icons.Default.Add, contentDescription = "Add Cosplan")
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            items(cosplans) { cosplan ->
                CosplanItem(cosplan = cosplan, onClick = { onCosplanClick(cosplan.id) })
            }
        }
    }
}

@Composable
fun CosplanItem(cosplan: Cosplan, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = cosplan.name, style = MaterialTheme.typography.titleLarge)
            Text(text = "Status: ${cosplan.status}", style = MaterialTheme.typography.bodyMedium)
            Text(text = "Difficulty: ${cosplan.difficulty}", style = MaterialTheme.typography.bodySmall)
        }
    }
}

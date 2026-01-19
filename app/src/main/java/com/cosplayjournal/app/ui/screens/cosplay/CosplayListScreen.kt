package com.cosplayjournal.app.ui.screens.cosplay

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.cosplayjournal.app.data.entity.Cosplay
import com.cosplayjournal.app.ui.viewmodel.CosplayViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CosplayListScreen(
    viewModel: CosplayViewModel,
    cosplanId: Long? = null,
    onCosplayClick: (Long) -> Unit,
    onAddCosplayClick: (Long) -> Unit,
    onNavigateBack: (() -> Unit)? = null
) {
    // Si cosplanId es null, seteamos -1 para que el ViewModel cargue TODOS los cosplays
    LaunchedEffect(cosplanId) {
        viewModel.setCosplanId(cosplanId ?: -1L)
    }

    val cosplays by viewModel.cosplaysForPlan.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (cosplanId != null) "Cosplays del Plan" else "Todos mis Cosplays") },
                navigationIcon = {
                    if (onNavigateBack != null) {
                        IconButton(onClick = onNavigateBack) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
                        }
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { onAddCosplayClick(cosplanId ?: 0L) }) {
                Icon(Icons.Default.Add, contentDescription = "Añadir Cosplay")
            }
        }
    ) { padding ->
        if (cosplays.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = androidx.compose.ui.Alignment.Center) {
                Text("No hay cosplays registrados aún.")
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            ) {
                items(cosplays) { cosplay ->
                    CosplayItem(cosplay = cosplay, onClick = { onCosplayClick(cosplay.id) })
                }
            }
        }
    }
}

@Composable
fun CosplayItem(cosplay: Cosplay, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = cosplay.characterName, style = MaterialTheme.typography.titleLarge)
            Text(text = cosplay.series, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

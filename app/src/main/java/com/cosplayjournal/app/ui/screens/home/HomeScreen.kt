package com.cosplayjournal.app.ui.screens.home

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.cosplayjournal.app.ui.components.EmptyState
import com.cosplayjournal.app.ui.screens.cosplan.CosplanItem
import com.cosplayjournal.app.ui.viewmodel.CosplanViewModel
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.RocketLaunch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: CosplanViewModel,
    onCosplanClick: (Long) -> Unit
) {
    val cosplans by viewModel.allCosplans.collectAsState()
    val activeCosplans = cosplans.filter { it.status == "In Progress" }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(title = { Text("Activity Log", fontWeight = FontWeight.Bold) })
        }
    ) { padding ->
        if (activeCosplans.isEmpty()) {
            EmptyState(
                modifier = Modifier.padding(padding),
                icon = Icons.Default.RocketLaunch,
                title = "Welcome back!",
                description = "You don't have any active projects. Go to Stash to start one!"
            )
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    Text("Ongoing Projects", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))
                }
                items(activeCosplans) { cosplan ->
                    CosplanItem(cosplan = cosplan, onClick = { onCosplanClick(cosplan.id) })
                }
            }
        }
    }
}

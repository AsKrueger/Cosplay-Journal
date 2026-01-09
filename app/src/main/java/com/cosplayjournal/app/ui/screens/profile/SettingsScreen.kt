package com.cosplayjournal.app.ui.screens.profile

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ColorLens
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onNavigateBack: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Appearance Section
            Text(text = "Appearance", style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.primary)
            
            ListItem(
                headlineContent = { Text("Dark Mode") },
                supportingContent = { Text("Use system settings or force dark theme") },
                leadingContent = { Icon(Icons.Default.ColorLens, contentDescription = null) },
                trailingContent = {
                    Switch(checked = false, onCheckedChange = { /* TODO: Implement DataStore */ })
                }
            )

            Divider()

            // About Section
            Text(text = "About", style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.primary)

            ListItem(
                headlineContent = { Text("App Version") },
                supportingContent = { Text("1.0.0 (MVP)") },
                leadingContent = { Icon(Icons.Default.Info, contentDescription = null) }
            )

            ListItem(
                headlineContent = { Text("Developer") },
                supportingContent = { Text("LocalVault Development") }
            )

            Spacer(modifier = Modifier.weight(1f))

            Text(
                text = "Cosplay Journal is 100% offline-first. Your data never leaves your device.",
                style = MaterialTheme.typography.bodySmall,
                color = androidx.compose.ui.graphics.Color.Gray,
                modifier = Modifier.fillMaxWidth(),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        }
    }
}

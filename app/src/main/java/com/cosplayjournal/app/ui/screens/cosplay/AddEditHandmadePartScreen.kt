package com.cosplayjournal.app.ui.screens.cosplay

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.cosplayjournal.app.data.entity.HandmadePart
import com.cosplayjournal.app.ui.viewmodel.CosplayViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditHandmadePartScreen(
    viewModel: CosplayViewModel,
    cosplayId: Long,
    partId: Long? = null,
    onNavigateBack: () -> Unit
) {
    var name by remember { mutableStateOf("") }
    var processSteps by remember { mutableStateOf("") }
    var materials by remember { mutableStateOf("") }
    var estimatedCost by remember { mutableStateOf("") }

    // In a real app, load existing part data if partId is not null

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (partId == null) "New Handmade Part" else "Edit Part") },
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
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Part Name (e.g., Sword, Corset)") },
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = processSteps,
                onValueChange = { processSteps = it },
                label = { Text("Process Steps") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3
            )
            OutlinedTextField(
                value = materials,
                onValueChange = { materials = it },
                label = { Text("Materials Used") },
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = estimatedCost,
                onValueChange = { estimatedCost = it },
                label = { Text("Estimated Cost") },
                modifier = Modifier.fillMaxWidth()
            )

            Button(
                onClick = {
                    val part = HandmadePart(
                        id = partId ?: 0,
                        cosplayId = cosplayId,
                        name = name,
                        processSteps = processSteps,
                        materials = materials,
                        estimatedCost = estimatedCost.toDoubleOrNull() ?: 0.0,
                        isFinished = false
                    )
                    viewModel.insertHandmadePart(part)
                    onNavigateBack()
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = name.isNotBlank()
            ) {
                Text("Save Part")
            }
        }
    }
}

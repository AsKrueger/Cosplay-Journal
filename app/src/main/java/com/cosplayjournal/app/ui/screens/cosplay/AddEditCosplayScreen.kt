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
import com.cosplayjournal.app.data.entity.Cosplay
import com.cosplayjournal.app.ui.viewmodel.CosplayViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditCosplayScreen(
    viewModel: CosplayViewModel,
    cosplanId: Long,
    cosplayId: Long? = null,
    onNavigateBack: () -> Unit
) {
    var characterName by remember { mutableStateOf("") }
    var series by remember { mutableStateOf("") }
    var wigs by remember { mutableStateOf("") }
    var makeup by remember { mutableStateOf("") }
    var accessories by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }

    // In a real app, we'd load existing cosplay data here if cosplayId is not null
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (cosplayId == null) "New Cosplay" else "Edit Cosplay") },
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
                value = characterName,
                onValueChange = { characterName = it },
                label = { Text("Character Name") },
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = series,
                onValueChange = { series = it },
                label = { Text("Series/Game/Anime") },
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = wigs,
                onValueChange = { wigs = it },
                label = { Text("Wigs") },
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = makeup,
                onValueChange = { makeup = it },
                label = { Text("Makeup") },
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = accessories,
                onValueChange = { accessories = it },
                label = { Text("Accessories") },
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = notes,
                onValueChange = { notes = it },
                label = { Text("Notes") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3
            )

            Button(
                onClick = {
                    val cosplay = Cosplay(
                        id = cosplayId ?: 0,
                        cosplanId = cosplanId,
                        characterName = characterName,
                        series = series,
                        wigs = wigs,
                        makeup = makeup,
                        accessories = accessories,
                        notes = notes
                    )
                    viewModel.insertCosplay(cosplay)
                    onNavigateBack()
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = characterName.isNotBlank()
            ) {
                Text("Save Cosplay")
            }
        }
    }
}

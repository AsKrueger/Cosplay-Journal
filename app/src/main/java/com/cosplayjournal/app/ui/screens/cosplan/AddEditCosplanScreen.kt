package com.cosplayjournal.app.ui.screens.cosplan

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.cosplayjournal.app.data.entity.Cosplan
import com.cosplayjournal.app.ui.viewmodel.CosplanViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditCosplanScreen(
    viewModel: CosplanViewModel,
    cosplanId: Long? = null,
    onNavigateBack: () -> Unit
) {
    var name by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var status by remember { mutableStateOf("Planned") }
    var difficulty by remember { mutableStateOf("Easy") }
    var season by remember { mutableStateOf("") }
    var estimatedBudget by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }

    val cosplans by viewModel.allCosplans.collectAsState()

    LaunchedEffect(cosplanId) {
        if (cosplanId != null) {
            cosplans.find { it.id == cosplanId }?.let {
                name = it.name
                description = it.description
                status = it.status
                difficulty = it.difficulty
                season = it.season
                estimatedBudget = it.estimatedBudget.toString()
                notes = it.notes
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (cosplanId == null) "New Cosplan" else "Edit Cosplan") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(MaterialTheme.colorScheme.background)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            val textFieldColors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                focusedLabelColor = MaterialTheme.colorScheme.primary,
                unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                cursorColor = MaterialTheme.colorScheme.primary
            )

            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Name") },
                modifier = Modifier.fillMaxWidth(),
                colors = textFieldColors
            )
            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Description") },
                modifier = Modifier.fillMaxWidth(),
                colors = textFieldColors
            )
            
            OutlinedTextField(
                value = status,
                onValueChange = { status = it },
                label = { Text("Status") },
                modifier = Modifier.fillMaxWidth(),
                colors = textFieldColors
            )
            OutlinedTextField(
                value = difficulty,
                onValueChange = { difficulty = it },
                label = { Text("Difficulty") },
                modifier = Modifier.fillMaxWidth(),
                colors = textFieldColors
            )
            
            OutlinedTextField(
                value = season,
                onValueChange = { season = it },
                label = { Text("Season") },
                modifier = Modifier.fillMaxWidth(),
                colors = textFieldColors
            )
            OutlinedTextField(
                value = estimatedBudget,
                onValueChange = { estimatedBudget = it },
                label = { Text("Estimated Budget") },
                modifier = Modifier.fillMaxWidth(),
                colors = textFieldColors
            )
            OutlinedTextField(
                value = notes,
                onValueChange = { notes = it },
                label = { Text("Notes") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3,
                colors = textFieldColors
            )

            Spacer(modifier = Modifier.weight(1f))

            Button(
                onClick = {
                    val newCosplan = Cosplan(
                        id = cosplanId ?: 0,
                        name = name,
                        description = description,
                        status = status,
                        tags = "",
                        season = season,
                        difficulty = difficulty,
                        estimatedBudget = estimatedBudget.toDoubleOrNull() ?: 0.0,
                        realBudget = 0.0,
                        notes = notes
                    )
                    if (cosplanId == null) {
                        viewModel.insert(newCosplan)
                    } else {
                        viewModel.update(newCosplan)
                    }
                    onNavigateBack()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                enabled = name.isNotBlank(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Save Cosplan", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            }
        }
    }
}

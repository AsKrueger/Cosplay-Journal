package com.cosplayjournal.app.ui.screens.cosplay

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
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
    var imageUri by remember { mutableStateOf<Uri?>(null) }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { uri -> imageUri = uri }
    )

    // Load existing data if editing
    LaunchedEffect(cosplayId) {
        if (cosplayId != null) {
            viewModel.getCosplayById(cosplayId)?.let { cosplay ->
                characterName = cosplay.characterName
                series = cosplay.series
                wigs = cosplay.wigs
                makeup = cosplay.makeup
                accessories = cosplay.accessories
                notes = cosplay.notes
                imageUri = cosplay.mainImageUri?.let { Uri.parse(it) }
            }
        }
    }

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
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Image Picker
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color.LightGray)
                    .clickable {
                        launcher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                    },
                contentAlignment = Alignment.Center
            ) {
                if (imageUri != null) {
                    AsyncImage(
                        model = imageUri,
                        contentDescription = "Cosplay Image",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.AddAPhoto, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(48.dp))
                        Text("Add Character Image", color = Color.Gray)
                    }
                }
            }

            OutlinedTextField(
                value = characterName,
                onValueChange = { characterName = it },
                label = { Text("Character Name") },
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                series,
                onValueChange = { series = it },
                label = { Text("Series/Game/Anime") },
                modifier = Modifier.fillMaxWidth()
            )
            
            Divider()
            Text("Details", style = MaterialTheme.typography.titleSmall, color = Color.Gray)

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
                        notes = notes,
                        mainImageUri = imageUri?.toString()
                    )
                    viewModel.insertCosplay(cosplay)
                    onNavigateBack()
                },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                enabled = characterName.isNotBlank(),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text("Save Cosplay")
            }
        }
    }
}

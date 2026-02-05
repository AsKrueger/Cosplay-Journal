package com.cosplayjournal.app.ui.screens.cosplay

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.cosplayjournal.app.data.entity.HandmadePart
import com.cosplayjournal.app.data.entity.PartResource
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
    var price by remember { mutableStateOf("") }
    var timeSpent by remember { mutableStateOf("") }
    var processDescription by remember { mutableStateOf("") }
    var projectPercentage by remember { mutableFloatStateOf(0f) }
    var imageUris by remember { mutableStateOf<List<Uri>>(emptyList()) }

    // State for materials added during creation (before partId exists)
    val temporaryResources = remember { mutableStateListOf<PartResource>() }
    
    // Persistent resources from DB
    val persistentResources by if (partId != null) {
        viewModel.getResourcesForPart(partId).collectAsState(initial = emptyList())
    } else {
        remember { mutableStateOf(emptyList<PartResource>()) }
    }

    val allResources = temporaryResources + persistentResources

    var showAddResourceDialog by remember { mutableStateOf(false) }

    // Design Colors (Purple Theme)
    val primaryPurple = Color(0xFF6750A4)
    val lightPurpleBackground = Color(0xFFF7F2FA)
    val fieldBorderColor = Color(0xFFE7E0EC)
    val grayText = Color(0xFF79747E)

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { uri -> uri?.let { if (imageUris.size < 6) imageUris = imageUris + it } }
    )

    LaunchedEffect(partId) {
        if (partId != null) {
            // Load existing part data logic here if needed
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Add Handmade Part", fontWeight = FontWeight.Bold, fontSize = 20.sp) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    Button(
                        onClick = {
                            val part = HandmadePart(
                                id = partId ?: 0,
                                cosplayId = cosplayId,
                                name = name,
                                price = price.toDoubleOrNull() ?: 0.0,
                                timeSpent = timeSpent,
                                processDescription = processDescription,
                                projectPercentage = projectPercentage.toInt(),
                                imageUris = imageUris.joinToString(",") { it.toString() }
                            )
                            // Note: In a production app, we'd handle saving temporary resources here too
                            viewModel.insertHandmadePart(part)
                            onNavigateBack()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = primaryPurple),
                        shape = RoundedCornerShape(100.dp),
                        enabled = name.isNotBlank(),
                        modifier = Modifier.padding(end = 8.dp)
                    ) {
                        Text("Save", fontWeight = FontWeight.Bold)
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // PHOTOS SECTION
            Column {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("PHOTOS", style = MaterialTheme.typography.labelLarge, color = grayText, fontWeight = FontWeight.Bold)
                    Text("${imageUris.size} / 6", style = MaterialTheme.typography.labelSmall, color = grayText)
                }
                Spacer(modifier = Modifier.height(12.dp))
                Row(modifier = Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    // ADD PHOTO BUTTON
                    Box(
                        modifier = Modifier
                            .size(100.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color.White)
                            .border(1.dp, Color.LightGray, RoundedCornerShape(16.dp)) // Dashed border not native
                            .clickable { launcher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) },
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.AddAPhoto, contentDescription = null, tint = primaryPurple, modifier = Modifier.size(24.dp))
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("Add Photo", fontSize = 10.sp, color = primaryPurple)
                        }
                    }
                    
                    imageUris.forEach { uri ->
                        AsyncImage(
                            model = uri,
                            contentDescription = null,
                            modifier = Modifier.size(100.dp).clip(RoundedCornerShape(16.dp)),
                            contentScale = ContentScale.Crop
                        )
                    }
                    
                    // Placeholders for empty slots
                    repeat(maxOf(0, 5 - imageUris.size)) {
                        Box(modifier = Modifier.size(100.dp).clip(RoundedCornerShape(16.dp)).background(lightPurpleBackground))
                    }
                }
            }

            // PART NAME
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Part Name", color = grayText) },
                shape = RoundedCornerShape(4.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedContainerColor = lightPurpleBackground,
                    focusedContainerColor = lightPurpleBackground,
                    unfocusedBorderColor = fieldBorderColor,
                    focusedBorderColor = primaryPurple
                )
            )

            // PRICE & TIME
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = price,
                    onValueChange = { price = it },
                    modifier = Modifier.weight(1f),
                    placeholder = { Text("Estimated Cost ($)", color = grayText) },
                    shape = RoundedCornerShape(4.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedContainerColor = lightPurpleBackground,
                        focusedContainerColor = lightPurpleBackground,
                        unfocusedBorderColor = fieldBorderColor
                    )
                )
                OutlinedTextField(
                    value = timeSpent,
                    onValueChange = { timeSpent = it },
                    modifier = Modifier.weight(1f),
                    placeholder = { Text("Time Spent (hrs)", color = grayText) },
                    shape = RoundedCornerShape(4.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedContainerColor = lightPurpleBackground,
                        focusedContainerColor = lightPurpleBackground,
                        unfocusedBorderColor = fieldBorderColor
                    )
                )
            }

            // COMPLETION STATUS SLIDER
            Surface(
                color = lightPurpleBackground,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Completion Status", fontWeight = FontWeight.Bold, color = primaryPurple)
                        Text("${projectPercentage.toInt()}%", fontWeight = FontWeight.Bold, color = primaryPurple)
                    }
                    Slider(
                        value = projectPercentage,
                        onValueChange = { projectPercentage = it },
                        valueRange = 0f..100f,
                        colors = SliderDefaults.colors(thumbColor = primaryPurple, activeTrackColor = primaryPurple)
                    )
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("STARTED", fontSize = 10.sp, color = grayText)
                        Text("FINISHED", fontSize = 10.sp, color = grayText)
                    }
                }
            }

            // PROCESS DESCRIPTION
            Column {
                OutlinedTextField(
                    value = processDescription,
                    onValueChange = { processDescription = it },
                    modifier = Modifier.fillMaxWidth().height(120.dp),
                    placeholder = { Text("Process Description", color = grayText) },
                    shape = RoundedCornerShape(4.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedContainerColor = lightPurpleBackground,
                        focusedContainerColor = lightPurpleBackground,
                        unfocusedBorderColor = fieldBorderColor
                    )
                )
                Text("Notes about techniques, failures, or successes...", style = MaterialTheme.typography.bodySmall, color = grayText, modifier = Modifier.padding(top = 4.dp))
            }

            // RESOURCES USED
            Column {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text("RESOURCES USED", style = MaterialTheme.typography.labelLarge, color = grayText, fontWeight = FontWeight.Bold)
                    TextButton(onClick = { showAddResourceDialog = true }) {
                        Icon(Icons.Default.AddCircle, contentDescription = null, modifier = Modifier.size(20.dp), tint = primaryPurple)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Add Material", color = primaryPurple, fontWeight = FontWeight.Bold)
                    }
                }
                
                if (allResources.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(120.dp)
                            .border(1.dp, fieldBorderColor, RoundedCornerShape(16.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.ShoppingBasket, contentDescription = null, tint = Color.LightGray, modifier = Modifier.size(32.dp))
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("No more materials added yet", color = Color.LightGray, fontSize = 12.sp)
                        }
                    }
                } else {
                    allResources.forEach { resource ->
                        ResourceItem(resource, primaryPurple)
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
            }
            Spacer(modifier = Modifier.height(20.dp))
        }
    }

    if (showAddResourceDialog) {
        AddResourceDialog(
            onDismiss = { showAddResourceDialog = false },
            onSave = { resName, web, priceValue ->
                if (partId != null) {
                    viewModel.insertPartResource(PartResource(partId = partId, name = resName, webLink = web, price = priceValue))
                } else {
                    // Temporary addition for new parts
                    temporaryResources.add(PartResource(partId = 0, name = resName, webLink = web, price = priceValue))
                }
                showAddResourceDialog = false
            }
        )
    }
}

@Composable
fun ResourceItem(resource: PartResource, accentColor: Color) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        border = BorderStroke(1.dp, Color(0xFFE7E0EC)),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier.size(40.dp).background(Color(0xFFF7F2FA), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Link, contentDescription = null, modifier = Modifier.size(20.dp), tint = accentColor)
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(resource.name, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                if (resource.webLink.isNotBlank()) {
                    Text(resource.webLink, fontSize = 11.sp, color = Color.Gray)
                }
            }
            Text("$${resource.price}", fontWeight = FontWeight.Bold, fontSize = 14.sp, modifier = Modifier.padding(end = 4.dp))
        }
    }
}

@Composable
fun AddResourceDialog(onDismiss: () -> Unit, onSave: (String, String, Double) -> Unit) {
    var name by remember { mutableStateOf("") }
    var web by remember { mutableStateOf("") }
    var price by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Material") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Name") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = web, onValueChange = { web = it }, label = { Text("Web/Store") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = price, onValueChange = { price = it }, label = { Text("Price") }, modifier = Modifier.fillMaxWidth())
            }
        },
        confirmButton = {
            TextButton(onClick = { onSave(name, web, price.toDoubleOrNull() ?: 0.0) }) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

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
import kotlinx.coroutines.launch

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
    var imageUris by remember { mutableStateOf<List<Uri>>(emptyList()) }

    val scope = rememberCoroutineScope()

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

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { uri -> uri?.let { if (imageUris.size < 6) imageUris = imageUris + it } }
    )

    LaunchedEffect(partId) {
        if (partId != null) {
            viewModel.getHandmadePartById(partId)?.let { part ->
                name = part.name
                price = if (part.price > 0) part.price.toString() else ""
                timeSpent = part.timeSpent
                processDescription = part.processDescription
                imageUris = if (part.imageUris.isNotBlank()) {
                    part.imageUris.split(",").map { Uri.parse(it) }
                } else {
                    emptyList()
                }
            }
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = { Text(if (partId == null) "Add Handmade Part" else "Edit Handmade Part", fontWeight = FontWeight.Bold, fontSize = 20.sp) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    Button(
                        onClick = {
                            scope.launch {
                                val part = HandmadePart(
                                    id = partId ?: 0,
                                    cosplayId = cosplayId,
                                    name = name,
                                    price = price.toDoubleOrNull() ?: 0.0,
                                    timeSpent = timeSpent,
                                    processDescription = processDescription,
                                    imageUris = imageUris.joinToString(",") { it.toString() }
                                )
                                
                                if (partId == null) {
                                    val newId = viewModel.insertHandmadePartAndGetId(part)
                                    // Save temporary resources with the new part ID
                                    temporaryResources.forEach { res ->
                                        viewModel.insertPartResource(res.copy(partId = newId))
                                    }
                                } else {
                                    viewModel.updateHandmadePart(part)
                                }
                                onNavigateBack()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        ),
                        shape = RoundedCornerShape(100.dp),
                        enabled = name.isNotBlank(),
                        modifier = Modifier.padding(end = 8.dp)
                    ) {
                        Text("Save", fontWeight = FontWeight.Bold)
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
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            val fieldColors = OutlinedTextFieldDefaults.colors(
                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                focusedTextColor = MaterialTheme.colorScheme.onSurface,
                unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                focusedLabelColor = MaterialTheme.colorScheme.primary
            )

            // PHOTOS SECTION
            Column {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("PHOTOS", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.Bold)
                    Text("${imageUris.size} / 6", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Spacer(modifier = Modifier.height(12.dp))
                Row(modifier = Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    // ADD PHOTO BUTTON
                    Box(
                        modifier = Modifier
                            .size(100.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(MaterialTheme.colorScheme.surface)
                            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(16.dp))
                            .clickable { launcher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) },
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.AddAPhoto, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(24.dp))
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("Add Photo", fontSize = 10.sp, color = MaterialTheme.colorScheme.primary)
                        }
                    }
                    
                    imageUris.forEach { uri ->
                        Box {
                            AsyncImage(
                                model = uri,
                                contentDescription = null,
                                modifier = Modifier.size(100.dp).clip(RoundedCornerShape(16.dp)),
                                contentScale = ContentScale.Crop
                            )
                            IconButton(
                                onClick = { imageUris = imageUris - uri },
                                modifier = Modifier.size(24.dp).align(Alignment.TopEnd).padding(4.dp).background(Color.Black.copy(alpha = 0.5f), CircleShape)
                            ) {
                                Icon(Icons.Default.Close, contentDescription = "Remove", tint = Color.White, modifier = Modifier.size(12.dp))
                            }
                        }
                    }
                }
            }

            // PART NAME
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Part Name (e.g. Helmet, Cape)", color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)) },
                shape = RoundedCornerShape(4.dp),
                colors = fieldColors
            )

            // PRICE & TIME
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = price,
                    onValueChange = { price = it },
                    modifier = Modifier.weight(1f),
                    placeholder = { Text("Estimated Cost (€)", color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)) },
                    shape = RoundedCornerShape(4.dp),
                    colors = fieldColors
                )
                OutlinedTextField(
                    value = timeSpent,
                    onValueChange = { timeSpent = it },
                    modifier = Modifier.weight(1f),
                    placeholder = { Text("Time Spent (hrs)", color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)) },
                    shape = RoundedCornerShape(4.dp),
                    colors = fieldColors
                )
            }

            // PROCESS DESCRIPTION
            Column {
                OutlinedTextField(
                    value = processDescription,
                    onValueChange = { processDescription = it },
                    modifier = Modifier.fillMaxWidth().height(120.dp),
                    placeholder = { Text("Process Description", color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)) },
                    shape = RoundedCornerShape(4.dp),
                    colors = fieldColors
                )
                Text("Notes about techniques, failures, or successes...", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(top = 4.dp))
            }

            // RESOURCES USED
            Column {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text("RESOURCES USED", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.Bold)
                    TextButton(onClick = { showAddResourceDialog = true }) {
                        Icon(Icons.Default.AddCircle, contentDescription = null, modifier = Modifier.size(20.dp), tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Add Material", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                    }
                }
                
                if (allResources.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(120.dp)
                            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(16.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.ShoppingBasket, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f), modifier = Modifier.size(32.dp))
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("No more materials added yet", color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f), fontSize = 12.sp)
                        }
                    }
                } else {
                    allResources.forEach { resource ->
                        ResourceItem(resource, MaterialTheme.colorScheme.primary)
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
            onSave = { resName, web, priceValue, images, usage ->
                val resource = PartResource(
                    partId = partId ?: 0,
                    name = resName,
                    webLink = web,
                    price = priceValue,
                    imageUris = images,
                    usageDescription = usage
                )
                if (partId != null) {
                    viewModel.insertPartResource(resource)
                } else {
                    temporaryResources.add(resource)
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
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (resource.imageUris.isNotBlank()) {
                    AsyncImage(
                        model = resource.imageUris.split(",").first(),
                        contentDescription = null,
                        modifier = Modifier.size(40.dp).clip(RoundedCornerShape(8.dp)),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Box(
                        modifier = Modifier.size(40.dp).background(MaterialTheme.colorScheme.surfaceVariant, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Link, contentDescription = null, modifier = Modifier.size(20.dp), tint = accentColor)
                    }
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(resource.name, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurface)
                    if (resource.webLink.isNotBlank()) {
                        Text(resource.webLink, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
                Text("${resource.price}€", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurface)
            }
            if (resource.usageDescription.isNotBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = resource.usageDescription,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 16.sp,
                    modifier = Modifier.padding(start = 52.dp)
                )
            }
        }
    }
}

@Composable
fun AddResourceDialog(onDismiss: () -> Unit, onSave: (String, String, Double, String, String) -> Unit) {
    var name by remember { mutableStateOf("") }
    var web by remember { mutableStateOf("") }
    var price by remember { mutableStateOf("") }
    var usage by remember { mutableStateOf("") }
    var imageUris by remember { mutableStateOf<List<Uri>>(emptyList()) }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { uri -> uri?.let { if (imageUris.size < 3) imageUris = imageUris + it } }
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface,
        title = { Text("Add Material", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface) },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Photo section for material
                Row(modifier = Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Box(
                        modifier = Modifier
                            .size(60.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .clickable { launcher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.AddAPhoto, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                    }
                    imageUris.forEach { uri ->
                        Box {
                            AsyncImage(
                                model = uri,
                                contentDescription = null,
                                modifier = Modifier.size(60.dp).clip(RoundedCornerShape(8.dp)),
                                contentScale = ContentScale.Crop
                            )
                            IconButton(
                                onClick = { imageUris = imageUris - uri },
                                modifier = Modifier.size(16.dp).align(Alignment.TopEnd).background(Color.Black.copy(alpha = 0.5f), CircleShape)
                            ) {
                                Icon(Icons.Default.Close, contentDescription = null, tint = Color.White, modifier = Modifier.size(10.dp))
                            }
                        }
                    }
                }

                val fieldColors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                    focusedTextColor = MaterialTheme.colorScheme.onSurface,
                    unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                    focusedLabelColor = MaterialTheme.colorScheme.primary,
                    unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant
                )

                OutlinedTextField(
                    value = name, 
                    onValueChange = { name = it }, 
                    label = { Text("Material Name") }, 
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    colors = fieldColors
                )
                OutlinedTextField(
                    value = web, 
                    onValueChange = { web = it }, 
                    label = { Text("Web Link or Store") }, 
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    colors = fieldColors
                )
                OutlinedTextField(
                    value = price, 
                    onValueChange = { price = it }, 
                    label = { Text("Price (Approx €)") }, 
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    colors = fieldColors
                )
                OutlinedTextField(
                    value = usage, 
                    onValueChange = { usage = it }, 
                    label = { Text("How was it used?") }, 
                    modifier = Modifier.fillMaxWidth().height(100.dp),
                    shape = RoundedCornerShape(8.dp),
                    placeholder = { Text("Describe techniques, amount used, etc.", fontSize = 12.sp) },
                    colors = fieldColors
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onSave(name, web, price.toDoubleOrNull() ?: 0.0, imageUris.joinToString(",") { it.toString() }, usage) },
                enabled = name.isNotBlank(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                )
            ) {
                Text("Add Material")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    )
}

package com.cosplayjournal.app.ui.screens.cosplay

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
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
import com.cosplayjournal.app.data.entity.Cosplay
import com.cosplayjournal.app.ui.viewmodel.CosplayViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditCosplayScreen(
    viewModel: CosplayViewModel,
    cosplanId: Long?,
    cosplayId: Long? = null,
    onAddHandmadePart: (Long) -> Unit,
    onAddPurchasedItem: (Long) -> Unit,
    onAddWig: (Long) -> Unit,
    onAddMakeup: (Long) -> Unit,
    onNavigateBack: () -> Unit
) {
    var currentCosplayId by remember { mutableStateOf(cosplayId) }
    
    var characterName by remember { mutableStateOf("") }
    var series by remember { mutableStateOf("") }
    var preferredWeather by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    var imageUri by remember { mutableStateOf<Uri?>(null) }
    
    val scope = rememberCoroutineScope()
    
    var showWeatherDialog by remember { mutableStateOf(false) }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { uri -> imageUri = uri }
    )

    LaunchedEffect(currentCosplayId) {
        if (currentCosplayId != null) {
            viewModel.getCosplayById(currentCosplayId!!)?.let { cosplay ->
                characterName = cosplay.characterName
                series = cosplay.series
                preferredWeather = cosplay.preferredWeather
                notes = cosplay.notes
                imageUri = cosplay.mainImageUri?.let { Uri.parse(it) }
            }
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            CenterAlignedTopAppBar(
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface,
                    navigationIconContentColor = MaterialTheme.colorScheme.primary,
                    actionIconContentColor = MaterialTheme.colorScheme.primary
                ),
                title = { Text(if (currentCosplayId == null) "New Cosplay" else "Edit Cosplay", fontWeight = FontWeight.Bold, fontSize = 18.sp) },
                navigationIcon = {
                    TextButton(onClick = onNavigateBack) {
                        Text("Cancel", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.SemiBold)
                    }
                },
                actions = {
                    TextButton(
                        onClick = {
                            scope.launch {
                                val cosplay = Cosplay(
                                    id = currentCosplayId ?: 0,
                                    cosplanId = cosplanId,
                                    characterName = characterName,
                                    series = series,
                                    preferredWeather = preferredWeather,
                                    notes = notes,
                                    mainImageUri = imageUri?.toString()
                                )
                                viewModel.insertCosplay(cosplay)
                                onNavigateBack()
                            }
                        },
                        enabled = characterName.isNotBlank()
                    ) {
                        Text(
                            "Save", 
                            color = if (characterName.isNotBlank()) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant, 
                            fontWeight = FontWeight.Bold
                        )
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
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            val fieldColors = OutlinedTextFieldDefaults.colors(
                unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                focusedContainerColor = MaterialTheme.colorScheme.surface,
                unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                focusedTextColor = MaterialTheme.colorScheme.onSurface,
                unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                focusedLabelColor = MaterialTheme.colorScheme.primary
            )

            Column {
                Text("CHARACTER NAME", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = characterName,
                    onValueChange = { characterName = it },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = fieldColors,
                    placeholder = { Text("Enter character name...", color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)) }
                )
            }

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("SERIES / GAME", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = series,
                        onValueChange = { series = it },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(24.dp),
                        colors = fieldColors
                    )
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text("PREFERRED WEATHER", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(24.dp))
                            .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(24.dp))
                            .clickable { showWeatherDialog = true }
                            .padding(horizontal = 16.dp),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                            Text(
                                text = preferredWeather.ifBlank { "Select weather..." },
                                color = if (preferredWeather.isBlank()) MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f) else MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.weight(1f),
                                maxLines = 1
                            )
                            Icon(Icons.Default.CloudQueue, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        }
                    }
                }
            }

            Column {
                Text("NOTES / DESCRIPTION", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    modifier = Modifier.fillMaxWidth().heightIn(min = 100.dp),
                    shape = RoundedCornerShape(24.dp),
                    colors = fieldColors
                )
            }

            Column {
                Text("COVER PHOTO", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(12.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    if (imageUri != null) {
                        AsyncImage(
                            model = imageUri,
                            contentDescription = null,
                            modifier = Modifier.size(100.dp).clip(RoundedCornerShape(16.dp)),
                            contentScale = ContentScale.Crop
                        )
                    }
                    Box(
                        modifier = Modifier
                            .size(100.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(MaterialTheme.colorScheme.surface)
                            .border(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
                            .clickable { launcher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) },
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.AddAPhoto, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            Text("ADD PHOTO", fontSize = 10.sp, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            Column {
                Text("COSPLAY ARTICLES", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(12.dp))
                
                var showAddMenu by remember { mutableStateOf(false) }
                
                Box {
                    Button(
                        onClick = { if (characterName.isNotBlank()) showAddMenu = true },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        ),
                        shape = RoundedCornerShape(20.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Add Article", fontWeight = FontWeight.Bold)
                    }
                    
                    DropdownMenu(
                        expanded = showAddMenu, 
                        onDismissRequest = { showAddMenu = false },
                        modifier = Modifier.background(MaterialTheme.colorScheme.surface)
                    ) {
                        DropdownMenuItem(
                            text = { Text("Peluca (Wig)", color = MaterialTheme.colorScheme.onSurface) },
                            onClick = { 
                                showAddMenu = false
                                scope.launch {
                                    val id = currentCosplayId ?: viewModel.insertCosplayAndGetId(
                                        Cosplay(cosplanId = cosplanId, characterName = characterName, series = series, preferredWeather = preferredWeather, notes = notes, mainImageUri = imageUri?.toString())
                                    )
                                    currentCosplayId = id
                                    onAddWig(id)
                                }
                            },
                            leadingIcon = { Icon(Icons.Default.Face, contentDescription = null, tint = MaterialTheme.colorScheme.primary) }
                        )
                        DropdownMenuItem(
                            text = { Text("Maquillaje (Makeup)", color = MaterialTheme.colorScheme.onSurface) },
                            onClick = { 
                                showAddMenu = false
                                scope.launch {
                                    val id = currentCosplayId ?: viewModel.insertCosplayAndGetId(
                                        Cosplay(cosplanId = cosplanId, characterName = characterName, series = series, preferredWeather = preferredWeather, notes = notes, mainImageUri = imageUri?.toString())
                                    )
                                    currentCosplayId = id
                                    onAddMakeup(id)
                                }
                            },
                            leadingIcon = { Icon(Icons.Default.AutoFixHigh, contentDescription = null, tint = MaterialTheme.colorScheme.primary) }
                        )
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                        DropdownMenuItem(
                            text = { Text("A Mano (Handmade)", color = MaterialTheme.colorScheme.onSurface) },
                            onClick = { 
                                showAddMenu = false
                                scope.launch {
                                    val id = currentCosplayId ?: viewModel.insertCosplayAndGetId(
                                        Cosplay(cosplanId = cosplanId, characterName = characterName, series = series, preferredWeather = preferredWeather, notes = notes, mainImageUri = imageUri?.toString())
                                    )
                                    currentCosplayId = id
                                    onAddHandmadePart(id)
                                }
                            },
                            leadingIcon = { Icon(Icons.Default.Handyman, contentDescription = null, tint = MaterialTheme.colorScheme.primary) }
                        )
                        DropdownMenuItem(
                            text = { Text("Comprado (Purchased)", color = MaterialTheme.colorScheme.onSurface) },
                            onClick = { 
                                showAddMenu = false
                                scope.launch {
                                    val id = currentCosplayId ?: viewModel.insertCosplayAndGetId(
                                        Cosplay(cosplanId = cosplanId, characterName = characterName, series = series, preferredWeather = preferredWeather, notes = notes, mainImageUri = imageUri?.toString())
                                    )
                                    currentCosplayId = id
                                    onAddPurchasedItem(id)
                                }
                            },
                            leadingIcon = { Icon(Icons.Default.ShoppingCart, contentDescription = null, tint = MaterialTheme.colorScheme.primary) }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Button(
                onClick = { 
                    scope.launch {
                        val cosplay = Cosplay(id = currentCosplayId ?: 0, cosplanId = cosplanId, characterName = characterName, series = series, preferredWeather = preferredWeather, notes = notes, mainImageUri = imageUri?.toString())
                        viewModel.insertCosplay(cosplay)
                        onNavigateBack()
                    }
                },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ),
                shape = RoundedCornerShape(28.dp),
                enabled = characterName.isNotBlank()
            ) {
                Text("Save Portfolio Item", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
        }
    }

    if (showWeatherDialog) {
        WeatherSelectionDialog(
            initialSelection = preferredWeather,
            onDismiss = { showWeatherDialog = false },
            onConfirm = { 
                preferredWeather = it
                showWeatherDialog = false
            }
        )
    }
}

@Composable
fun WeatherSelectionDialog(
    initialSelection: String,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    val seasons = listOf("Winter", "Spring", "Summer", "Autumn")
    var selectedSeasons by remember { 
        mutableStateOf(initialSelection.split(", ").filter { it in seasons }.toSet()) 
    }
    var customWeather by remember { 
        mutableStateOf(initialSelection.split(", ").filter { it !in seasons }.joinToString(", ")) 
    }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface,
        title = { Text("Select Preferred Weather", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                seasons.forEach { season ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                selectedSeasons = if (season in selectedSeasons) {
                                    selectedSeasons - season
                                } else {
                                    selectedSeasons + season
                                }
                            }
                            .padding(vertical = 4.dp)
                    ) {
                        Checkbox(
                            checked = season in selectedSeasons,
                            onCheckedChange = null,
                            colors = CheckboxDefaults.colors(checkedColor = MaterialTheme.colorScheme.primary)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(season, color = MaterialTheme.colorScheme.onSurface)
                    }
                }
                
                OutlinedTextField(
                    value = customWeather,
                    onValueChange = { customWeather = it },
                    label = { Text("Custom Weather (e.g. Rainy)") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                        focusedTextColor = MaterialTheme.colorScheme.onSurface,
                        unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                        focusedLabelColor = MaterialTheme.colorScheme.primary,
                        unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                )
            }
        },
        confirmButton = {
            TextButton(onClick = {
                val result = (selectedSeasons + customWeather.split(",").map { it.trim() }.filter { it.isNotBlank() })
                    .joinToString(", ")
                onConfirm(result)
            }) {
                Text("Confirm", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    )
}

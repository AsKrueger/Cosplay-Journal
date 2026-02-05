package com.cosplayjournal.app.ui.screens.cosplay

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
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
import com.cosplayjournal.app.data.entity.WigMakeup
import com.cosplayjournal.app.ui.viewmodel.CosplayViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditWigMakeupScreen(
    viewModel: CosplayViewModel,
    cosplayId: Long,
    itemId: Long? = null,
    isWig: Boolean = true,
    onNavigateBack: () -> Unit
) {
    var name by remember { mutableStateOf(if (isWig) "Wig" else "Makeup") }
    var price by remember { mutableStateOf("") }
    var timeSpent by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var productsUsed by remember { mutableStateOf("") }
    var imageUris by remember { mutableStateOf<List<Uri>>(emptyList()) }

    val scope = rememberCoroutineScope()

    // Design Colors (Pink/Purple Theme for Wig & Makeup)
    val accentColor = Color(0xFFE91E63)
    val lightBackground = Color(0xFFFDE7E9)
    val fieldBorderColor = Color(0xFFF8BBD0)
    val grayText = Color(0xFF79747E)

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { uri -> uri?.let { if (imageUris.size < 6) imageUris = imageUris + it } }
    )

    LaunchedEffect(itemId) {
        if (itemId != null) {
            viewModel.getWigMakeupById(itemId)?.let { item ->
                name = item.name
                price = if (item.price > 0) item.price.toString() else ""
                timeSpent = item.timeSpent
                description = item.description
                productsUsed = item.productsUsed
                imageUris = if (item.imageUris.isNotBlank()) {
                    item.imageUris.split(",").map { Uri.parse(it) }
                } else {
                    emptyList()
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (itemId == null) "Add ${if (isWig) "Wig" else "Makeup"}" else "Edit Details", fontWeight = FontWeight.Bold, fontSize = 20.sp) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    Button(
                        onClick = {
                            scope.launch {
                                val item = WigMakeup(
                                    id = itemId ?: 0,
                                    cosplayId = cosplayId,
                                    name = name,
                                    price = price.toDoubleOrNull() ?: 0.0,
                                    timeSpent = timeSpent,
                                    description = description,
                                    productsUsed = productsUsed,
                                    imageUris = imageUris.joinToString(",") { it.toString() }
                                )
                                
                                if (itemId == null) {
                                    viewModel.insertWigMakeup(item)
                                } else {
                                    viewModel.updateWigMakeup(item)
                                }
                                onNavigateBack()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = accentColor),
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
                    Box(
                        modifier = Modifier
                            .size(100.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color.White)
                            .border(1.dp, Color.LightGray, RoundedCornerShape(16.dp))
                            .clickable { launcher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) },
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.AddAPhoto, contentDescription = null, tint = accentColor, modifier = Modifier.size(24.dp))
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("Add Photo", fontSize = 10.sp, color = accentColor)
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

            // NAME
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Name / Reference", color = grayText) },
                shape = RoundedCornerShape(4.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedContainerColor = lightBackground,
                    focusedContainerColor = lightBackground,
                    unfocusedBorderColor = fieldBorderColor,
                    focusedBorderColor = accentColor
                )
            )

            // PRODUCTS USED
            OutlinedTextField(
                value = productsUsed,
                onValueChange = { productsUsed = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text(if (isWig) "Wig Brand / Model" else "Products Used", color = grayText) },
                shape = RoundedCornerShape(4.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedContainerColor = lightBackground,
                    focusedContainerColor = lightBackground,
                    unfocusedBorderColor = fieldBorderColor
                )
            )

            // PRICE & TIME
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = price,
                    onValueChange = { price = it },
                    modifier = Modifier.weight(1f),
                    label = { Text("Cost (€)", color = grayText) },
                    shape = RoundedCornerShape(4.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedContainerColor = lightBackground,
                        focusedContainerColor = lightBackground,
                        unfocusedBorderColor = fieldBorderColor
                    )
                )
                OutlinedTextField(
                    value = timeSpent,
                    onValueChange = { timeSpent = it },
                    modifier = Modifier.weight(1f),
                    label = { Text("Time Spent", color = grayText) },
                    shape = RoundedCornerShape(4.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedContainerColor = lightBackground,
                        focusedContainerColor = lightBackground,
                        unfocusedBorderColor = fieldBorderColor
                    )
                )
            }

            // DESCRIPTION
            Column {
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    modifier = Modifier.fillMaxWidth().height(150.dp),
                    label = { Text(if (isWig) "Styling Notes" else "Application Steps", color = grayText) },
                    shape = RoundedCornerShape(4.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedContainerColor = lightBackground,
                        focusedContainerColor = lightBackground,
                        unfocusedBorderColor = fieldBorderColor
                    )
                )
                Text("Techniques, contacts used, or styling steps...", style = MaterialTheme.typography.bodySmall, color = grayText, modifier = Modifier.padding(top = 4.dp))
            }
            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}

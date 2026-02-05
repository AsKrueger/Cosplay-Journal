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
import com.cosplayjournal.app.data.entity.PurchasedItem
import com.cosplayjournal.app.ui.viewmodel.CosplayViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditPurchasedItemScreen(
    viewModel: CosplayViewModel,
    cosplayId: Long,
    itemId: Long? = null,
    onNavigateBack: () -> Unit
) {
    var name by remember { mutableStateOf("") }
    var purchaseLink by remember { mutableStateOf("") }
    var adjustmentDescription by remember { mutableStateOf("") }
    var projectPercentage by remember { mutableFloatStateOf(0f) }
    var imageUris by remember { mutableStateOf<List<Uri>>(emptyList()) }

    // Colors
    val primaryPurple = Color(0xFF6750A4)
    val lightPurpleBackground = Color(0xFFF7F2FA)
    val fieldBorderColor = Color(0xFFE7E0EC)
    val grayText = Color(0xFF79747E)

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { uri -> uri?.let { imageUris = imageUris + it } }
    )

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(if (itemId == null) "New Purchased Item" else "Edit Purchased Item", fontWeight = FontWeight.Bold, fontSize = 18.sp) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    Button(
                        onClick = {
                            val item = PurchasedItem(
                                id = itemId ?: 0,
                                cosplayId = cosplayId,
                                name = name,
                                purchaseLink = purchaseLink,
                                adjustmentDescription = adjustmentDescription,
                                projectPercentage = projectPercentage.toInt(),
                                imageUris = imageUris.joinToString(",") { it.toString() }
                            )
                            viewModel.insertPurchasedItem(item)
                            onNavigateBack()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = primaryPurple),
                        shape = RoundedCornerShape(20.dp),
                        enabled = name.isNotBlank()
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
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // PHOTO REFERENCE
            Column {
                Text("Photo Reference", style = MaterialTheme.typography.labelLarge, color = grayText)
                Spacer(modifier = Modifier.height(12.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color.White)
                        .border(1.dp, Color.LightGray, RoundedCornerShape(16.dp)) // Simulating dashed border
                        .clickable { launcher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) },
                    contentAlignment = Alignment.Center
                ) {
                    if (imageUris.isNotEmpty()) {
                        AsyncImage(
                            model = imageUris.last(),
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.AddAPhoto, contentDescription = null, tint = primaryPurple, modifier = Modifier.size(32.dp))
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Upload or take photo", fontWeight = FontWeight.Bold)
                            Text("Add a picture of the item as it arrived", fontSize = 12.sp, color = grayText)
                        }
                    }
                }
            }

            // ITEM NAME
            Column {
                Text("Item Name", style = MaterialTheme.typography.labelLarge, color = grayText)
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("e.g., Red Dragon Gauntlet", color = Color.LightGray) },
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedContainerColor = Color.White,
                        focusedContainerColor = Color.White,
                        unfocusedBorderColor = Color.LightGray
                    )
                )
            }

            // PURCHASE LINK
            Column {
                Text("Purchase Link", style = MaterialTheme.typography.labelLarge, color = grayText)
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = purchaseLink,
                    onValueChange = { purchaseLink = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("https://shop.example.com/...", color = Color.LightGray) },
                    shape = RoundedCornerShape(12.dp),
                    leadingIcon = { Icon(Icons.Default.Link, contentDescription = null, tint = Color.LightGray) },
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedContainerColor = Color.White,
                        focusedContainerColor = Color.White,
                        unfocusedBorderColor = Color.LightGray
                    )
                )
            }

            // ADJUSTMENTS & NOTES
            Column {
                Text("Adjustments & Notes", style = MaterialTheme.typography.labelLarge, color = grayText)
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = adjustmentDescription,
                    onValueChange = { adjustmentDescription = it },
                    modifier = Modifier.fillMaxWidth().height(120.dp),
                    placeholder = { Text("What modifications are needed? (Weathering, resizing, repainting...)", color = Color.LightGray) },
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedContainerColor = Color.White,
                        focusedContainerColor = Color.White,
                        unfocusedBorderColor = Color.LightGray
                    )
                )
            }

            // PROJECT COMPLETION
            Surface(
                color = lightPurpleBackground,
                shape = RoundedCornerShape(24.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Project Completion", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                            Text("How much does this item contribute to the whole?", fontSize = 12.sp, color = grayText)
                        }
                        Text("${projectPercentage.toInt()}%", fontWeight = FontWeight.Bold, fontSize = 24.sp, color = primaryPurple)
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Slider(
                        value = projectPercentage,
                        onValueChange = { projectPercentage = it },
                        valueRange = 0f..100f,
                        colors = SliderDefaults.colors(thumbColor = primaryPurple, activeTrackColor = primaryPurple)
                    )
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("JUST STARTED", fontSize = 10.sp, color = grayText, fontWeight = FontWeight.Bold)
                        Text("COMPLETE", fontSize = 10.sp, color = grayText, fontWeight = FontWeight.Bold)
                    }
                }
            }

            // PRICING & TRACKING MOCKUP
            Surface(
                color = lightPurpleBackground,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.LocalOffer, contentDescription = null, tint = primaryPurple)
                    Spacer(modifier = Modifier.width(12.dp))
                    Text("Pricing & Tracking", fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                    Icon(Icons.Default.ChevronRight, contentDescription = null, tint = Color.LightGray)
                }
            }

            // ADD TO PROJECT BUTTON
            Button(
                onClick = {
                    val item = PurchasedItem(
                        id = itemId ?: 0,
                        cosplayId = cosplayId,
                        name = name,
                        purchaseLink = purchaseLink,
                        adjustmentDescription = adjustmentDescription,
                        projectPercentage = projectPercentage.toInt(),
                        imageUris = imageUris.joinToString(",") { it.toString() }
                    )
                    viewModel.insertPurchasedItem(item)
                    onNavigateBack()
                },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = primaryPurple),
                shape = RoundedCornerShape(28.dp),
                enabled = name.isNotBlank()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Add to Project", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            }
            
            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}

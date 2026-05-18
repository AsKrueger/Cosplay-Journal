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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.cosplayjournal.app.data.entity.PurchasedItem
import com.cosplayjournal.app.ui.viewmodel.CosplayViewModel
import kotlinx.coroutines.launch

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
    var price by remember { mutableStateOf("") }
    var imageUris by remember { mutableStateOf<List<Uri>>(emptyList()) }

    val scope = rememberCoroutineScope()

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { uri -> uri?.let { if (imageUris.size < 6) imageUris = imageUris + it } }
    )

    LaunchedEffect(itemId) {
        if (itemId != null) {
            viewModel.getPurchasedItemById(itemId)?.let { item ->
                name = item.name
                purchaseLink = item.purchaseLink
                adjustmentDescription = item.adjustmentDescription
                price = if (item.price > 0) item.price.toString() else ""
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
                title = { 
                    Text(
                        if (itemId == null) "NUEVO ARTÍCULO" else "EDITAR ARTÍCULO", 
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.ExtraBold,
                        fontStyle = FontStyle.Italic
                    ) 
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = MaterialTheme.colorScheme.onSurface)
                    }
                },
                actions = {
                    TextButton(
                        onClick = {
                            if (name.isNotBlank()) {
                                scope.launch {
                                    val item = PurchasedItem(
                                        id = itemId ?: 0,
                                        cosplayId = cosplayId,
                                        name = name,
                                        purchaseLink = purchaseLink,
                                        adjustmentDescription = adjustmentDescription,
                                        price = price.toDoubleOrNull() ?: 0.0,
                                        imageUris = imageUris.joinToString(",") { it.toString() }
                                    )
                                    if (itemId == null) {
                                        viewModel.insertPurchasedItem(item)
                                    } else {
                                        viewModel.updatePurchasedItem(item)
                                    }
                                    onNavigateBack()
                                }
                            }
                        }
                    ) {
                        Text(
                            "GUARDAR", 
                            fontWeight = FontWeight.ExtraBold, 
                            fontStyle = FontStyle.Italic,
                            color = if (name.isNotBlank()) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(MaterialTheme.colorScheme.background)
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // PHOTO REFERENCE
            Column {
                Text(
                    "FOTO DE REFERENCIA", 
                    style = MaterialTheme.typography.labelLarge, 
                    fontWeight = FontWeight.Bold,
                    fontStyle = FontStyle.Italic,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Spacer(modifier = Modifier.height(12.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .shadow(4.dp, RoundedCornerShape(4.dp))
                        .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(4.dp))
                        .border(2.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(4.dp))
                        .clickable { launcher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) },
                    contentAlignment = Alignment.Center
                ) {
                    if (imageUris.isNotEmpty()) {
                        AsyncImage(
                            model = imageUris.last(),
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(4.dp)),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.AddAPhoto, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(32.dp))
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                "SUBIR FOTO", 
                                fontWeight = FontWeight.ExtraBold, 
                                fontStyle = FontStyle.Italic,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }

            // ITEM NAME
            Column {
                Text(
                    "NOMBRE DEL ARTÍCULO", 
                    style = MaterialTheme.typography.labelLarge, 
                    fontWeight = FontWeight.Bold,
                    fontStyle = FontStyle.Italic,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Spacer(modifier = Modifier.height(8.dp))
                BrutalistTextField(
                    value = name,
                    onValueChange = { name = it },
                    placeholder = "Ej: Guantelete de Dragón Rojo"
                )
            }

            // PRICE
            Column {
                Text(
                    "COSTO (€)", 
                    style = MaterialTheme.typography.labelLarge, 
                    fontWeight = FontWeight.Bold,
                    fontStyle = FontStyle.Italic,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Spacer(modifier = Modifier.height(8.dp))
                BrutalistTextField(
                    value = price,
                    onValueChange = { price = it },
                    placeholder = "0.00"
                )
            }

            // PURCHASE LINK
            Column {
                Text(
                    "LINK DE COMPRA", 
                    style = MaterialTheme.typography.labelLarge, 
                    fontWeight = FontWeight.Bold,
                    fontStyle = FontStyle.Italic,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Spacer(modifier = Modifier.height(8.dp))
                BrutalistTextField(
                    value = purchaseLink,
                    onValueChange = { purchaseLink = it },
                    placeholder = "https://tienda.com/...",
                    leadingIcon = { Icon(Icons.Default.Link, contentDescription = null, tint = MaterialTheme.colorScheme.onSurface) }
                )
            }

            // ADJUSTMENTS & NOTES
            Column {
                Text(
                    "AJUSTES Y NOTAS", 
                    style = MaterialTheme.typography.labelLarge, 
                    fontWeight = FontWeight.Bold,
                    fontStyle = FontStyle.Italic,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Spacer(modifier = Modifier.height(8.dp))
                BrutalistTextField(
                    value = adjustmentDescription,
                    onValueChange = { adjustmentDescription = it },
                    placeholder = "¿Qué modificaciones necesita?",
                    modifier = Modifier.height(120.dp),
                    singleLine = false
                )
            }

            // SAVE BUTTON
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .shadow(if (name.isNotBlank()) 4.dp else 0.dp, RoundedCornerShape(4.dp))
                    .background(
                        if (name.isNotBlank()) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant, 
                        RoundedCornerShape(4.dp)
                    )
                    .border(2.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(4.dp))
                    .clickable(enabled = name.isNotBlank()) {
                        scope.launch {
                            val item = PurchasedItem(
                                id = itemId ?: 0,
                                cosplayId = cosplayId,
                                name = name,
                                purchaseLink = purchaseLink,
                                adjustmentDescription = adjustmentDescription,
                                price = price.toDoubleOrNull() ?: 0.0,
                                imageUris = imageUris.joinToString(",") { it.toString() }
                            )
                            if (itemId == null) {
                                viewModel.insertPurchasedItem(item)
                            } else {
                                viewModel.updatePurchasedItem(item)
                            }
                            onNavigateBack()
                        }
                    },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    if (itemId == null) "AÑADIR AL PROYECTO" else "ACTUALIZAR ARTÍCULO", 
                    fontWeight = FontWeight.ExtraBold, 
                    fontStyle = FontStyle.Italic,
                    color = if (name.isNotBlank()) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 16.sp
                )
            }
            
            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}

@Composable
fun BrutalistTextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier,
    leadingIcon: @Composable (() -> Unit)? = null,
    singleLine: Boolean = true
) {
    TextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier
            .fillMaxWidth()
            .border(2.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(4.dp)),
        placeholder = { Text(placeholder, color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodyMedium) },
        leadingIcon = leadingIcon,
        singleLine = singleLine,
        shape = RoundedCornerShape(4.dp),
        colors = TextFieldDefaults.colors(
            focusedContainerColor = MaterialTheme.colorScheme.surface,
            unfocusedContainerColor = MaterialTheme.colorScheme.surface,
            disabledContainerColor = MaterialTheme.colorScheme.surface,
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent,
            cursorColor = MaterialTheme.colorScheme.onSurface,
            focusedTextColor = MaterialTheme.colorScheme.onSurface,
            unfocusedTextColor = MaterialTheme.colorScheme.onSurface
        ),
        textStyle = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium)
    )
}

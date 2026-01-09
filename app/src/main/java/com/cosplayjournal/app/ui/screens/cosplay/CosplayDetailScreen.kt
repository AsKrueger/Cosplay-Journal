package com.cosplayjournal.app.ui.screens.cosplay

import android.content.Intent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import coil.compose.AsyncImage
import com.cosplayjournal.app.data.entity.Cosplay
import com.cosplayjournal.app.data.entity.HandmadePart
import com.cosplayjournal.app.data.entity.PurchasedItem
import com.cosplayjournal.app.ui.viewmodel.CosplayViewModel
import com.cosplayjournal.app.util.PdfExporter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CosplayDetailScreen(
    viewModel: CosplayViewModel,
    cosplayId: Long,
    onEditClick: (Long) -> Unit,
    onAddHandmadePart: (Long) -> Unit,
    onAddPurchasedItem: (Long) -> Unit,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    var cosplay by remember { mutableStateOf<Cosplay?>(null) }
    val handmadeParts by viewModel.getHandmadeParts(cosplayId).collectAsState(initial = emptyList())
    val purchasedItems by viewModel.getPurchasedItems(cosplayId).collectAsState(initial = emptyList())

    LaunchedEffect(cosplayId) {
        cosplay = viewModel.getCosplayById(cosplayId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(cosplay?.characterName ?: "Cosplay Progress") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = {
                        cosplay?.let { c ->
                            val file = PdfExporter.exportCosplayToPdf(context, c, handmadeParts, purchasedItems)
                            file?.let {
                                val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", it)
                                val intent = Intent(Intent.ACTION_SEND).apply {
                                    type = "application/pdf"
                                    putExtra(Intent.EXTRA_STREAM, uri)
                                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                }
                                context.startActivity(Intent.createChooser(intent, "Share Cosplay PDF"))
                            }
                        }
                    }) {
                        Icon(Icons.Default.PictureAsPdf, contentDescription = "Export PDF")
                    }
                    IconButton(onClick = { onEditClick(cosplayId) }) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit")
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                if (cosplay?.mainImageUri != null) {
                    AsyncImage(
                        model = cosplay?.mainImageUri,
                        contentDescription = "Cosplay Image",
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(250.dp),
                        contentScale = ContentScale.Crop
                    )
                }
            }

            item {
                Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                    CosplayHeaderInfo(cosplay)
                }
            }

            item {
                Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                    SectionHeader("Handmade Parts", onAddClick = { onAddHandmadePart(cosplayId) })
                }
            }
            items(handmadeParts) { part ->
                Box(modifier = Modifier.padding(horizontal = 16.dp)) {
                    HandmadePartItem(part = part, onCheckedChange = { isFinished ->
                        viewModel.updateHandmadePart(part.copy(isFinished = isFinished))
                    })
                }
            }

            item {
                Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                    SectionHeader("Purchased Items", onAddClick = { onAddPurchasedItem(cosplayId) })
                }
            }
            items(purchasedItems) { item ->
                Box(modifier = Modifier.padding(horizontal = 16.dp)) {
                    PurchasedItemRow(item = item, onCheckedChange = { isReceived ->
                        viewModel.updatePurchasedItem(item.copy(isReceived = isReceived))
                    })
                }
            }
        }
    }
}

@Composable
fun CosplayHeaderInfo(cosplay: Cosplay?) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = cosplay?.series ?: "", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(8.dp))
            if (!cosplay?.wigs.isNullOrBlank()) Text("Wigs: ${cosplay?.wigs}")
            if (!cosplay?.makeup.isNullOrBlank()) Text("Makeup: ${cosplay?.makeup}")
            if (!cosplay?.accessories.isNullOrBlank()) Text("Accessories: ${cosplay?.accessories}")
            if (!cosplay?.notes.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text("Notes: ${cosplay?.notes}", style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}

@Composable
fun SectionHeader(title: String, onAddClick: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = title, style = MaterialTheme.typography.headlineSmall)
        IconButton(onClick = onAddClick) {
            Icon(Icons.Default.Add, contentDescription = "Add")
        }
    }
}

@Composable
fun HandmadePartItem(part: HandmadePart, onCheckedChange: (Boolean) -> Unit) {
    OutlinedCard(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = part.name, style = MaterialTheme.typography.titleMedium)
                if (part.materials.isNotBlank()) {
                    Text(text = "Materials: ${part.materials}", style = MaterialTheme.typography.bodySmall)
                }
                if (part.estimatedCost > 0) {
                    Text(text = "Est. Cost: $${part.estimatedCost}", style = MaterialTheme.typography.bodySmall)
                }
            }
            Checkbox(checked = part.isFinished, onCheckedChange = onCheckedChange)
        }
    }
}

@Composable
fun PurchasedItemRow(item: PurchasedItem, onCheckedChange: (Boolean) -> Unit) {
    OutlinedCard(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = item.name, style = MaterialTheme.typography.titleMedium)
                Text(text = "Store: ${item.storeName} - $${item.price}", style = MaterialTheme.typography.bodySmall)
            }
            Checkbox(checked = item.isReceived, onCheckedChange = onCheckedChange)
        }
    }
}

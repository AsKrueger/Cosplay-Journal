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
    var storeName by remember { mutableStateOf("") }
    var purchaseLink by remember { mutableStateOf("") }
    var price by remember { mutableStateOf("") }

    // In a real app, load existing item data if itemId is not null

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (itemId == null) "New Purchased Item" else "Edit Item") },
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
                value = name,
                onValueChange = { name = it },
                label = { Text("Item Name (e.g., Wig, Boots)") },
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = storeName,
                onValueChange = { storeName = it },
                label = { Text("Store Name") },
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = purchaseLink,
                onValueChange = { purchaseLink = it },
                label = { Text("Purchase Link (URL)") },
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = price,
                onValueChange = { price = it },
                label = { Text("Price") },
                modifier = Modifier.fillMaxWidth()
            )

            Button(
                onClick = {
                    val item = PurchasedItem(
                        id = itemId ?: 0,
                        cosplayId = cosplayId,
                        name = name,
                        storeName = storeName,
                        purchaseLink = purchaseLink,
                        price = price.toDoubleOrNull() ?: 0.0,
                        isReceived = false
                    )
                    viewModel.insertPurchasedItem(item)
                    onNavigateBack()
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = name.isNotBlank()
            ) {
                Text("Save Item")
            }
        }
    }
}

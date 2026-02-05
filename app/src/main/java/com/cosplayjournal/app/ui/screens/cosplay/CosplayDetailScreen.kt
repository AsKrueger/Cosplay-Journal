package com.cosplayjournal.app.ui.screens.cosplay

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.cosplayjournal.app.data.entity.Cosplay
import com.cosplayjournal.app.data.entity.HandmadePart
import com.cosplayjournal.app.data.entity.PartResource
import com.cosplayjournal.app.data.entity.PurchasedItem
import com.cosplayjournal.app.ui.viewmodel.CosplayViewModel
import kotlinx.coroutines.launch

enum class DetailType {
    WIG_MAKEUP, HANDMADE, PURCHASED
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CosplayDetailScreen(
    cosplayId: Long,
    viewModel: CosplayViewModel,
    onNavigateBack: () -> Unit,
    onEditClick: (Long) -> Unit,
    onAddHandmadePart: (Long) -> Unit,
    onAddPurchasedItem: (Long) -> Unit,
    onAddWig: (Long) -> Unit,
    onAddMakeup: (Long) -> Unit,
    onEditWigMakeup: (Long, Long, Boolean) -> Unit
) {
    var cosplay by remember { mutableStateOf<Cosplay?>(null) }
    val handmadeParts by viewModel.getHandmadeParts(cosplayId).collectAsState(initial = emptyList())
    val purchasedItems by viewModel.getPurchasedItems(cosplayId).collectAsState(initial = emptyList())

    LaunchedEffect(cosplayId) {
        cosplay = viewModel.getCosplayById(cosplayId)
    }
    
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()
    var showSheet by remember { mutableStateOf(false) }
    var currentDetailType by remember { mutableStateOf<DetailType?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(cosplay?.characterName ?: "Details", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { onEditClick(cosplayId) }) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit")
                    }
                }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(Color(0xFFF8F9FA))
        ) {
            item {
                Box(modifier = Modifier.fillMaxWidth().height(220.dp)) {
                    AsyncImage(
                        model = cosplay?.mainImageUri,
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                    Surface(
                        modifier = Modifier.align(Alignment.BottomCenter).fillMaxWidth().padding(16.dp),
                        color = Color.Black.copy(alpha = 0.7f),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(cosplay?.series ?: "", color = Color.White.copy(alpha = 0.7f), fontSize = 11.sp)
                                Text(cosplay?.characterName ?: "", color = Color.White, fontWeight = FontWeight.ExtraBold, fontSize = 20.sp)
                            }
                            
                            Column(horizontalAlignment = Alignment.End) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.CloudQueue, contentDescription = null, tint = Color(0xFF03A9F4), modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("WEATHER", color = Color.White.copy(alpha = 0.7f), fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                }
                                Text(
                                    text = formatWeather(cosplay?.preferredWeather),
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp
                                )
                            }
                        }
                    }
                }
            }

            item {
                Surface(
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp).fillMaxWidth(),
                    color = Color.White,
                    shape = RoundedCornerShape(20.dp),
                    border = BorderStroke(1.dp, Color(0xFFF1F3F4))
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Text(
                            text = cosplay?.notes ?: "No notes yet.", 
                            fontSize = 14.sp, 
                            lineHeight = 20.sp,
                            color = if (cosplay?.notes.isNullOrBlank()) Color.LightGray else Color.Black
                        )
                    }
                }
            }

            item {
                Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)) {
                    Text("BUILD DETAILS", style = MaterialTheme.typography.labelLarge, color = Color.Gray, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(10.dp))
                    
                    val wigMakeupSubtitle = remember(cosplay?.wigs, cosplay?.makeup) {
                        val parts = listOfNotNull(
                            cosplay?.wigs?.takeIf { it.isNotBlank() },
                            cosplay?.makeup?.takeIf { it.isNotBlank() }
                        )
                        if (parts.isEmpty()) "Tap to add wig and makeup info" else parts.joinToString(", ")
                    }

                    DetailNavigationRow(
                        icon = Icons.Default.Face,
                        title = "Wig & Makeup",
                        subtitle = wigMakeupSubtitle,
                        onClick = { 
                            currentDetailType = DetailType.WIG_MAKEUP
                            showSheet = true
                        }
                    )
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        SquareDetailCard(
                            modifier = Modifier.weight(1f),
                            icon = Icons.Default.Handyman,
                            title = "Handmade",
                            subtitle = "${handmadeParts.size} Items",
                            iconColor = Color(0xFFF07D3E),
                            onClick = { 
                                currentDetailType = DetailType.HANDMADE
                                showSheet = true
                            }
                        )
                        SquareDetailCard(
                            modifier = Modifier.weight(1f),
                            icon = Icons.Default.ShoppingBag,
                            title = "Purchased",
                            subtitle = "${purchasedItems.size} Items",
                            iconColor = Color(0xFF34A853),
                            onClick = { 
                                currentDetailType = DetailType.PURCHASED
                                showSheet = true
                            }
                        )
                    }
                }
            }

            if (!cosplay?.recognition.isNullOrBlank()) {
                item {
                    Surface(
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp).fillMaxWidth(),
                        color = Color(0xFFFFF7E6),
                        shape = RoundedCornerShape(16.dp),
                        border = BorderStroke(1.dp, Color(0xFFFFD591))
                    ) {
                        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                            Box(modifier = Modifier.size(36.dp).background(Color(0xFFFFE58F), CircleShape), contentAlignment = Alignment.Center) {
                                Icon(Icons.Default.EmojiEvents, contentDescription = null, tint = Color(0xFFD48806), modifier = Modifier.size(20.dp))
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text("RECOGNITION", fontSize = 9.sp, color = Color(0xFFD48806), fontWeight = FontWeight.Bold)
                                Text(cosplay?.recognition ?: "", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            }
                        }
                    }
                }
            }
        }
    }

    if (showSheet) {
        ModalBottomSheet(
            onDismissRequest = { 
                showSheet = false 
                currentDetailType = null
            },
            sheetState = sheetState,
            containerColor = Color.White
        ) {
            ItemDetailPanel(
                type = currentDetailType,
                handmadeParts = handmadeParts,
                purchasedItems = purchasedItems,
                viewModel = viewModel,
                cosplay = cosplay,
                onAddHandmadePart = {
                    scope.launch { sheetState.hide() }.invokeOnCompletion {
                        showSheet = false
                        currentDetailType = null
                        onAddHandmadePart(cosplayId)
                    }
                },
                onAddPurchasedItem = {
                    scope.launch { sheetState.hide() }.invokeOnCompletion {
                        showSheet = false
                        currentDetailType = null
                        onAddPurchasedItem(cosplayId)
                    }
                },
                onEditCosplay = {
                    scope.launch { sheetState.hide() }.invokeOnCompletion {
                        showSheet = false
                        currentDetailType = null
                        onEditClick(cosplayId)
                    }
                },
                onClose = { 
                    scope.launch { sheetState.hide() }.invokeOnCompletion { 
                        showSheet = false 
                        currentDetailType = null
                    }
                }
            )
        }
    }
}

fun formatWeather(weather: String?): String {
    if (weather.isNullOrBlank()) return "Any"
    return weather.split(", ")
        .map { it.trim() }
        .joinToString(", ") {
            when (it) {
                "Winter" -> "❄️ Winter"
                "Spring" -> "🌸 Spring"
                "Summer" -> "☀️ Summer"
                "Autumn" -> "🍂 Autumn"
                else -> it
            }
        }
}

@Composable
fun ItemDetailPanel(
    type: DetailType?,
    handmadeParts: List<HandmadePart>,
    purchasedItems: List<PurchasedItem>,
    viewModel: CosplayViewModel,
    cosplay: Cosplay?,
    onAddHandmadePart: () -> Unit,
    onAddPurchasedItem: () -> Unit,
    onEditCosplay: () -> Unit,
    onClose: () -> Unit
) {
    val primaryPurple = Color(0xFF6750A4)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight(0.6f)
            .padding(horizontal = 20.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Row(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onClose) { Icon(Icons.Default.Close, contentDescription = "Close") }
            Text(
                text = when(type) {
                    DetailType.WIG_MAKEUP -> "Wig & Makeup"
                    DetailType.HANDMADE -> "Handmade"
                    DetailType.PURCHASED -> "Purchased"
                    null -> ""
                },
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
            IconButton(
                onClick = {
                    when(type) {
                        DetailType.HANDMADE -> onAddHandmadePart()
                        DetailType.PURCHASED -> onAddPurchasedItem()
                        else -> onEditCosplay()
                    }
                }
            ) { 
                Icon(Icons.Default.Edit, contentDescription = "Edit", tint = primaryPurple) 
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        when (type) {
            DetailType.WIG_MAKEUP -> {
                DetailSection("WIG DETAILS", cosplay?.wigs ?: "", Icons.Default.Face)
                Spacer(modifier = Modifier.height(16.dp))
                DetailSection("MAKEUP NOTES", cosplay?.makeup ?: "", Icons.Default.AutoFixHigh)
            }
            DetailType.HANDMADE -> {
                if (handmadeParts.isEmpty()) {
                    EmptyStatePanel("No handmade items added yet.")
                } else {
                    handmadeParts.forEach { part ->
                        HandmadePartDetailContent(part, viewModel)
                        Spacer(modifier = Modifier.height(20.dp))
                        HorizontalDivider(color = Color.LightGray.copy(alpha = 0.3f))
                        Spacer(modifier = Modifier.height(20.dp))
                    }
                }
            }
            DetailType.PURCHASED -> {
                if (purchasedItems.isEmpty()) {
                    EmptyStatePanel("No purchased items added yet.")
                } else {
                    purchasedItems.forEach { item ->
                        PurchasedItemDetailContent(item)
                        Spacer(modifier = Modifier.height(20.dp))
                        HorizontalDivider(color = Color.LightGray.copy(alpha = 0.3f))
                        Spacer(modifier = Modifier.height(20.dp))
                    }
                }
            }
            null -> {}
        }
        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
fun EmptyStatePanel(message: String) {
    Box(modifier = Modifier.fillMaxWidth().height(150.dp), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(Icons.Default.Inbox, contentDescription = null, modifier = Modifier.size(40.dp), tint = Color.LightGray)
            Spacer(modifier = Modifier.height(8.dp))
            Text(message, color = Color.Gray, fontSize = 14.sp)
        }
    }
}

@Composable
fun HandmadePartDetailContent(part: HandmadePart, viewModel: CosplayViewModel) {
    val resources by viewModel.getResourcesForPart(part.id).collectAsState(initial = emptyList())
    
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        if (part.imageUris.isNotBlank()) {
            AsyncImage(
                model = part.imageUris.split(",").first(),
                contentDescription = null,
                modifier = Modifier.fillMaxWidth().height(180.dp).clip(RoundedCornerShape(12.dp)),
                contentScale = ContentScale.Crop
            )
        }
        
        Surface(color = Color(0xFFFFF7E6), shape = RoundedCornerShape(4.dp)) {
            Text("HANDMADE", color = Color(0xFFD48806), fontSize = 9.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 4.dp))
        }
        
        Text(part.name, fontSize = 22.sp, fontWeight = FontWeight.ExtraBold)
        DetailSection("HOW IT WAS MADE", part.processDescription, Icons.Default.Handyman)
        
        Text("MATERIALS USED", style = MaterialTheme.typography.labelLarge, color = Color.Gray, fontWeight = FontWeight.Bold)
        Column {
            resources.chunked(2).forEach { rowResources ->
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    rowResources.forEach { res -> ResourceMiniCard(res, Modifier.weight(1f)) }
                    if (rowResources.size == 1) Spacer(Modifier.weight(1f))
                }
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
        
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            StatBox(Modifier.weight(1f), Icons.Default.AccessTime, "TIME SPENT", part.timeSpent)
            StatBox(Modifier.weight(1f), Icons.Default.Payments, "MATERIAL COST", "${part.price}€")
        }
    }
}

@Composable
fun PurchasedItemDetailContent(item: PurchasedItem) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        if (item.imageUris.isNotBlank()) {
            AsyncImage(
                model = item.imageUris.split(",").first(),
                contentDescription = null,
                modifier = Modifier.fillMaxWidth().height(180.dp).clip(RoundedCornerShape(12.dp)),
                contentScale = ContentScale.Crop
            )
        }
        Text(item.name, fontSize = 22.sp, fontWeight = FontWeight.ExtraBold)
        DetailSection("WHERE IT WAS BOUGHT", item.purchaseLink, Icons.Default.ShoppingCart)
        DetailSection("ADJUSTMENTS MADE", item.adjustmentDescription, Icons.Default.AutoFixNormal)
        StatBox(Modifier.fillMaxWidth(), Icons.Default.Payments, "COST", "${item.price}€")
    }
}

@Composable
fun ResourceMiniCard(resource: PartResource, modifier: Modifier) {
    Surface(
        modifier = modifier,
        color = Color.White,
        shape = RoundedCornerShape(8.dp),
        border = BorderStroke(1.dp, Color(0xFFF1F3F4))
    ) {
        Column(modifier = Modifier.padding(8.dp)) {
            Text(resource.name, fontSize = 10.sp, color = Color.Gray)
            Text(resource.webLink.take(if(resource.webLink.length > 15) 15 else resource.webLink.length) + "...", fontWeight = FontWeight.Bold, fontSize = 12.sp)
        }
    }
}

@Composable
fun StatBox(modifier: Modifier, icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, value: String) {
    Surface(modifier = modifier, color = Color(0xFFF7F2FA), shape = RoundedCornerShape(12.dp)) {
        Column(modifier = Modifier.padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(icon, contentDescription = null, tint = Color(0xFF6750A4), modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.height(6.dp))
            Text(label, fontSize = 8.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
            Text(value, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color(0xFF6750A4))
        }
    }
}

@Composable
fun DetailSection(label: String, content: String, icon: androidx.compose.ui.graphics.vector.ImageVector) {
    Column {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, contentDescription = null, tint = Color(0xFF6750A4), modifier = Modifier.size(14.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text(label, fontSize = 9.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
        }
        Spacer(modifier = Modifier.height(6.dp))
        Surface(color = Color(0xFFF8F9FA), shape = RoundedCornerShape(10.dp)) {
            Text(
                text = content.ifBlank { "No info provided." }, 
                modifier = Modifier.padding(12.dp).fillMaxWidth(), 
                fontSize = 12.sp, 
                lineHeight = 16.sp,
                color = if (content.isBlank()) Color.Gray else Color.Black
            )
        }
    }
}

@Composable
fun PortfolioStatCard(
    modifier: Modifier,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String,
    showArrow: Boolean = false,
    iconColor: Color,
    onClick: () -> Unit = {}
) {
    Card(
        modifier = modifier.clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        border = BorderStroke(1.dp, Color(0xFFF1F3F4)),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(icon, contentDescription = null, tint = iconColor, modifier = Modifier.size(14.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(label, fontSize = 9.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.height(6.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text(value, fontWeight = FontWeight.Bold, color = Color(0xFF1967D2), fontSize = 14.sp)
                if (showArrow) Icon(Icons.Default.ChevronRight, contentDescription = null, tint = Color.LightGray, modifier = Modifier.size(14.dp))
            }
        }
    }
}

@Composable
fun DetailNavigationRow(icon: androidx.compose.ui.graphics.vector.ImageVector, title: String, subtitle: String, onClick: () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        color = Color.White,
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, Color(0xFFF1F3F4))
    ) {
        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.size(38.dp).background(Color(0xFFFDE7E9), CircleShape), contentAlignment = Alignment.Center) {
                Icon(icon, contentDescription = null, tint = Color(0xFFE91E63), modifier = Modifier.size(20.dp))
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(title, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Text(
                    text = subtitle, 
                    fontSize = 11.sp, 
                    color = Color.Gray,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Icon(Icons.Default.ChevronRight, contentDescription = null, tint = Color.LightGray, modifier = Modifier.size(20.dp))
        }
    }
}

@Composable
fun SquareDetailCard(modifier: Modifier, icon: androidx.compose.ui.graphics.vector.ImageVector, title: String, subtitle: String, iconColor: Color, onClick: () -> Unit) {
    Surface(
        modifier = modifier.clickable(onClick = onClick),
        color = Color.White,
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, Color(0xFFF1F3F4))
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Box(modifier = Modifier.size(36.dp).background(iconColor.copy(alpha = 0.1f), CircleShape), contentAlignment = Alignment.Center) {
                Icon(icon, contentDescription = null, tint = iconColor, modifier = Modifier.size(20.dp))
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(title, fontWeight = FontWeight.Bold, fontSize = 14.sp)
            Text(subtitle, fontSize = 10.sp, color = Color.Gray)
            Spacer(modifier = Modifier.height(6.dp))
            Icon(Icons.Default.OpenInNew, contentDescription = null, tint = Color.LightGray, modifier = Modifier.size(12.dp).align(Alignment.End))
        }
    }
}

private fun Modifier.size(size: Int) = this.size(size.dp)

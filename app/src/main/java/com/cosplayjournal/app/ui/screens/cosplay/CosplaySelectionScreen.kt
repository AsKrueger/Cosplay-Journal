package com.cosplayjournal.app.ui.screens.cosplay

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import com.cosplayjournal.app.ui.viewmodel.CosplanViewModel
import com.cosplayjournal.app.ui.viewmodel.CosplayViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CosplaySelectionScreen(
    cosplayViewModel: CosplayViewModel,
    cosplanViewModel: CosplanViewModel,
    onNavigateToCosplays: () -> Unit,
    onNavigateToCosplans: () -> Unit,
    onAddCosplanClick: () -> Unit,
    onSettingsClick: () -> Unit,
    onMenuClick: () -> Unit = {}
) {
    val cosplays by cosplayViewModel.allCosplays.collectAsState()
    val cosplans by cosplanViewModel.allCosplans.collectAsState()

    val lastCosplay = cosplays.lastOrNull()
    val lastCosplan = cosplans.lastOrNull()

    // Buscamos la imagen del cosplay más reciente asociado al último plan si existe
    val lastCosplanImage = cosplays.findLast { it.cosplanId == lastCosplan?.id }?.mainImageUri

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                        Text(
                            "¿Qué deseas gestionar?", 
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 20.sp
                            )
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onMenuClick) {
                        Icon(
                            imageVector = Icons.Default.Menu,
                            contentDescription = "Menú",
                            tint = Color(0xFF00ACC1),
                            modifier = Modifier.size(28.dp)
                        )
                    }
                },
                actions = {
                    IconButton(onClick = onSettingsClick) {
                        Icon(
                            imageVector = Icons.Default.Settings, 
                            contentDescription = "Ajustes", 
                            modifier = Modifier.size(28.dp)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // CARD COSPLAYS
            SelectionCard(
                title = "Mis Cosplays",
                subtitle = lastCosplay?.let { "${it.characterName} - ${it.series}" } ?: "Empieza a registrar tus trajes",
                imageUri = lastCosplay?.mainImageUri,
                buttonText = "Ir a mis Cosplays",
                label = "FINALIZADO",
                labelColor = Color(0xFF00BFA5),
                onClick = onNavigateToCosplays,
                modifier = Modifier.weight(1f)
            )

            // CARD COSPLANS
            SelectionCard(
                title = "Mis Cosplans",
                subtitle = lastCosplan?.let { "${it.name}" } ?: "Planifica tu próximo proyecto",
                imageUri = lastCosplanImage,
                buttonText = "Ir a mis Cosplans",
                label = "EN PROGRESO",
                labelColor = Color(0xFFFF8A80),
                progress = 0.65f,
                showAddButton = true,
                onAddButtonClick = onAddCosplanClick,
                onClick = onNavigateToCosplans,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
fun SelectionCard(
    title: String,
    subtitle: String,
    imageUri: String?,
    buttonText: String,
    label: String,
    labelColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    progress: Float? = null,
    showAddButton: Boolean = false,
    onAddButtonClick: (() -> Unit)? = null
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(32.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(32.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Image(
                painter = rememberAsyncImagePainter(
                    model = imageUri ?: "https://images.unsplash.com/photo-1514328537558-630b978dba33?q=80&w=1000&auto=format&fit=crop"
                ),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.8f)),
                            startY = 400f
                        )
                    )
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                verticalArrangement = Arrangement.Bottom
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        color = labelColor,
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = label,
                            color = Color.White,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, fontSize = 10.sp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Text("Hace 2 semanas", color = Color.White.copy(alpha = 0.7f), fontSize = 12.sp)
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = title,
                    color = Color.White,
                    style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold, fontSize = 28.sp)
                )

                if (progress != null) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        LinearProgressIndicator(
                            progress = progress,
                            modifier = Modifier.weight(1f).height(4.dp).clip(CircleShape),
                            color = Color(0xFF00BFA5),
                            trackColor = Color.White.copy(alpha = 0.3f)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text("${(progress * 100).toInt()}%", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }

                Text(
                    text = subtitle,
                    color = Color.White.copy(alpha = 0.8f),
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 1
                )

                Spacer(modifier = Modifier.height(20.dp))

                Surface(
                    color = Color.White.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(20.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.2f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = buttonText, color = Color.White, fontWeight = FontWeight.Bold)
                        Icon(
                            imageVector = Icons.Default.ArrowForwardIos, 
                            contentDescription = null, 
                            tint = Color.White, 
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            if (showAddButton) {
                IconButton(
                    onClick = { onAddButtonClick?.invoke() },
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(16.dp)
                        .background(Color(0xFF00BFA5).copy(alpha = 0.9f), CircleShape)
                        .size(40.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Add, 
                        contentDescription = "Añadir", 
                        tint = Color.White
                    )
                }
            }
        }
    }
}

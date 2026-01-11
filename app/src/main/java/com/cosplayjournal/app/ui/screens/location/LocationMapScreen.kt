package com.cosplayjournal.app.ui.screens.location

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddLocation
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.cosplayjournal.app.data.entity.Location
import com.cosplayjournal.app.ui.viewmodel.LocationViewModel
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LocationMapScreen(
    viewModel: LocationViewModel,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val locations by viewModel.allLocations.collectAsState()
    
    val mapView = remember {
        MapView(context).apply {
            setTileSource(TileSourceFactory.MAPNIK)
            setMultiTouchControls(true)
            controller.setZoom(15.0)
            controller.setCenter(GeoPoint(40.4168, -3.7038))
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Photo Locations") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { 
                val center = mapView.mapCenter
                viewModel.insertLocation(
                    Location(
                        name = "New Location",
                        latitude = center.latitude,
                        longitude = center.longitude
                    )
                )
            }) {
                Icon(Icons.Default.AddLocation, contentDescription = "Add Location")
            }
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding).fillMaxSize()) {
            AndroidView(
                factory = { mapView },
                modifier = Modifier.fillMaxSize(),
                update = { view ->
                    view.overlays.clear()
                    locations.forEach { location ->
                        val marker = Marker(view).apply {
                            position = GeoPoint(location.latitude, location.longitude)
                            title = location.name
                            snippet = location.notes
                            setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                        }
                        view.overlays.add(marker)
                    }
                    view.invalidate()
                }
            )
            
            Surface(
                modifier = Modifier.align(Alignment.TopCenter).padding(16.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.8f),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    "Center the map and press + to save a spot",
                    modifier = Modifier.padding(8.dp),
                    style = MaterialTheme.typography.labelSmall
                )
            }
        }
    }
}

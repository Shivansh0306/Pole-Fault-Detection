package com.ksebl.comkseblfaultapp.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.ksebl.comkseblfaultapp.model.Node
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapType
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState

@Composable
fun MapComponent(
    selectedLocation: Pair<Double, Double>?,
    onLocationSelected: (Double, Double) -> Unit,
    modifier: Modifier = Modifier,
    nodes: List<Node> = emptyList()
) {
    // Patiala coordinates (user's location)
    val defaultLocation = LatLng(30.3398, 76.3869)

    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(
            selectedLocation?.let { LatLng(it.first, it.second) } ?: defaultLocation,
            15f
        )
    }

    val mapProperties = MapProperties(
        isMyLocationEnabled = false,
        mapType = MapType.NORMAL
    )

    Box(modifier = modifier.fillMaxSize()) {
        GoogleMap(
            modifier = Modifier.fillMaxSize(),
            cameraPositionState = cameraPositionState,
            properties = mapProperties,
            onMapClick = { latLng ->
                onLocationSelected(latLng.latitude, latLng.longitude)
            }
        ) {
            // Render backend-provided nodes as map markers
            nodes.forEach { node ->
                val position = LatLng(node.location.latitude, node.location.longitude)
                Marker(
                    state = MarkerState(position = position),
                    title = node.name,
                    snippet = "Status: ${node.status}"
                )
            }

            selectedLocation?.let { location ->
                Marker(
                    state = MarkerState(position = LatLng(location.first, location.second)),
                    title = "Selected Fault Location",
                    snippet = "Tap to report fault here"
                )
            }
        }

        if (selectedLocation == null) {
            Card(
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(16.dp)
                    .wrapContentSize(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f)
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = "Location icon",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(32.dp)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Tap on the map to select fault location",
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.Medium,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }

        selectedLocation?.let { location ->
            Card(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(16.dp)
                    .wrapContentSize(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier.padding(12.dp)
                ) {
                    Text(
                        text = "Selected Location:",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Text(
                        text = "Lat: ${String.format("%.4f", location.first)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Text(
                        text = "Lng: ${String.format("%.4f", location.second)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
        }
    }
}
package com.example.finalproject_mobdev.ui

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.IntentSender
import android.os.Looper
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.finalproject_mobdev.data.Pub
import com.google.accompanist.permissions.*
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import com.google.firebase.auth.FirebaseAuth
import kotlin.math.*

// Coordinates for Dublin and Limerick
val DUBLIN_COORDINATES = Pair(53.3498, -6.2603)
val LIMERICK_COORDINATES = Pair(52.6703343, -8.6289896)

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun HomeScreen(onLogout: () -> Unit) {
    val context = LocalContext.current
    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }

    // Permission states for fine and coarse location
    val locationPermissionsState = rememberMultiplePermissionsState(
        permissions = listOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
    )

    // State variables for location and pub info
    var locationText by remember { mutableStateOf("No location selected") }
    var pubs by remember { mutableStateOf(updatePubListBasedOnLocation(locationText)) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Column {
            Text(
                "🔥 Best Craic Right Now",
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.padding(top = 24.dp, bottom = 8.dp)
            )

            // Change Location Button
            Button(
                onClick = {
                    // Check if all location permissions are granted
                    if (locationPermissionsState.allPermissionsGranted) {
                        locationText = "Retrieving location..."
                        pubs = emptyList()

                        // Check if GPS is enabled, then request the new location
                        checkIfGpsIsEnabled(context) {
                            getCurrentLocation(context, fusedLocationClient) { location ->
                                locationText = location ?: "Unable to retrieve location"
                                pubs = updatePubListBasedOnLocation(locationText)
                            }
                        }
                    } else {
                        // Request location permissions
                        locationPermissionsState.launchMultiplePermissionRequest()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
            ) {
                Text("Change Location")
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Display location and pub info
            Text(
                text = locationText,
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(8.dp)
            )

            // List of Top 10 Pubs
            Text(
                text = "🍺 Top 10 Pubs",
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.padding(top = 24.dp, bottom = 8.dp)
            )

            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                items(pubs) { pub ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(pub.name, style = MaterialTheme.typography.bodyLarge)
                                Text("Craic Score: ${pub.craicScore} 🔥", style = MaterialTheme.typography.bodyMedium)
                                Text(pub.comment, style = MaterialTheme.typography.bodySmall)
                            }
                            Button(onClick = { /* Handle rating functionality here */ }) {
                                Text("Rate 🔥")
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Logout Button
            Button(
                onClick = {
                    FirebaseAuth.getInstance().signOut() // Sign out from Firebase
                    onLogout() // Call the callback to navigate back to MainScreen
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color.Red),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Logout")
            }
        }

        // Floating Action Button for rating
        FloatingActionButton(
            onClick = {
                // Handle floating action button click for rating
            },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp),
            containerColor = Color.Red,
            contentColor = Color.White
        ) {
            Text("Rate 🔥")
        }
    }
}

// Function to update the pub list based on location
private fun updatePubListBasedOnLocation(locationText: String): List<Pub> {
    val locationParts = locationText.split(", ")
    if (locationParts.size < 2) return emptyList()

    val lat = locationParts[0].substringAfter("Lat: ").toDoubleOrNull() ?: return emptyList()
    val lng = locationParts[1].substringAfter("Lng: ").toDoubleOrNull() ?: return emptyList()

    val dublinDistance = calculateDistance(lat, lng, DUBLIN_COORDINATES.first, DUBLIN_COORDINATES.second)
    val limerickDistance = calculateDistance(lat, lng, LIMERICK_COORDINATES.first, LIMERICK_COORDINATES.second)

    return when {
        dublinDistance < 10 -> listOf(
            Pub("Dublin Pub A", 5, "Fantastic atmosphere!"),
            Pub("Dublin Pub B", 4, "Great live music and crowd."),
            Pub("Dublin Pub C", 3, "Relaxed vibe with good drinks.")
        )
        limerickDistance < 10 -> listOf(
            Pub("Limerick Pub A", 5, "Lively crowd and great music."),
            Pub("Limerick Pub B", 4, "Friendly staff and cool ambiance."),
            Pub("Limerick Pub C", 3, "Nice spot for a casual night out.")
        )
        else -> listOf(
            Pub("Generic Pub 1", 2, "Not in Dublin or Limerick"),
            Pub("Generic Pub 2", 3, "Try Dublin or Limerick for better craic!")
        )
    }
}

@SuppressLint("MissingPermission") // Permissions are checked before calling this function
private fun getCurrentLocation(
    context: Context,
    fusedLocationClient: FusedLocationProviderClient,
    onLocationResult: (String?) -> Unit
) {
    val locationRequest = LocationRequest.create().apply {
        priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        interval = 10000
        fastestInterval = 5000
    }

    val locationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {
            val location = locationResult.lastLocation
            if (location != null) {
                onLocationResult("Lat: ${location.latitude}, Lng: ${location.longitude}")
            } else {
                onLocationResult("Location not available")
            }
            // Remove location updates to stop continuous tracking
            fusedLocationClient.removeLocationUpdates(this)
        }
    }

    // Request new location updates
    fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper())
}

private fun calculateDistance(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
    val earthRadius = 6371.0 // Kilometers

    val dLat = Math.toRadians(lat2 - lat1)
    val dLon = Math.toRadians(lon2 - lon1)

    val a = sin(dLat / 2).pow(2) + cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) * sin(dLon / 2).pow(2)
    val c = 2 * atan2(sqrt(a), sqrt(1 - a))

    return earthRadius * c
}

// Function to check if GPS is enabled and request to enable it if not
private fun checkIfGpsIsEnabled(context: Context, onEnabled: () -> Unit) {
    val activity = context as? Activity ?: return

    val locationRequest = LocationRequest.create().apply {
        priority = LocationRequest.PRIORITY_HIGH_ACCURACY
    }
    val builder = LocationSettingsRequest.Builder().addLocationRequest(locationRequest)

    val settingsClient = LocationServices.getSettingsClient(context)
    val task = settingsClient.checkLocationSettings(builder.build())

    task.addOnSuccessListener {
        onEnabled()
    }
    task.addOnFailureListener { exception ->
        if (exception is ResolvableApiException) {
            try {
                exception.startResolutionForResult(activity, 1001)
            } catch (sendEx: IntentSender.SendIntentException) {
                // Ignore the error
            }
        }
    }
}

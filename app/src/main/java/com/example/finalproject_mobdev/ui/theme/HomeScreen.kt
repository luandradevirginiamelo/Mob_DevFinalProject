// HomeScreen.kt
package com.example.finalproject_mobdev.ui

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.IntentSender
import android.os.Looper
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.accompanist.permissions.*
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import kotlin.math.*

// Coordinates for Dublin and Limerick
val DUBLIN_COORDINATES = Pair(53.3498, -6.2603) // Example coordinates for Dublin
val LIMERICK_COORDINATES = Pair(52.6703343, -8.6289896) // Example coordinates for Limerick

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun HomeScreen() {
    val context = LocalContext.current
    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }

    // Permission states for fine and coarse location
    val locationPermissionsState = rememberMultiplePermissionsState(
        permissions = listOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
    )

    var locationText by remember { mutableStateOf("No location selected") }
    var pubInfo by remember { mutableStateOf("") }

    Box(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                "Bem-vindo à Home Screen LUANNA THE BEST!",
                style = MaterialTheme.typography.headlineMedium
            )

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                "Let's find your next unforgettable craic. Choose a location below to get started.",
                style = MaterialTheme.typography.bodyMedium,
                fontSize = 16.sp
            )

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = locationText,
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(8.dp)
            )

            Text(
                text = pubInfo,
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(8.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(onClick = {
                // Check if all location permissions are granted
                if (locationPermissionsState.allPermissionsGranted) {
                    // Reset state for new location selection
                    locationText = "Retrieving location..."
                    pubInfo = ""

                    // Check if GPS is enabled, then request the new location
                    checkIfGpsIsEnabled(context) {
                        getCurrentLocation(context, fusedLocationClient) { location ->
                            locationText = location ?: "Unable to retrieve location"
                            pubInfo = determinePubInfo(locationText)
                        }
                    }
                } else {
                    // Request location permissions
                    locationPermissionsState.launchMultiplePermissionRequest()
                }
            }) {
                Text("Choose Your Location")
            }
        }
    }

    // Observe permission changes and take action when permissions are granted
    LaunchedEffect(locationPermissionsState.allPermissionsGranted) {
        if (locationPermissionsState.allPermissionsGranted) {
            checkIfGpsIsEnabled(context) {
                getCurrentLocation(context, fusedLocationClient) { location ->
                    locationText = location ?: "Unable to retrieve location"
                    pubInfo = determinePubInfo(locationText)
                }
            }
        }
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

    // Request new location updates each time the button is clicked
    fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper())
}

// Function to determine pub information based on coordinates
private fun determinePubInfo(location: String): String {
    val locationParts = location.split(", ")
    if (locationParts.size < 2) return "Invalid location data"

    val lat = locationParts[0].substringAfter("Lat: ").toDoubleOrNull() ?: return "Invalid latitude"
    val lng = locationParts[1].substringAfter("Lng: ").toDoubleOrNull() ?: return "Invalid longitude"

    val dublinDistance = calculateDistance(lat, lng, DUBLIN_COORDINATES.first, DUBLIN_COORDINATES.second)
    val limerickDistance = calculateDistance(lat, lng, LIMERICK_COORDINATES.first, LIMERICK_COORDINATES.second)

    return when {
        dublinDistance < 10 -> "You are in Dublin! Check out Pub B."
        limerickDistance < 10 -> "You are in Limerick! Check out Pub A."
        else -> "Sorry, but you're out of the craic locations."
    }
}

// Calculate distance between two coordinates using Haversine formula
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
    val activity = context as? Activity
    if (activity == null) {
        // If context is not an Activity, we can't show the GPS enable dialog
        return
    }

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
                // Ignore the error.
            }
        }
    }
}

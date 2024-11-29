package com.example.finalproject_mobdev.screen

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.IntentSender
import android.location.Geocoder
import android.os.Looper
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Place
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.google.accompanist.permissions.*
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import java.util.Locale

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun HomeScreen(
    onLogout: () -> Unit,
    onNavigateToPubDetails: (String) -> Unit // Pass pubId to navigate to PubDetailsScreen
) {
    val context = LocalContext.current
    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }
    val coroutineScope = rememberCoroutineScope()
    val auth = FirebaseAuth.getInstance()
    val db = FirebaseFirestore.getInstance()

    // State variables
    var userName by remember { mutableStateOf<String?>(null) } // User name
    var locationText by remember { mutableStateOf("No location selected") } // Location street
    var messageText by remember { mutableStateOf("") } // Location city
    var pubsList by remember { mutableStateOf(listOf<Pair<String, String>>()) } // List of pubs (pubId, pubName)

    // Fetch user name when the screen is loaded
    LaunchedEffect(auth.currentUser?.uid) {
        val uid = auth.currentUser?.uid
        if (uid != null) {
            db.collection("user").document(uid).get()
                .addOnSuccessListener { document ->
                    userName = document.getString("nameandsurname")
                }
                .addOnFailureListener {
                    userName = "Guest" // Fallback if data fetch fails
                }
        }
    }

    // Handle permissions for location
    val locationPermissionsState = rememberMultiplePermissionsState(
        permissions = listOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
    )

    Scaffold(
        snackbarHost = { SnackbarHost(remember { SnackbarHostState() }) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            // Fixed part (Welcome Message and Update Location Button)
            userName?.let { name ->
                Text(
                    text = "Welcome to Limerick, $name! See below where the best Craic is now.",
                    style = MaterialTheme.typography.headlineSmall,
                    modifier = Modifier.padding(top = 24.dp, bottom = 8.dp)
                )
            }

            Button(
                onClick = {
                    if (locationPermissionsState.allPermissionsGranted) {
                        locationText = "Retrieving location..."
                        messageText = ""

                        // Check if GPS is enabled, then request the new location
                        checkIfGpsIsEnabled(context) {
                            getCurrentLocation(context, fusedLocationClient) { location ->
                                if (location != null) {
                                    locationText = location.first // Street
                                    messageText = location.second // City

                                    // Fetch pubs if the city is Limerick
                                    if (messageText == "Limerick") {
                                        fetchPubsFromDatabase { pubs ->
                                            pubsList = pubs
                                        }
                                    } else {
                                        pubsList = listOf() // Clear pubs if not in Limerick
                                        messageText = "No pubs available in your city."
                                    }
                                } else {
                                    locationText = "Unable to retrieve location"
                                    messageText = "Unable to retrieve city"
                                }
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
                Text("Click to update your location")
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Dynamic part (Scrollable list with pubs and logout button)
            LazyColumn(
                modifier = Modifier.fillMaxSize() // Ensure scrollable content takes up the rest of the screen
            ) {
                // Add location details
                item {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Place,
                            contentDescription = "Location Icon",
                            tint = Color.Red, // Icon color
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(locationText, style = MaterialTheme.typography.bodyLarge)
                    }

                    if (messageText.isNotEmpty()) {
                        Text(
                            text = messageText,
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.Black,
                            modifier = Modifier.padding(8.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                }

                // Add pubs list
                items(pubsList) { (pubId, pubName) ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp),
                        elevation = CardDefaults.cardElevation(4.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Pub Name
                            Text(
                                text = pubName,
                                style = MaterialTheme.typography.bodyLarge,
                                modifier = Modifier.weight(1f)
                            )

                            // Floating Action Button for each pub
                            FloatingActionButton(
                                onClick = { onNavigateToPubDetails(pubId) }, // Navigate to details
                                modifier = Modifier.size(65.dp),
                                containerColor = Color.Red,
                                contentColor = Color.White
                            ) {
                                Text("🔥RATE")
                            }
                        }
                    }
                }

                // Add logout button as the last item in the list
                item {
                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = {
                            coroutineScope.launch {
                                FirebaseAuth.getInstance().signOut() // Log out
                                onLogout() // Navigate back to the login screen
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Red),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp)
                    ) {
                        Text("Logout")
                    }
                }
            }
        }
    }
}

// Function to fetch pubs from Firestore
private fun fetchPubsFromDatabase(onResult: (List<Pair<String, String>>) -> Unit) {
    val db = FirebaseFirestore.getInstance()

    db.collection("pubs")
        .whereEqualTo("city", "Limerick") // Query pubs in Limerick
        .get()
        .addOnSuccessListener { documents ->
            val pubs = documents.map { it.id to (it.getString("name") ?: "Unnamed Pub") }
            onResult(pubs) // Return pubId and pubName
        }
        .addOnFailureListener {
            onResult(listOf()) // Return empty list on failure
        }
}

// Get current location (latitude, longitude)
@SuppressLint("MissingPermission")
private fun getCurrentLocation(
    context: Context,
    fusedLocationClient: FusedLocationProviderClient,
    onLocationResult: (Pair<String, String>?) -> Unit
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
                val address = getAddressFromLocation(context, location.latitude, location.longitude)
                onLocationResult(address)
            } else {
                onLocationResult(null)
            }
            fusedLocationClient.removeLocationUpdates(this)
        }
    }

    fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper())
}

// Check if GPS is enabled
private fun checkIfGpsIsEnabled(context: Context, onEnabled: () -> Unit) {
    val activity = context as? Activity ?: return

    val locationRequest = LocationRequest.create().apply {
        priority = LocationRequest.PRIORITY_HIGH_ACCURACY
    }
    val builder = LocationSettingsRequest.Builder().addLocationRequest(locationRequest)

    val settingsClient = LocationServices.getSettingsClient(context)
    val task = settingsClient.checkLocationSettings(builder.build())

    task.addOnSuccessListener { onEnabled() }
    task.addOnFailureListener { exception ->
        if (exception is ResolvableApiException) {
            try {
                exception.startResolutionForResult(activity, 1001)
            } catch (_: IntentSender.SendIntentException) { /* Ignore */ }
        }
    }
}

// Get address from latitude and longitude
private fun getAddressFromLocation(context: Context, latitude: Double, longitude: Double): Pair<String, String>? {
    return try {
        val geocoder = Geocoder(context, Locale.getDefault())
        val addresses = geocoder.getFromLocation(latitude, longitude, 1)
        if (!addresses.isNullOrEmpty()) {
            val address = addresses[0]
            val street = address.thoroughfare ?: "Unknown Street"
            val city = address.locality ?: "Unknown City"
            Pair(street, city)
        } else null
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}

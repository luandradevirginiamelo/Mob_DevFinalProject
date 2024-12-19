@file:OptIn(ExperimentalPermissionsApi::class, ExperimentalMaterial3Api::class)

package com.example.finalproject_mobdev.screen

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.IntentSender
import android.location.Geocoder
import android.os.Looper
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
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
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.finalproject_mobdev.utils.openGoogleMaps
import com.example.finalproject_mobdev.utils.getAddressFromLocation


@Composable
fun HomeScreen(
    homeViewModel: HomeViewModel = viewModel(),
    onLogout: () -> Unit,
    onNavigateToPubDetails: (String) -> Unit,
    onProfileClick: () -> Unit,
    onSettingsClick: () -> Unit,
    navController: NavController
) {
    // State variables to observe changes in location, message, and pub list
    val locationText by homeViewModel.locationText.collectAsState()
    val messageText by homeViewModel.messageText.collectAsState()
    val pubsList by homeViewModel.pubsList.collectAsState()

    // Retrieve the current context for location services
    val context = LocalContext.current
    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }
    val coroutineScope = rememberCoroutineScope()
    val auth = FirebaseAuth.getInstance()
    val db = FirebaseFirestore.getInstance()
    val snackbarHostState = remember { SnackbarHostState() }

    // State for location permissions
    val locationPermissionsState = rememberMultiplePermissionsState(
        permissions = listOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
    )
    var userName by remember { mutableStateOf<String?>(null) }
    var showPermissionErrorDialog by remember { mutableStateOf(false) }
    var isDarkMode by remember { mutableStateOf(false) }
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    // Effect to fetch user information when authentication state changes
    LaunchedEffect(auth.currentUser?.uid) {
        val uid = auth.currentUser?.uid
        if (uid != null) {
            db.collection("user").document(uid).get()
                .addOnSuccessListener { document ->
                    // Retrieve the user's name from Firestore
                    userName = document.getString("nameandsurname")
                }
                .addOnFailureListener {
                    // Default to "Guest" if retrieval fails
                    userName = "Guest"
                }
        }
    }
    // Launching an effect to check for location permissions when the composable enters the composition
    LaunchedEffect(Unit) {
        if (!locationPermissionsState.allPermissionsGranted) {
            locationPermissionsState.launchMultiplePermissionRequest()
        }
    }


    val shouldRefresh = navController.currentBackStackEntry?.savedStateHandle
        ?.getLiveData<Boolean>("shouldRefresh")

    LaunchedEffect(shouldRefresh?.value) {
        if (shouldRefresh?.value == true) {
            shouldRefresh.value = false // Reset the value to prevent duplicate refreshes
            if (locationPermissionsState.allPermissionsGranted) {
                homeViewModel.updateLocation("Retrieving location...", "")
                checkIfGpsIsEnabled(
                    context = context,
                    onGpsEnabled = {
                        getCurrentLocation(context, fusedLocationClient) { location ->
                            if (location != null) {
                                homeViewModel.updateLocation(location.first, location.second)

                                if (location.second == "Limerick") {
                                    fetchPubsFromDatabase { pubs ->
                                        homeViewModel.updatePubs(
                                            pubs.sortedByDescending {
                                                it.second.split("|")[1].toIntOrNull() ?: 0
                                            }
                                        )
                                    }
                                } else {
                                    homeViewModel.updatePubs(listOf())
                                    homeViewModel.updateLocation(location.first, "No pubs available in your city.")
                                }
                            } else {
                                homeViewModel.updateLocation("Unable to retrieve location", "Unable to retrieve city")
                            }
                        }
                    },
                    onGpsError = { errorMessage ->
                        coroutineScope.launch {
                            snackbarHostState.showSnackbar(errorMessage)
                        }
                    }
                )
                // Show a permission error dialog if location permissions are not granted
            } else {
                showPermissionErrorDialog = true
            }
        }
    }

    if (showPermissionErrorDialog) {
        AlertDialog(
            onDismissRequest = { showPermissionErrorDialog = false },
            confirmButton = {
                Button(onClick = {
                    showPermissionErrorDialog = false
                    locationPermissionsState.launchMultiplePermissionRequest()
                }) {
                    Text("Retry")
                }
            },
            dismissButton = {
                Button(onClick = { showPermissionErrorDialog = false }) {
                    Text("Exit")
                }
            },
            title = { Text("Permission Required") },
            text = { Text("Location permission is required to use this app. Please enable it to continue.") }
        )
    }

    val drawerContent: @Composable () -> Unit = {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFD1C4E9))
                .padding(16.dp)
        ) {
            Spacer(modifier = Modifier.height(40.dp))
            Text(
                text = "Menu",
                style = MaterialTheme.typography.headlineSmall,
                color = Color.White,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = { coroutineScope.launch { onProfileClick() } },
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF4A148C),
                    contentColor = Color.White
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Profile")
            }
            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = { coroutineScope.launch { onSettingsClick() } },
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF4A148C),
                    contentColor = Color.White
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Settings")
            }
            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = {
                    FirebaseAuth.getInstance().signOut() // logout user from firebase
                    navController.navigate("login") {    // go to login screen
                        popUpTo("home") { inclusive = true } // Remove HomeScreen from navigation
                    }
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF4A148C),
                    contentColor = Color.White
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Logout")
            }
        }
    }

    // Modal navigation drawer with drawer content and drawer state
    ModalNavigationDrawer(
        drawerContent = drawerContent,
        drawerState = drawerState
    ) {
        // Main scaffold containing the top bar and snackbar host
        Scaffold(
            snackbarHost = { SnackbarHost(snackbarHostState) },
            topBar = {
                // Top app bar with a menu icon to open the drawer
                TopAppBar(
                    title = { Text("Home") },
                    navigationIcon = {
                        IconButton(onClick = {
                            coroutineScope.launch { drawerState.open() }
                        }) {
                            Icon(Icons.Default.Menu, contentDescription = "Menu")
                        }
                    }
                )
            }
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 16.dp, vertical = 24.dp) // Padding ajustado
            ) {
                // Exibe a mensagem de boas-vindas dentro de um Card, se o userName não for nulo
                userName?.let { name ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 16.dp), // Espaçamento acima e abaixo do Card
                        elevation = CardDefaults.cardElevation(8.dp), // Elevação para destaque
                        colors = CardDefaults.cardColors(
                            containerColor = Color(0xFFEDE7F6) // Cor de fundo suave
                        )
                    ) {
                        Text(
                            text = "Welcome to Limerick, $name! See below where the best Craic is now.",
                            style = MaterialTheme.typography.headlineSmall,
                            modifier = Modifier
                                .padding(16.dp) // Padding interno do Card
                                .align(Alignment.CenterHorizontally) // Centraliza o texto horizontalmente
                        )
                    }
                }
                // Button to manually update the list of pubs
                Button(
                    onClick = {
                        if (locationPermissionsState.allPermissionsGranted) {
                            homeViewModel.updateLocation("Retrieving location...", "")
                            checkIfGpsIsEnabled(
                                context = context,
                                onGpsEnabled = {
                                    getCurrentLocation(context, fusedLocationClient) { location ->
                                        if (location != null) {
                                            homeViewModel.updateLocation(location.first, location.second)

                                            if (location.second == "Limerick") {
                                                fetchPubsFromDatabase { pubs ->
                                                    homeViewModel.updatePubs(
                                                        pubs.sortedByDescending {
                                                            it.second.split("|")[1].toIntOrNull() ?: 0
                                                        }
                                                    )
                                                }
                                            } else {
                                                homeViewModel.updatePubs(listOf())
                                                homeViewModel.updateLocation(location.first, "No pubs available in your city.")
                                            }
                                        } else {
                                            homeViewModel.updateLocation("Unable to retrieve location", "Unable to retrieve city")
                                        }
                                    }
                                },
                                onGpsError = { errorMessage ->
                                    coroutineScope.launch {
                                        snackbarHostState.showSnackbar(errorMessage)
                                    }
                                }
                            )
                        } else {
                            showPermissionErrorDialog = true
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp)
                ) {
                    Text("Click to update the List")
                }

                Spacer(modifier = Modifier.height(24.dp))

                LazyColumn(
                    modifier = Modifier.fillMaxSize()
                ) {
                    item {
                        // Linha com o ícone de localização, texto e botão "Maps"
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth() // Row
                                .padding(8.dp)
                        ) {
                            // Ícone de localização
                            Icon(
                                imageVector = Icons.Default.Place,
                                contentDescription = "Location Icon",
                                tint = Color.Red,
                                modifier = Modifier.size(20.dp)
                            )

                            Spacer(modifier = Modifier.width(8.dp))

                            // Text Location
                            Text(
                                text = locationText,
                                style = MaterialTheme.typography.bodyLarge
                            )

                            Spacer(modifier = Modifier.weight(1f)) // Empurra o botão "Maps" para o lado direito


                        }

                        // Exibe a mensagem caso exista algum texto em `messageText`
                        if (messageText.isNotEmpty()) {
                            Text(
                                text = messageText,
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.Black,
                                modifier = Modifier.padding(8.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp)) // Espaçamento adicional
                    }


                    items(pubsList) { (pubId, pubNameWithRate) ->
                        val (pubName, rate) = pubNameWithRate.split("|")
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
                                Text(
                                    text = "$pubName - $rate%",
                                    style = MaterialTheme.typography.bodyLarge,
                                    modifier = Modifier.weight(1f)
                                )

                                FloatingActionButton(
                                    onClick = { onNavigateToPubDetails(pubId) },
                                    modifier = Modifier.size(63.dp),
                                    containerColor = Color(0xFF212121), // Black
                                    contentColor = Color.White
                                ) {
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.Center
                                    ) {
                                        Text("⭐️", color = Color.White, style = MaterialTheme.typography.bodyMedium) // Estrela centralizada
                                        Text("Details", color = Color.White, style = MaterialTheme.typography.bodyMedium) // Palavra abaixo
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// Helper function: Fetch pubs from Firestore
private fun fetchPubsFromDatabase(onResult: (List<Pair<String, String>>) -> Unit) {
    val db = FirebaseFirestore.getInstance()

    db.collection("pubs")
        .whereEqualTo("city", "Limerick")
        .get()
        .addOnSuccessListener { documents ->
            val pubs = documents.map {
                val averageRating = it.getDouble("averageRating")?.toInt()?.toString() ?: "0"
                it.id to "${it.getString("name") ?: "Unnamed Pub"}|$averageRating"
            }.sortedByDescending { it.second.split("|")[1].toIntOrNull() ?: 0 }
            onResult(pubs)
        }
        .addOnFailureListener {
            onResult(listOf())
        }
}

// Helper function: Get current location
@SuppressLint("MissingPermission")
fun getCurrentLocation(
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

// Helper function: Check if GPS is enabled
fun checkIfGpsIsEnabled(
    context: Context,
    onGpsEnabled: () -> Unit,
    onGpsError: (String) -> Unit
) {
    val activity = context as? Activity ?: return
    val locationRequest = LocationRequest.create().apply {
        priority = LocationRequest.PRIORITY_HIGH_ACCURACY
    }
    val builder = LocationSettingsRequest.Builder().addLocationRequest(locationRequest)
    val settingsClient = LocationServices.getSettingsClient(context)
    val task = settingsClient.checkLocationSettings(builder.build())

    task.addOnSuccessListener { onGpsEnabled() }
    task.addOnFailureListener { exception ->
        if (exception is ResolvableApiException) {
            try {
                exception.startResolutionForResult(activity, 1001)
            } catch (_: IntentSender.SendIntentException) {
                onGpsError("Oops! It's not possible to see where the Craic is without your location.")
            }
        } else {
            onGpsError("Oops! It's not possible to see where the Craic is without your location.")
        }
    }
}

@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.finalproject_mobdev.screen

import android.content.Context
import android.content.Intent
import android.media.MediaPlayer
import android.net.Uri
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.finalproject_mobdev.R
import com.example.finalproject_mobdev.ui.theme.Finalproject_MOBDEVTheme
import com.example.finalproject_mobdev.utils.openGoogleMaps
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PubDetailsScreen(
    pubId: String,               // Pub ID to fetch details from Firestore
    onBack: () -> Unit,          // Callback to navigate back
    onNavigateToPubRate: () -> Unit, // Callback to navigate to the PubRateScreen
    onNavigateToGallery: () -> Unit // Callback to navigate to the GalleryScreen
) {
    var isDarkMode by remember { mutableStateOf(false) }
    val db = FirebaseFirestore.getInstance()
    var pubDetails by remember { mutableStateOf<Map<String, Any>?>(null) }
    var comments by remember { mutableStateOf<List<Pair<String, Int>>>(listOf()) } // List of comments and ratings
    var loading by remember { mutableStateOf(true) }
    val scrollState = rememberScrollState()
    val context = LocalContext.current

    // Fetch data from Firestore when the screen is loaded
    LaunchedEffect(pubId) {
        loading = true
        try {
            val pubSnapshot = db.collection("pubs").document(pubId).get().await()
            pubDetails = pubSnapshot.data

            val commentSnapshot = db.collection("pubs").document(pubId).collection("comments").get().await()
            comments = commentSnapshot.documents.mapNotNull { doc ->
                val comment = doc.getString("text") ?: return@mapNotNull null
                val rate = doc.getLong("rate")?.toInt() ?: 0
                comment to rate
            }
        } catch (e: Exception) {
            pubDetails = null
        } finally {
            loading = false
        }
    }

    // Main UI
    Finalproject_MOBDEVTheme(darkTheme = isDarkMode) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text(pubDetails?.get("name") as? String ?: "Pub Details") },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                        }
                    },
                    actions = {
                        IconButton(onClick = { isDarkMode = !isDarkMode }) {
                            Icon(
                                imageVector = if (isDarkMode) Icons.Default.LightMode else Icons.Default.DarkMode,
                                contentDescription = if (isDarkMode) "Light Mode" else "Dark Mode"
                            )
                        }
                    }
                )
            }
        ) { padding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp),
                contentAlignment = Alignment.TopStart
            ) {
                when {
                    loading -> CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                    pubDetails == null -> {
                        Text(
                            text = "Failed to load pub details.",
                            style = MaterialTheme.typography.bodyLarge,
                            color = Color.Red,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.align(Alignment.Center)
                        )
                    }
                    else -> {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .verticalScroll(scrollState)
                                .padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp),
                            horizontalAlignment = Alignment.CenterHorizontally // Centraliza tudo na horizontal
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp),
                                horizontalArrangement = Arrangement.SpaceEvenly,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // BUTTON "Rate 🔥"
                                FloatingActionButton(
                                    onClick = onNavigateToPubRate,
                                    modifier = Modifier.size(65.dp),
                                    containerColor = Color.Red,
                                    contentColor = Color.White
                                ) {
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.Center
                                    ) {
                                        Text("🔥", style = MaterialTheme.typography.bodyLarge) // Emoji
                                        Text("Rate", style = MaterialTheme.typography.labelSmall)
                                    }
                                }

                                // BUTTON "Go to Pub 📍"
                                FloatingActionButton(
                                    onClick = {
                                        val latitude = pubDetails?.get("latitude") as? Double
                                        val longitude = pubDetails?.get("longitude") as? Double
                                        if (latitude != null && longitude != null) {
                                            openGoogleMaps(context, latitude, longitude)
                                        }
                                    },
                                    modifier = Modifier.size(65.dp),
                                    containerColor = Color(0xFF4CAF50), // GREEN?
                                    contentColor = Color.White
                                ) {
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.Center
                                    ) {
                                        Text("📍", style = MaterialTheme.typography.bodyLarge) // Emoji
                                        Text("Pub", style = MaterialTheme.typography.labelSmall)
                                    }
                                }

                                // Botão "Gallery 📷"
                                FloatingActionButton(
                                    onClick = onNavigateToGallery,
                                    modifier = Modifier.size(65.dp),
                                    containerColor = Color(0xFF212121), // Black colour
                                    contentColor = Color.White
                                ) {
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.Center
                                    ) {
                                        Text("📷", style = MaterialTheme.typography.bodyLarge) // Emoji
                                        Text("Gallery", style = MaterialTheme.typography.labelSmall)
                                    }

                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            // CraicMeter Display
                            val averageRate =
                                (pubDetails?.get("averageRating") as? Double)?.toInt() ?: 0
                            CraicMeter(rate = averageRate)

                            Spacer(modifier = Modifier.height(16.dp))

                            // Container for Pub Details
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(8.dp),
                                elevation = CardDefaults.cardElevation(8.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                            ) {
                                Column(
                                    modifier = Modifier.padding(16.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Text(
                                        text = "Address: ${pubDetails?.get("Address") as? String ?: "No address available"}",
                                        style = MaterialTheme.typography.bodyLarge
                                    )
                                    Text(
                                        text = "Close Time: ${pubDetails?.get("Close") as? String ?: "No close time available"}",
                                        style = MaterialTheme.typography.bodyLarge
                                    )
                                    Text(
                                        text = "Phone Number: ${pubDetails?.get("phone") as? String ?: "No phone number available"}",
                                        style = MaterialTheme.typography.bodyLarge
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            // Comments Section
                            Text(
                                text = "User Comments and Ratings:",
                                style = MaterialTheme.typography.headlineSmall
                            )
                            comments.forEach { (comment, userRate) ->
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp),
                                    elevation = CardDefaults.cardElevation(4.dp)
                                ) {
                                    Column(modifier = Modifier.padding(8.dp)) {
                                        Text(text = comment, style = MaterialTheme.typography.bodyMedium)
                                        Text(
                                            text = "Rate: $userRate%",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = Color.Gray
                                        )
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

// CraicMeter Component
@Composable
fun CraicMeter(rate: Int) {
    val context = LocalContext.current
    var currentMediaPlayer by remember { mutableStateOf<MediaPlayer?>(null) }

    LaunchedEffect(rate) {
        currentMediaPlayer?.stop()
        currentMediaPlayer?.release()
        currentMediaPlayer = when {
            rate > 60 -> MediaPlayer.create(context, R.raw.oy) // High Craic
            rate in 40..60 -> MediaPlayer.create(context, R.raw.snoring) // Medium Craic
            else -> MediaPlayer.create(context, R.raw.cricket) // Low Craic
        }
        currentMediaPlayer?.start()
    }

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = when {
                rate > 60 -> "🔥 High Craic"
                rate < 40 -> "🧊 Low Craic"
                else -> "🎵 Medium Craic"
            },
            style = MaterialTheme.typography.headlineSmall
        )
        Text(
            text = "$rate%",
            style = MaterialTheme.typography.bodyLarge,
            color = Color.Red,
            textAlign = TextAlign.Center
        )
    }
}

// Helper function to open Google Maps
fun openGoogleMaps(context: Context, latitude: Double, longitude: Double) {
    val gmmIntentUri = Uri.parse("google.navigation:q=$latitude,$longitude&mode=d")
    val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
    mapIntent.setPackage("com.google.android.apps.maps")
    context.startActivity(mapIntent)
}

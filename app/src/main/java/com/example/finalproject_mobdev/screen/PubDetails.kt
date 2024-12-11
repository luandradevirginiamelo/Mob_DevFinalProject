@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.finalproject_mobdev.screen

import android.media.MediaPlayer
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.finalproject_mobdev.R
import com.example.finalproject_mobdev.ui.theme.Finalproject_MOBDEVTheme
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

@Composable
fun PubDetailsScreen(
    pubId: String,               // Pub ID to fetch details from Firestore
    onBack: () -> Unit,          // Callback to navigate back
    onNavigateToPubRate: () -> Unit // Callback to navigate to the PubRateScreen
) {
    // States for Dark Mode, Firestore Data, and Loading State
    var isDarkMode by remember { mutableStateOf(false) }
    val db = FirebaseFirestore.getInstance()
    var pubDetails by remember { mutableStateOf<Map<String, Any>?>(null) }
    var comments by remember { mutableStateOf<List<Pair<String, Int>>>(listOf()) } // List of comments and ratings
    var loading by remember { mutableStateOf(true) }
    val scrollState = rememberScrollState()

    // Fetch data from Firestore when the screen is loaded
    LaunchedEffect(pubId) {
        loading = true
        try {
            // Fetch pub details
            val pubSnapshot = db.collection("pubs").document(pubId).get().await()
            pubDetails = pubSnapshot.data

            // Fetch user comments and ratings
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
                    title = { Text(pubDetails?.get("name") as? String ?: "Pub Details") }, // Pub name dynamically fetched
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
                    loading -> {
                        // Show a loading spinner while data is being fetched
                        CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                    }
                    pubDetails == null -> {
                        // Show error message if data fetch fails
                        Text(
                            text = "Failed to load pub details.",
                            style = MaterialTheme.typography.bodyLarge,
                            color = Color.Red,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.align(Alignment.Center)
                        )
                    }
                    else -> {
                        // Display pub details and user comments
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .verticalScroll(scrollState) // Add vertical scroll
                                .padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp),
                            horizontalAlignment = Alignment.Start
                        ) {
                            // CraicMeter Title with Rate Button
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = "CraicMeter",
                                    style = MaterialTheme.typography.headlineSmall,
                                    color = Color.Blue,
                                    modifier = Modifier.weight(1f)
                                )

                                FloatingActionButton(
                                    onClick = onNavigateToPubRate, // Navigate to PubRateScreen
                                    containerColor = Color.Red,
                                    contentColor = Color.White,
                                    modifier = Modifier
                                        .size(80.dp) // Slightly larger button for prominence
                                        .padding(8.dp), // Add some padding around the button
                                    elevation = FloatingActionButtonDefaults.elevation(
                                        defaultElevation = 8.dp, // Add subtle elevation
                                        pressedElevation = 12.dp // More elevation when pressed
                                )
                                ) {
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.Center
                                    ) {
                                        Text(
                                            text = "Rate 🔥",
                                            style = MaterialTheme.typography.labelLarge.copy(color = Color.White), // Custom text style
                                            maxLines = 1
                                        )
                                    }
                                }

                            }

                            // CraicMeter (Thermometer)
                            val averageRate = (pubDetails?.get("averageRating") as? Double)?.toInt() ?: 0
                            CraicMeter(rate = averageRate)

                            Spacer(modifier = Modifier.height(16.dp))

                            // Pub Details
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
                                        Text(
                                            text = comment,
                                            style = MaterialTheme.typography.bodyMedium
                                        )
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

@Composable
fun CraicMeter(rate: Int) {
    val context = LocalContext.current
    var currentMediaPlayer by remember { mutableStateOf<MediaPlayer?>(null) }

    // Play sound based on the Craic level
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

    // Display the CraicMeter and global rating
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp),
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


@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.finalproject_mobdev.screen

import androidx.compose.foundation.layout.* // Correct imports
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import com.example.finalproject_mobdev.ui.theme.Finalproject_MOBDEVTheme // Import your theme
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.ui.draw.scale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PubDetailsScreen(
    pubId: String, // Pub ID to fetch details from Firestore
    onBack: () -> Unit // Callback to navigate back
) {
    // Dark mode state
    var isDarkMode by remember { mutableStateOf(false) }

    Finalproject_MOBDEVTheme(darkTheme = isDarkMode) { // Wrap the screen in your theme to apply Dark Mode
        val db = FirebaseFirestore.getInstance() // Initialize Firestore
        var pubDetails by remember { mutableStateOf<Map<String, Any>?>(null) } // State for pub details
        var comments by remember { mutableStateOf<List<String>>(listOf()) } // State for comments
        var newComment by remember { mutableStateOf("") } // State for new comment input
        var loading by remember { mutableStateOf(true) } // Loading state
        val scrollState = rememberScrollState() // State for vertical scrolling

        // Fetch pub details and comments when pubId changes
        LaunchedEffect(pubId) {
            loading = true
            try {
                // Fetch pub details
                val document = db.collection("pubs").document(pubId).get().await()
                pubDetails = document.data

                // Fetch comments from the subcollection "comments"
                val commentDocs = db.collection("pubs").document(pubId)
                    .collection("comments")
                    .get()
                    .await()
                comments = commentDocs.documents.mapNotNull { it.getString("text") }
            } catch (e: Exception) {
                pubDetails = null
            } finally {
                loading = false
            }
        }

        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Pub Details") },
                    navigationIcon = {
                        IconButton(onClick = onBack) { // Back button action
                            Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                        }
                    },
                    actions = {
                        IconButton(
                            onClick = { isDarkMode = !isDarkMode } // Toggle dark mode
                        ) {
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
                if (loading) {
                    // Show a loading spinner while fetching details
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                } else if (pubDetails == null) {
                    // Show error message if pub details couldn't be fetched
                    Text(
                        text = "Failed to load pub details.",
                        style = MaterialTheme.typography.bodyLarge,
                        color = Color.Red,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.align(Alignment.Center)
                    )
                } else {
                    // Display pub details
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .verticalScroll(scrollState) // Add vertical scroll
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        horizontalAlignment = Alignment.Start // Corrected here
                    ) {
                        // Pub Name
                        Text(
                            text = pubDetails?.get("name") as? String ?: "Unknown Pub",
                            style = MaterialTheme.typography.headlineMedium,
                            textAlign = TextAlign.Start
                        )

                        // CraicMeter (Interactive Thermometer)
                        val rate = (pubDetails?.get("rate") as? Long)?.toInt() ?: 0 // Get the rate from Firestore
                        CraicMeter(pubId = pubId, initialRate = rate)

                        // Address
                        Text(
                            text = "Address: ${pubDetails?.get("address") as? String ?: "No address available."}",
                            style = MaterialTheme.typography.bodyLarge
                        )

                        // Close Time
                        Text(
                            text = "Close Time: ${pubDetails?.get("closeTime") as? String ?: "No close time available."}",
                            style = MaterialTheme.typography.bodyLarge
                        )

                        // Phone Number
                        Text(
                            text = "Phone Number: ${pubDetails?.get("phoneNumber") as? String ?: "No phone number available."}",
                            style = MaterialTheme.typography.bodyLarge
                        )

                        // Comments Section
                        Text(
                            text = "Comments:",
                            style = MaterialTheme.typography.headlineSmall
                        )
                        // Display Existing Comments
                        comments.forEach { comment ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                elevation = CardDefaults.cardElevation(4.dp)
                            ) {
                                Text(
                                    text = comment,
                                    style = MaterialTheme.typography.bodyMedium,
                                    modifier = Modifier.padding(8.dp)
                                )
                            }
                        }

                        // Add New Comment
                        TextField(
                            value = newComment,
                            onValueChange = { newComment = it },
                            label = { Text("Add a comment") },
                            modifier = Modifier.fillMaxWidth()
                        )

                        Button(
                            onClick = {
                                if (newComment.isNotBlank()) {
                                    // Save the comment to Firestore
                                    db.collection("pubs").document(pubId)
                                        .collection("comments")
                                        .add(mapOf("text" to newComment))
                                        .addOnSuccessListener {
                                            // Update local comments list
                                            comments = comments + newComment
                                            newComment = "" // Clear input field
                                        }
                                }
                            },
                            modifier = Modifier.align(Alignment.End)
                        ) {
                            Text("Submit")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CraicMeter(pubId: String, initialRate: Int) {
    var rate by remember { mutableStateOf(initialRate) } // Local state for the slider
    val db = FirebaseFirestore.getInstance() // Firestore instance
    val fireEmojiVisible = rate > 60 // Show fire emoji if rate > 60
    val fireEmojiScale by animateFloatAsState(if (fireEmojiVisible) 1.5f else 1f)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Display the current value of the CraicMeter
        Text(
            text = when {
                rate > 60 -> "🔥 High Craic"
                rate < 40 -> "🧊 Low Craic"
                else -> "🎵 Medium Craic"
            },
            style = MaterialTheme.typography.headlineSmall,

        )
        Text(
            text = "$rate%", // Displays the current rate percentage
            style = MaterialTheme.typography.bodyLarge,
            color = Color.Red,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Fire Emoji Animation
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(80.dp)
                .scale(fireEmojiScale)
        ) {
            this@Column.AnimatedVisibility(visible = fireEmojiVisible) {
                Text(
                    text = "🔥",
                    fontSize = 40.sp,
                    modifier = Modifier.padding(4.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // CraicMeter Slider
        Slider(
            value = rate.toFloat(),
            onValueChange = { newRate -> rate = newRate.toInt() },
            onValueChangeFinished = {
                db.collection("pubs").document(pubId)
                    .update("rate", rate)
            },
            valueRange = 0f..100f,
            colors = SliderDefaults.colors(
                thumbColor = Color.Red,
                activeTrackColor = Color.Red,
                inactiveTrackColor = Color.Gray
            ),
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        Spacer(modifier = Modifier.height(8.dp))
    }
}

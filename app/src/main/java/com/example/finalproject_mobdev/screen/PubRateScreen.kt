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
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PubRateScreen(
    pubId: String, // Pub ID to fetch details from Firestore
    onBack: () -> Unit // Callback to navigate back
) {
    // Dark mode state
    var isDarkMode by remember { mutableStateOf(false) }

    Finalproject_MOBDEVTheme(darkTheme = isDarkMode) {
        val db = FirebaseFirestore.getInstance()
        val auth = FirebaseAuth.getInstance()
        val currentUid = auth.currentUser?.uid

        // State variables
        var pubDetails by remember { mutableStateOf<Map<String, Any>?>(null) }
        var comments by remember { mutableStateOf<List<Map<String, String>>>(listOf()) }
        var newComment by remember { mutableStateOf("") }
        var rate by remember { mutableStateOf(0) } // Track the user's current rate
        var loading by remember { mutableStateOf(true) }
        val coroutineScope = rememberCoroutineScope()

        // Fetch pub details and comments when pubId changes
        LaunchedEffect(pubId) {
            loading = true
            try {
                val document = db.collection("pubs").document(pubId).get().await()
                pubDetails = document.data

                val commentDocs = db.collection("pubs").document(pubId)
                    .collection("comments")
                    .get()
                    .await()

                // Map each comment's `userId` to `nameandsurname`
                val mappedComments = commentDocs.documents.mapNotNull { doc ->
                    val text = doc.getString("text")
                    val userId = doc.getString("userId")
                    val rateValue = doc.getLong("rate")?.toInt()
                    if (text != null && userId != null && rateValue != null) {
                        val userDocument = db.collection("user").document(userId).get().await()
                        val name = userDocument.getString("nameandsurname") ?: "Unknown User"
                        mapOf("text" to text, "name" to name, "rate" to rateValue.toString())
                    } else {
                        null
                    }
                }

                mappedComments.also { comments = it }
            } catch (e: Exception) {
                pubDetails = null
            } finally {
                loading = false
            }
        }

        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Your Pub Rating") },
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
                if (loading) {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                } else if (pubDetails == null) {
                    Text(
                        text = "Failed to load pub details.",
                        style = MaterialTheme.typography.bodyLarge,
                        color = Color.Red,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.align(Alignment.Center)
                    )
                } else {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                    ) {
                        Text(
                            text = pubDetails?.get("name") as? String ?: "Unknown Pub",
                            style = MaterialTheme.typography.headlineMedium
                        )

                        CraicMeter(
                            pubId = pubId,
                            initialRate = (pubDetails?.get("averageRating") as? Long)?.toInt() ?: 0,
                            onRateChange = { newRate -> rate = newRate } // Track the rate
                        )

                        Text(
                            text = "Comments:",
                            style = MaterialTheme.typography.headlineSmall
                        )
                        comments.forEach { comment ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                elevation = CardDefaults.cardElevation(4.dp)
                            ) {
                                Column(modifier = Modifier.padding(8.dp)) {
                                    Text(
                                        text = comment["text"] ?: "",
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                    Text(
                                        text = "User: ${comment["userId"]}, Rate: ${comment["rate"]}%",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Color.Gray
                                    )
                                }
                            }
                        }

                        TextField(
                            value = newComment,
                            onValueChange = { newComment = it },
                            label = { Text("Add a comment") },
                            modifier = Modifier.fillMaxWidth()
                        )

                        Button(
                            onClick = {
                                if (newComment.isNotBlank() && currentUid != null) {
                                    // Add comment to Firestore
                                    db.collection("pubs").document(pubId)
                                        .collection("comments")
                                        .add(
                                            mapOf(
                                                "text" to newComment,
                                                "userId" to currentUid,
                                                "rate" to rate // Save the current rate
                                            )
                                        )
                                        .addOnSuccessListener {
                                            comments = comments + mapOf(
                                                "text" to newComment,
                                                "userId" to currentUid,
                                                "rate" to rate.toString()
                                            )
                                            newComment = ""

                                            coroutineScope.launch {
                                                updateGlobalRating(db, pubId)
                                            }
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
fun CraicMeter(pubId: String, initialRate: Int, onRateChange: (Int) -> Unit) {
    var rate by remember { mutableStateOf(initialRate) }
    val context = LocalContext.current
    var currentMediaPlayer by remember { mutableStateOf<MediaPlayer?>(null) }

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

        Spacer(modifier = Modifier.height(8.dp))

        Slider(
            value = rate.toFloat(),
            onValueChange = { newRate ->
                val oldRate = rate
                rate = newRate.toInt()
                onRateChange(rate) // Notify the parent of rate change

                if (getCraicRange(oldRate) != getCraicRange(rate)) {
                    currentMediaPlayer?.stop()
                    currentMediaPlayer?.release()

                    currentMediaPlayer = when {
                        rate > 60 -> MediaPlayer.create(context, R.raw.oy)
                        rate in 40..59 -> MediaPlayer.create(context, R.raw.snoring)
                        else -> MediaPlayer.create(context, R.raw.cricket)
                    }
                    currentMediaPlayer?.start()
                }
            },
            valueRange = 0f..100f,
            colors = SliderDefaults.colors(
                thumbColor = Color.Red,
                activeTrackColor = Color.Red,
                inactiveTrackColor = Color.Gray
            ),
            modifier = Modifier.padding(horizontal = 16.dp)
        )
    }
}

fun getCraicRange(rate: Int): String {
    return when {
        rate > 60 -> "HIGH"
        rate in 40..59 -> "MEDIUM"
        else -> "LOW"
    }
}

suspend fun updateGlobalRating(db: FirebaseFirestore, pubId: String) {
    try {
        val commentDocs = db.collection("pubs").document(pubId)
            .collection("comments")
            .get()
            .await()

        val allRates = commentDocs.documents.mapNotNull { it.getLong("rate")?.toInt() }

        if (allRates.isNotEmpty()) {
            val averageRating = allRates.average()
            db.collection("pubs").document(pubId)
                .update("averageRating", averageRating)
        }
    } catch (e: Exception) {
        e.printStackTrace()
    }
}

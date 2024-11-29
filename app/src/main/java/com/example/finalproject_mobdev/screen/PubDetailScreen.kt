@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.finalproject_mobdev.screen

import androidx.compose.foundation.layout.* // Correct imports
import androidx.compose.foundation.background
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
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
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

@Composable
fun PubDetailsScreen(
    pubId: String, // Pub ID to fetch details from Firestore
    onBack: () -> Unit // Callback to navigate back
) {
    val db = FirebaseFirestore.getInstance() // Initialize Firestore
    var pubDetails by remember { mutableStateOf<Map<String, Any>?>(null) } // State for pub details
    var comments by remember { mutableStateOf<List<String>>(listOf()) } // State for comments
    var newComment by remember { mutableStateOf("") } // State for new comment input
    var loading by remember { mutableStateOf(true) } // Loading state

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
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Pub Name
                    Text(
                        text = pubDetails?.get("name") as? String ?: "Unknown Pub",
                        style = MaterialTheme.typography.headlineMedium,
                        textAlign = TextAlign.Start
                    )

                    // Thermometer with Fire Icon
                    Thermometer()

                    // Pub City
                    Text(
                        text = "City: ${pubDetails?.get("city") as? String ?: "Unknown City"}",
                        style = MaterialTheme.typography.bodyLarge
                    )
                    // Pub Description
                    Text(
                        text = "Description: ${pubDetails?.get("description") as? String ?: "No description available."}",
                        style = MaterialTheme.typography.bodyMedium
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

@Composable
fun Thermometer() {
    // Placeholder for thermometer logic
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(vertical = 8.dp)
    ) {
        Text(
            text = "🔥",
            fontSize = 24.sp,
            color = Color.Red,
            modifier = Modifier.padding(end = 8.dp)
        )
        LinearProgressIndicator(
            progress = 0.7f, // Example: 70% (this can be dynamic)
            color = Color.Red,
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
        )
    }
}


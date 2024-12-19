@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.finalproject_mobdev.screen

import android.content.Context
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GalleryScreen(pubId: String, onBack: () -> Unit) {
    val context = LocalContext.current
    val firebaseStorage = FirebaseStorage.getInstance()

    // States
    var imageUris by remember { mutableStateOf<List<Uri>>(emptyList()) }
    var imageUriToUpload by remember { mutableStateOf<Uri?>(null) }
    var uploadStatus by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    val coroutineScope = rememberCoroutineScope()

    // Load images into the screen
    LaunchedEffect(Unit) {
        isLoading = true
        imageUris = loadImages(pubId, firebaseStorage)
        isLoading = false
    }

    // Launcher to choose image from gallery
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
        onResult = { uri: Uri? ->
            imageUriToUpload = uri
        }
    )

    // UI
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Gallery Pictures") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {
            // Button to choose an image
            Button(
                onClick = { imagePickerLauncher.launch("image/*") },
                colors = ButtonDefaults.buttonColors(containerColor = Color.Black, contentColor = Color.White),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text("Choose Photo")
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Show preview of the selected image
            imageUriToUpload?.let { uri ->
                Image(
                    painter = rememberAsyncImagePainter(model = uri),
                    contentDescription = null,
                    modifier = Modifier
                        .size(200.dp)
                        .padding(8.dp),
                    contentScale = ContentScale.Crop
                )

                Spacer(modifier = Modifier.height(8.dp))

                Button(
                    onClick = {
                        imageUriToUpload?.let { uri ->
                            coroutineScope.launch {
                                isLoading = true
                                uploadStatus = uploadPhotoToFirebaseStorage(context, pubId, uri)
                                imageUris = loadImages(pubId, firebaseStorage) // Reload images after upload
                                isLoading = false
                            }
                        } ?: run {
                            Toast.makeText(context, "No image selected!", Toast.LENGTH_SHORT).show()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Black, contentColor = Color.White)
                ) {
                    Text("Upload Photo")
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Display upload status
            uploadStatus?.let { status ->
                Text(
                    text = status,
                    color = if (status.contains("success", true)) Color.Green else Color.Red,
                    modifier = Modifier.padding(8.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Show loading indicator or display images
            if (isLoading) {
                CircularProgressIndicator()
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(imageUris) { uri ->
                        Image(
                            painter = rememberAsyncImagePainter(model = uri),
                            contentDescription = null,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp)
                                .padding(8.dp),
                            contentScale = ContentScale.Crop
                        )
                    }
                }
            }
        }
    }
}

// Function to load images
private suspend fun loadImages(pubId: String, firebaseStorage: FirebaseStorage): List<Uri> {
    return try {
        val storageRef = firebaseStorage.reference.child("pubs/$pubId/gallery")
        val images = storageRef.listAll().await()
        images.items.mapNotNull { it.downloadUrl.await() }
    } catch (e: Exception) {
        e.printStackTrace()
        emptyList()
    }
}

// Function to upload photo to Firebase Storage
private suspend fun uploadPhotoToFirebaseStorage(
    context: Context,
    pubId: String,
    imageUri: Uri
): String {
    try {
        // Generate a unique name for the file
        val fileName = "gallery/${UUID.randomUUID()}.jpg"

        // Path in Firebase Storage
        val storageRef = FirebaseStorage.getInstance()
            .reference.child("pubs/$pubId/$fileName")

        // Log the path
        Log.d("Upload", "Uploading image to path: pubs/$pubId/$fileName")

        // Check if URI is valid before upload
        if (imageUri.toString().isEmpty()) {
            val errorMessage = "Error: Invalid image URI"
            Log.e("Upload", errorMessage)
            return errorMessage
        }

        // Perform the upload
        val uploadTask = storageRef.putFile(imageUri).await()
        Log.d("Upload", "Upload completed successfully: $uploadTask")

        // Get the download URL
        val downloadUrl = storageRef.downloadUrl.await()
        val successMessage = "Photo uploaded successfully! URL: $downloadUrl"
        Log.d("Upload", successMessage)

        // Show success message to the user
        Toast.makeText(context, "Upload Successful!", Toast.LENGTH_SHORT).show()

        return successMessage
    } catch (e: Exception) {
        // Capture errors
        val errorMessage = "Failed to upload photo: ${e.message}"
        Log.e("Upload", errorMessage, e)

        // Show error message to the user
        Toast.makeText(context, "Upload Failed: ${e.message}", Toast.LENGTH_SHORT).show()

        return errorMessage
    }
}
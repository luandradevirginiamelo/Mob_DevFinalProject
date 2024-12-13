package com.example.finalproject_mobdev.screen

import android.content.Context
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
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
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PhotoUploadScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    var imageUri by remember { mutableStateOf<Uri?>(null) }
    var uploadStatus by remember { mutableStateOf<String?>(null) }

    // Lançador para escolher a foto
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
        onResult = { uri: Uri? ->
            imageUri = uri
        }
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Upload Photo") },
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
            verticalArrangement = Arrangement.Center
        ) {
            Button(onClick = { imagePickerLauncher.launch("image/*") }) {
                Text("Choose Photo")
            }

            Spacer(modifier = Modifier.height(16.dp))

            imageUri?.let { uri ->
                Image(
                    painter = rememberAsyncImagePainter(model = uri),
                    contentDescription = null,
                    modifier = Modifier.size(200.dp),
                    contentScale = ContentScale.Crop
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            imageUri?.let { uri ->
                Button(onClick = {
                    uploadPhotoToFirebaseStorage(context, uri) { status ->
                        uploadStatus = status
                    }
                }) {
                    Text("Upload Photo")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            uploadStatus?.let { status ->
                Text(
                    text = status,
                    color = if (status.contains("success", true)) Color.Green else Color.Red
                )
            }
        }
    }
}

// Função para fazer upload da imagem ao Firebase
fun uploadPhotoToFirebaseStorage(
    context: Context,
    imageUri: Uri,
    onStatusUpdate: (String) -> Unit
) {
    val storage = FirebaseStorage.getInstance() // Inicialize o Firebase Storage
    val storageRef = storage.reference.child("uploads/${UUID.randomUUID()}.jpg") // Define o caminho no Storage

    storageRef.putFile(imageUri)
        .addOnSuccessListener {
            storageRef.downloadUrl.addOnSuccessListener { downloadUrl ->
                val successMessage = "Photo uploaded successfully!\nURL: $downloadUrl"
                onStatusUpdate(successMessage)
                Toast.makeText(context, "Upload Successful!", Toast.LENGTH_SHORT).show()
            }
        }
        .addOnFailureListener { exception ->
            val errorMessage = "Failed to upload photo: ${exception.message}"
            onStatusUpdate(errorMessage)
            Toast.makeText(context, "Upload Failed: ${exception.message}", Toast.LENGTH_SHORT).show()
        }
}

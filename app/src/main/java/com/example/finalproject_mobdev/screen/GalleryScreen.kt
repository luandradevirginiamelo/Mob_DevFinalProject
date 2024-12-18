@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.finalproject_mobdev.screen

import android.content.Context
import android.net.Uri
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

@Composable
fun GalleryScreen(pubId: String, onBack: () -> Unit) {
    val context = LocalContext.current
    val firebaseStorage = FirebaseStorage.getInstance()

    // Estados
    var imageUris by remember { mutableStateOf<List<Uri>>(emptyList()) }
    var imageUriToUpload by remember { mutableStateOf<Uri?>(null) }
    var uploadStatus by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    val coroutineScope = rememberCoroutineScope()

    // Carregar imagens ao iniciar a tela
    LaunchedEffect(Unit) {
        isLoading = true
        imageUris = loadImages(pubId, firebaseStorage)
        isLoading = false
    }

    // Launcher para escolher imagem da galeria
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
        onResult = { uri: Uri? ->
            imageUriToUpload = uri
        }
    )

    // Scaffold com estrutura da tela
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
            // Botão para escolher imagem
            Button(
                onClick = { imagePickerLauncher.launch("image/*") },
                colors = ButtonDefaults.buttonColors(containerColor = Color.Blue, contentColor = Color.White),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text("Choose Photo")
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Exibir pré-visualização da imagem selecionada
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

                val coroutineScope = rememberCoroutineScope() // Define o CoroutineScope fora do botão

                Button(
                    onClick = {
                        imageUriToUpload?.let { uri -> // Verifica se o URI não é nulo
                            coroutineScope.launch {
                                isLoading = true
                                uploadPhotoToFirebaseStorage(context, pubId, uri) { status ->
                                    uploadStatus = status
                                }
                                // Recarregar imagens após upload
                                imageUris = loadImages(pubId, firebaseStorage)
                                isLoading = false
                            }
                        } ?: run {
                            Toast.makeText(context, "No image selected!", Toast.LENGTH_SHORT).show()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Green, contentColor = Color.White)
                ) {
                    Text("Upload Photo")
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Exibir status do upload
            uploadStatus?.let { status ->
                Text(
                    text = status,
                    color = if (status.contains("success", true)) Color.Green else Color.Red,
                    modifier = Modifier.padding(8.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Indicador de carregamento inicial
            if (isLoading) {
                CircularProgressIndicator()
            } else {
                // Lista de imagens carregadas
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

/**
 * Função para carregar imagens do Firebase Storage.
 */
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

/**
 * Função para fazer upload da imagem ao Firebase Storage.
 */
private suspend fun uploadPhotoToFirebaseStorage(
    context: Context,
    pubId: String,
    imageUri: Uri,
    onStatusUpdate: (String) -> Unit
) {
    try {
        val storageRef = FirebaseStorage.getInstance()
            .reference.child("pubs/$pubId/gallery/${UUID.randomUUID()}.jpg")

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
    } catch (e: Exception) {
        e.printStackTrace()
        onStatusUpdate("Failed to upload photo: ${e.message}")
    }
}

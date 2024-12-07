package com.example.finalproject_mobdev.screen

import androidx.compose.foundation.layout.* // Required for layout modifiers
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.* // Required for Material3 components
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class) // Ensure ExperimentalMaterial3Api is opted in
@Composable
fun ProfileScreen(onBack: () -> Unit) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Profile") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding), // Apply padding from Scaffold
            contentAlignment = Alignment.Center // Center the content
        ) {
            Text(
                text = "This is the Profile Screen", // Profile screen text
                style = MaterialTheme.typography.headlineMedium // Apply a headline style
            )
        }
    }
}

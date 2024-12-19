package com.example.finalproject_mobdev.screen

import android.net.Uri
import android.view.ViewGroup
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.finalproject_mobdev.R
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.ui.PlayerView
import com.google.android.exoplayer2.ui.AspectRatioFrameLayout
import com.google.firebase.auth.FirebaseAuth
import android.content.Context

@Composable
fun LoginScreen(
    onRegisterClick: () -> Unit,
    onCraicClick: () -> Unit
) {
    val auth = FirebaseAuth.getInstance()
    val context = LocalContext.current
    val sharedPreferences = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)

    // States for input fields
    var username by remember { mutableStateOf(TextFieldValue()) }
    var password by remember { mutableStateOf(TextFieldValue()) }
    var passwordVisible by remember { mutableStateOf(false) }
    var keepMeLoggedIn by remember { mutableStateOf(false) }
    var showForgotPasswordDialog by remember { mutableStateOf(false) }

    // Snackbar state
    val snackbarHostState = remember { SnackbarHostState() }
    var snackbarMessage by remember { mutableStateOf<String?>(null) }


    LaunchedEffect(Unit) {
        val keepMeLoggedIn = sharedPreferences.getBoolean("keep_me_logged_in", false)


        if (auth.currentUser != null) {
            if (!keepMeLoggedIn) {
                // If not selected do logout
                auth.signOut()
            } else {
                // Navigate to Home if "Keep Me Logged In" is selected
                onCraicClick()
            }
        }
    }

    // Trigger Snackbar when a message is set
    LaunchedEffect(snackbarMessage) {
        snackbarMessage?.let { message ->
            snackbarHostState.showSnackbar(message)
            snackbarMessage = null // Clear the message after showing it
        }
    }

    // Main layout
    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) } // Attach the SnackbarHost
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            // Background video
            VideoBackground()

            // Content overlay (on top of the video)
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    // Welcome text
                    Text(
                        text = "Welcome to Where's the Craic! Discover the best pubs around.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White,
                        modifier = Modifier.padding(16.dp)
                    )

                    // Animated logo
                    AnimatedLogo()

                    Spacer(modifier = Modifier.height(16.dp))

                    // Email input field
                    TextField(
                        value = username,
                        onValueChange = { username = it },
                        label = { Text("Email") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Password input field with visibility toggle
                    TextField(
                        value = password,
                        onValueChange = { password = it },
                        label = { Text("Password") },
                        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        modifier = Modifier.fillMaxWidth(),
                        trailingIcon = {
                            val icon = if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility
                            val description = if (passwordVisible) "Hide password" else "Show password"
                            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                Icon(imageVector = icon, contentDescription = description)
                            }
                        }
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Checkbox "Keep Me Logged In"
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Checkbox(
                            checked = keepMeLoggedIn,
                            onCheckedChange = { isChecked ->
                                keepMeLoggedIn = isChecked
                                sharedPreferences.edit().putBoolean("keep_me_logged_in", isChecked).apply()
                            }
                        )
                        Text("Keep Me Logged In", color = Color.White)
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = {
                            if (username.text.isNotEmpty() && password.text.isNotEmpty()) {
                                auth.signInWithEmailAndPassword(username.text, password.text)
                                    .addOnCompleteListener { task ->
                                        if (task.isSuccessful) {
                                            onCraicClick() // Navega para a HomeScreen
                                        } else {
                                            snackbarMessage = "Login failed: ${task.exception?.message}"
                                        }
                                    }
                            } else {
                                snackbarMessage = "Please fill in all fields."
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Log In")
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Button(
                        onClick = onRegisterClick,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Sign Up")
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Button(
                        onClick = { showForgotPasswordDialog = true },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Forgot Password?")
                    }
                }
            }

            if (showForgotPasswordDialog) {
                ForgotPasswordDialog(
                    onDismiss = { showForgotPasswordDialog = false },
                    onPasswordReset = { email ->
                        if (email.isNotEmpty()) {
                            auth.sendPasswordResetEmail(email)
                                .addOnCompleteListener { task ->
                                    if (task.isSuccessful) {
                                        snackbarMessage = "Password reset email sent to $email"
                                    } else {
                                        snackbarMessage = "Error: ${task.exception?.message}"
                                    }
                                    showForgotPasswordDialog = false
                                }
                        } else {
                            snackbarMessage = "Please enter a valid email."
                        }
                    }
                )
            }
        }
    }
}

@Composable
fun ForgotPasswordDialog(onDismiss: () -> Unit, onPasswordReset: (String) -> Unit) {
    var email by remember { mutableStateOf(TextFieldValue()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            Button(onClick = { onPasswordReset(email.text) }) {
                Text("Send Reset Email")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        },
        title = { Text("Forgot Password?") },
        text = {
            Column {
                Text("Enter your email to receive a password reset link.")
                Spacer(modifier = Modifier.height(8.dp))
                TextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Email") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    )
}

@Composable
fun VideoBackground() {
    val context = LocalContext.current
    val exoPlayer = remember {
        ExoPlayer.Builder(context).build().apply {
            val videoUri = Uri.parse("android.resource://${context.packageName}/raw/video2_background")
            val mediaItem = MediaItem.fromUri(videoUri)
            setMediaItem(mediaItem)
            repeatMode = ExoPlayer.REPEAT_MODE_ALL
            prepare()
            playWhenReady = true
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            exoPlayer.release()
        }
    }

    AndroidView(
        // I use the AndroidView Composable to embed a traditional Android View (PlayerView) into my Compose UI.
        factory = {
            // Here, I create an instance of PlayerView using the provided context.
            PlayerView(context).apply {
                // I set the ExoPlayer instance to the PlayerView so it can play the video.
                player = exoPlayer

                // I disable the default media controls because I don't want the user to interact with them directly.
                useController = false

                // I set the resize mode to RESIZE_MODE_FIXED_HEIGHT to ensure the video maintains its aspect ratio
                // while fitting within the available height.
                resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FIXED_HEIGHT

                // I configure the layout parameters to make the PlayerView fill the entire screen.
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, // Full width
                    ViewGroup.LayoutParams.MATCH_PARENT  // Full height
                )
            }
        },
        // I make sure the embedded view also fills the entire available space within the Compose layout.
        modifier = Modifier.fillMaxSize()
    )
}

@Composable
fun AnimatedLogo() {
    val infiniteTransition = rememberInfiniteTransition()
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ), label = ""
    )

    Image(
        painter = painterResource(id = R.drawable.logo),
        contentDescription = "Logo",
        modifier = Modifier
            .size(150.dp)
            .graphicsLayer(
                scaleX = scale,
                scaleY = scale
            )
    )
}

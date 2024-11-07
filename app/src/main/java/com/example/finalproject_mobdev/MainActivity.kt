package com.example.finalproject_mobdev

import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import com.example.finalproject_mobdev.ui.HomeScreen
import com.example.finalproject_mobdev.ui.theme.Finalproject_MOBDEVTheme
import com.google.firebase.auth.FirebaseAuth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.tooling.preview.Preview
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.ui.AspectRatioFrameLayout
import com.google.android.exoplayer2.ui.PlayerView
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.viewinterop.AndroidView
import android.view.ViewGroup

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            Finalproject_MOBDEVTheme {
                MainScreen() // Calls the MainScreen function
            }
        }
    }
}

@Composable
fun MainScreen() {
    // State variables to control which screen to show
    var showRegisterScreen by remember { mutableStateOf(false) }
    var showHomeScreen by remember { mutableStateOf(false) }
    var showForgotPassword by remember { mutableStateOf(false) }
    var emailForReset by remember { mutableStateOf(TextFieldValue("")) }

    // Get the current context and FirebaseAuth instance
    val context = LocalContext.current
    val auth = FirebaseAuth.getInstance()

    // Check if user is already logged in when MainScreen is first loaded
    LaunchedEffect(auth) {
        if (auth.currentUser != null) {
            showHomeScreen = true // User is already logged in, show HomeScreen directly
        }
    }

    // Display different screens based on state
    when {
        showRegisterScreen -> {
            // Show the registration screen with a callback to dismiss it
            RegisterScreen(
                onDismiss = { showRegisterScreen = false }, // Go back to login screen
                onRegisterSuccess = { showHomeScreen = true } // Navigate to HomeScreen after registration
            )
        }
        showHomeScreen -> {
            // Show the home screen after successful login or registration
            HomeScreen( onLogout = { showHomeScreen = false } // Go back to main screen after logout
            )
        }
        else -> {
            // Main container with background and conditional screens
            Box(modifier = Modifier.fillMaxSize()) {
                // Background video screen for the main screen
                VideoBackgroundScreen()

                // Check if Forgot Password screen should be shown
                if (showForgotPassword) {
                    // Forgot Password screen
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color(0x80000000)) // Semi-transparent background
                            .padding(32.dp)
                    ) {
                        Text(
                            "Reset Password",
                            style = MaterialTheme.typography.headlineSmall,
                            color = Color.White
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Field to enter the email for password reset
                        TextField(
                            value = emailForReset,
                            onValueChange = { emailForReset = it },
                            label = { Text("Enter your email", color = Color.White) },
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Button to send the password reset email
                        Button(
                            onClick = {
                                if (emailForReset.text.isNotEmpty()) {
                                    // Send password reset email using FirebaseAuth
                                    auth.sendPasswordResetEmail(emailForReset.text)
                                        .addOnCompleteListener { task ->
                                            if (task.isSuccessful) {
                                                // Successfully sent email
                                                Toast.makeText(
                                                    context,
                                                    "Reset email sent to ${emailForReset.text}",
                                                    Toast.LENGTH_SHORT
                                                ).show()
                                                // Hide the reset password screen
                                                showForgotPassword = false
                                            } else {
                                                // Error sending reset email
                                                Toast.makeText(
                                                    context,
                                                    "Error: ${task.exception?.message}",
                                                    Toast.LENGTH_LONG
                                                ).show()
                                                task.exception?.printStackTrace()
                                            }
                                        }
                                } else {
                                    // Warning if email field is empty
                                    Toast.makeText(context, "Please enter your email", Toast.LENGTH_SHORT).show()
                                }
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Send Reset Email")
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Button to go back to the main login screen
                        TextButton(onClick = { showForgotPassword = false }) {
                            Text("Back", color = Color.White)
                        }
                    }
                } else {
                    // Default login screen with "Forgot Password?" button
                    LoginScreen(
                        onRegisterClick = { showRegisterScreen = true },
                        onCraicClick = { showHomeScreen = true } // Navigate to HomeScreen after login
                    )

                    // "Forgot Password?" button on the main screen
                    Column(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        TextButton(
                            onClick = { showForgotPassword = true } // Show Forgot Password screen
                        ) {
                            Text("Forgot Password?")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun VideoBackgroundScreen() {
    // Configure the context and create an ExoPlayer instance
    val context = LocalContext.current
    val player = remember { ExoPlayer.Builder(context).build() }

    // Set up the video background using a local resource
    val videoUri = Uri.parse("android.resource://${context.packageName}/raw/video2_background")
    val mediaItem = MediaItem.fromUri(videoUri)
    player.setMediaItem(mediaItem)
    player.prepare()
    player.playWhenReady = true

    // Enable loop mode for continuous video playback
    player.repeatMode = Player.REPEAT_MODE_ALL

    // Release the player resources when the composable exits
    DisposableEffect(Unit) {
        onDispose {
            player.release()
        }
    }

    // Display the PlayerView as the video background
    AndroidView(factory = { PlayerView(context).apply {
        this.player = player
        this.resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FIXED_HEIGHT
        this.setUseController(false)
        layoutParams = ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        )
    } })
}

@Composable
fun LoginScreen(onRegisterClick: () -> Unit, onCraicClick: () -> Unit, modifier: Modifier = Modifier) {
    // Initialize FirebaseAuth and the current context
    val auth = FirebaseAuth.getInstance()
    val context = LocalContext.current

    // State variables for username and password input fields
    var username by remember { mutableStateOf(TextFieldValue()) }
    var password by remember { mutableStateOf(TextFieldValue()) }

    // Main layout container for the login screen
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Transparent),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Image(
                painter = painterResource(id = R.drawable.logo),
                contentDescription = "Logo",
                modifier = Modifier
                    .size(260.dp)
                    .padding(bottom = 16.dp)
                    .shadow(8.dp, shape = RoundedCornerShape(16.dp))
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Username input field
            TextField(
                value = username,
                onValueChange = { username = it },
                label = { Text("Username") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 32.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Password input field with hidden characters
            TextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Password") },
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 32.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Login button using Firebase Authentication
            Button(
                onClick = {
                    if (username.text.isNotEmpty() && password.text.isNotEmpty()) {
                        auth.signInWithEmailAndPassword(username.text, password.text)
                            .addOnCompleteListener { task ->
                                if (task.isSuccessful) {
                                    Toast.makeText(context, "Login successful", Toast.LENGTH_SHORT).show()
                                    onCraicClick() // Navigate to HomeScreen
                                } else {
                                    Toast.makeText(context, "Login failed: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                                }
                            }
                    } else {
                        Toast.makeText(context, "Please fill in all fields", Toast.LENGTH_SHORT).show()
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF008000)),
                modifier = Modifier.padding(top = 16.dp)
            ) {
                Text("Log In HERE")
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Button to open the registration screen
            Button(
                onClick = onRegisterClick,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF008000)),
                modifier = Modifier.padding(top = 16.dp)
            ) {
                Text("SIGN UP")
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    // Preview function to display the main screen in Android Studio Preview
    Finalproject_MOBDEVTheme {
        MainScreen()
    }
}

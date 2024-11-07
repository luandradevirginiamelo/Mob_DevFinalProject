package com.example.finalproject_mobdev

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.google.firebase.auth.FirebaseAuth

@Composable
fun RegisterScreen(
    onDismiss: () -> Unit,
    onRegisterSuccess: () -> Unit // Callback to navigate to HomeScreen after successful registration
) {
    val auth = FirebaseAuth.getInstance()
    val context = LocalContext.current

    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    // Function to validate password
    fun isPasswordValid(password: String): Boolean {
        val hasUpperCase = password.any { it.isUpperCase() }
        val hasDigit = password.any { it.isDigit() }
        val hasSpecialChar = password.any { !it.isLetterOrDigit() }
        return password.length >= 8 && hasUpperCase && hasDigit && hasSpecialChar
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Text("Register", style = MaterialTheme.typography.headlineMedium)

        Spacer(modifier = Modifier.height(16.dp))

        // Name input field
        TextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("Name") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Email input field
        TextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Password input field with hidden characters
        TextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password") },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Register button
        Button(
            onClick = {
                if (email.isNotEmpty() && password.isNotEmpty() && name.isNotEmpty()) {
                    if (isPasswordValid(password)) {
                        // Attempt to register the user with FirebaseAuth
                        auth.createUserWithEmailAndPassword(email, password)
                            .addOnCompleteListener { task ->
                                if (task.isSuccessful) {
                                    // Check if user is logged in after registration
                                    if (auth.currentUser != null) {
                                        Toast.makeText(context, "User registered successfully", Toast.LENGTH_SHORT).show()
                                        // Navigate to HomeScreen after successful registration
                                        onRegisterSuccess()
                                    } else {
                                        // In rare cases, Firebase might not log in the user immediately
                                        Toast.makeText(context, "User registered but not logged in. Please log in manually.", Toast.LENGTH_SHORT).show()
                                    }
                                } else {
                                    // Display error message if registration fails
                                    Toast.makeText(context, "Registration failed: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                                }
                            }
                    } else {
                        // Error message if password doesn't meet requirements
                        Toast.makeText(context, "Password must be at least 8 characters long, contain one uppercase letter, one digit, and one special character.", Toast.LENGTH_LONG).show()
                    }
                } else {
                    // Error message if any field is empty
                    Toast.makeText(context, "Please fill in all fields", Toast.LENGTH_SHORT).show()
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Register")
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Button to return to the login screen
        Button(onClick = onDismiss, modifier = Modifier.fillMaxWidth()) {
            Text("Go to Home Screen :)")
        }
    }
}

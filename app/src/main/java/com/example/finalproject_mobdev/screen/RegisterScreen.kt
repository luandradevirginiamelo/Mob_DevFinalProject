package com.example.finalproject_mobdev.screen

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.example.finalproject_mobdev.R
import com.example.finalproject_mobdev.ui.theme.Finalproject_MOBDEVTheme
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

@Composable
fun RegisterScreen(onDismiss: () -> Unit, onRegisterSuccess: () -> Unit) {
    val auth = FirebaseAuth.getInstance()
    val db = FirebaseFirestore.getInstance() // Firestore instance
    val context = LocalContext.current

    // Input fields
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var retypePassword by remember { mutableStateOf("") }

    // Password validation
    var passwordError by remember { mutableStateOf<String?>(null) }
    var retypePasswordError by remember { mutableStateOf<String?>(null) }

    // Snackbar state
    val snackbarHostState = remember { SnackbarHostState() }
    var showSnackbarMessage by remember { mutableStateOf<String?>(null) }

    // Visibility toggle states
    var passwordVisible by remember { mutableStateOf(false) }
    var retypePasswordVisible by remember { mutableStateOf(false) }

    // Trigger Snackbar when a message is set
    showSnackbarMessage?.let { message ->
        LaunchedEffect(message) {
            snackbarHostState.showSnackbar(message)
            showSnackbarMessage = null // Clear message after showing
        }
    }
    Finalproject_MOBDEVTheme{
    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) } // Attach the snackbar to the Scaffold
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Logo
            Image(
                painter = painterResource(id = R.drawable.logo),
                contentDescription = "Logo",
                modifier = Modifier
                    .size(150.dp)
                    .padding(bottom = 16.dp)
            )

            // Register title
            Text(
                text = "Register",
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.padding(8.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Name input
            TextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Name & Surname") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Email input
            TextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Password input with toggle visibility
            TextField(
                value = password,
                onValueChange = {
                    password = it
                    passwordError = validatePassword(password)
                },
                label = { Text("Password") },
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth(),
                isError = passwordError != null,
                trailingIcon = {
                    val icon = if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility
                    val description = if (passwordVisible) "Hide password" else "Show password"
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(imageVector = icon, contentDescription = description)
                    }
                }
            )

            // Password error message
            passwordError?.let { error ->
                Text(
                    text = error,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.align(Alignment.Start).padding(start = 4.dp)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Re-type password input with toggle visibility
            TextField(
                value = retypePassword,
                onValueChange = {
                    retypePassword = it
                    retypePasswordError =
                        if (retypePassword != password) "Passwords do not match" else null
                },
                label = { Text("Re-type Password") },
                visualTransformation = if (retypePasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth(),
                isError = retypePasswordError != null,
                trailingIcon = {
                    val icon = if (retypePasswordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility
                    val description = if (retypePasswordVisible) "Hide password" else "Show password"
                    IconButton(onClick = { retypePasswordVisible = !retypePasswordVisible }) {
                        Icon(imageVector = icon, contentDescription = description)
                    }
                }
            )

            // Re-type password error message
            retypePasswordError?.let { error ->
                Text(
                    text = error,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.align(Alignment.Start).padding(start = 4.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Register button
            Button(
                onClick = {
                    if (email.isNotEmpty() && password.isNotEmpty() && name.isNotEmpty() && retypePassword.isNotEmpty()) {
                        if (passwordError == null && retypePasswordError == null) {
                            // Register user in Firebase Authentication
                            auth.createUserWithEmailAndPassword(email, password)
                                .addOnCompleteListener { task ->
                                    if (task.isSuccessful) {
                                        // Get the UID of the registered user
                                        val uid = task.result?.user?.uid
                                        if (uid != null) {
                                            // Save the name to Firestore
                                            val userData = hashMapOf(
                                                "nameandsurname" to name
                                            )

                                            db.collection("user").document(uid)
                                                .set(userData)
                                                .addOnSuccessListener {
                                                    showSnackbarMessage = "User registered successfully"
                                                    onRegisterSuccess()
                                                }
                                                .addOnFailureListener { e ->
                                                    showSnackbarMessage = "Failed to save user data: ${e.message}"
                                                }
                                        }
                                    } else {
                                        val errorMessage = task.exception?.message ?: "Unknown error"
                                        showSnackbarMessage = "Registration failed: $errorMessage"
                                    }
                                }
                        } else {
                            showSnackbarMessage = "Please fix the errors"
                        }
                    } else {
                        showSnackbarMessage = "Please fill in all fields"
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Register")
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Back to Login button
            Button(
                onClick = onDismiss,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Back to Login")
            }
        }
    }
}
}
// Password validation function
fun validatePassword(password: String): String? {
    val hasUpperCase = password.any { it.isUpperCase() }
    val hasDigit = password.any { it.isDigit() }
    val hasSpecialChar = password.any { !it.isLetterOrDigit() }
    val isValidLength = password.length >= 8

    return when {
        !isValidLength -> "Password must be at least 8 characters long"
        !hasUpperCase -> "Password must contain at least one uppercase letter"
        !hasDigit -> "Password must contain at least one number"
        !hasSpecialChar -> "Password must contain at least one special character"
        else -> null // No error
    }
}

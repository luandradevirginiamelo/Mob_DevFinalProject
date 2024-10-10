package com.example.finalproject_mobdev

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.finalproject_mobdev.ui.theme.Finalproject_MOBDEVTheme
import com.google.firebase.auth.FirebaseAuth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.draw.shadow


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            Finalproject_MOBDEVTheme {
                var isRegistering by remember { mutableStateOf(false) }
                var isCraicScreen by remember { mutableStateOf(false) } // Nova variável de estado

                when {
                    isRegistering -> RegisterScreen(onRegisterSuccess = {
                        isRegistering = false // Fecha a tela de registro
                        isCraicScreen = true // Abre a tela WHERE IS THE CRAIC
                    })
                    isCraicScreen -> WhereIsTheCraicScreen(onBack = { isCraicScreen = false })
                    else -> LoginScreen(
                        onRegisterClick = { isRegistering = true },
                        onCraicClick = { isCraicScreen = true }
                    )
                }
            }
        }
    }
}

@Composable
fun LoginScreen(onRegisterClick: () -> Unit, onCraicClick: () -> Unit, modifier: Modifier = Modifier) {
    var username by remember { mutableStateOf(TextFieldValue()) }
    var password by remember { mutableStateOf(TextFieldValue()) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFADD8E6)), // Cor de fundo roxa clara
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
                    .shadow(8.dp, shape = RoundedCornerShape(16.dp)) // Adiciona sombra com bordas arredondadas
            )


            Spacer(modifier = Modifier.height(16.dp))



            Button(
                onClick = onRegisterClick,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50)),
                modifier = Modifier.padding(top = 16.dp)
            ) {
                Text("Log In HERE") // Texto do botão
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Botão de Sign In
            Button(
                onClick = {
                    // Ação para o botão Sign In
                    if (username.text.isNotEmpty() && password.text.isNotEmpty()) {
                        // Lógica de login
                        onCraicClick() // Navega para a tela WHERE IS THE CRAIC
                    } else {
                        // Exibir mensagem de erro para campos vazios
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4A4A4A)),
                modifier = Modifier.padding(top = 16.dp)
            ) {
                Text("Forgot your password?") // Texto do botão
            }

            // Mensagem abaixo do botão Sign In
            Text(
                text = "Don't have an account?",
                modifier = Modifier.padding(top = 8.dp),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Botão para navegar para a tela de registro
            Button(
                onClick = onRegisterClick,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4A4A4A)),
                modifier = Modifier.padding(top = 16.dp)
            ) {
                Text("SIGN UP") // Texto do botão
            }
        }
    }
}

@Composable
fun RegisterScreen(onRegisterSuccess: () -> Unit) {
    var name by remember { mutableStateOf(TextFieldValue()) }
    var email by remember { mutableStateOf(TextFieldValue()) }
    var password by remember { mutableStateOf(TextFieldValue()) }

    val auth = FirebaseAuth.getInstance()

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text("Register", style = MaterialTheme.typography.headlineLarge)

        Spacer(modifier = Modifier.height(16.dp))

        // Campo de texto para o nome
        TextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("Name") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Campo de texto para o e-mail
        TextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Campo de texto para a senha
        TextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password") },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Botão para registrar o usuário
        Button(onClick = {
            auth.createUserWithEmailAndPassword(email.text, password.text)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        onRegisterSuccess() // Navegar de volta ou mostrar mensagem de sucesso
                    } else {
                        // Tratar falha no registro (exibir mensagem de erro)
                    }
                }
        }) {
            Text("Register") // Texto do botão
        }
    }
}

// Nova tela WHERE IS THE CRAIC
@Composable
fun WhereIsTheCraicScreen(onBack: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFE0BBE4)), // Cor de fundo roxa clara
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "WHERE IS THE CRAIC",
                style = MaterialTheme.typography.headlineLarge,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Exemplo de conteúdo adicional
            Text(
                text = "This is where you can find information about craic!",
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Botão para voltar
            Button(onClick = onBack) {
                Text("Back to Login") // Botão para voltar à tela de login
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun LoginScreenPreview() {
    Finalproject_MOBDEVTheme {
        LoginScreen(onRegisterClick = {}, onCraicClick = {})
    }
}

package com.example.finalproject_mobdev.screen

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.finalproject_mobdev.ui.theme.Finalproject_MOBDEVTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            Finalproject_MOBDEVTheme {
                AppNavigation()
            }
        }
    }
}

@Composable
fun AppNavigation() {
    val navController: NavHostController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = "login" // Tela inicial
    ) {
        composable("login") {
            LoginScreen(
                onRegisterClick = { navController.navigate("register") },
                onCraicClick = { navController.navigate("home") }
            )
        }
        composable("register") {
            RegisterScreen(
                onDismiss = { navController.popBackStack() },
                onRegisterSuccess = { navController.navigate("home") }
            )
        }
        composable("home") {
            HomeScreen(
                onLogout = { navController.popBackStack("login", inclusive = true) }
            )
        }
    }
}

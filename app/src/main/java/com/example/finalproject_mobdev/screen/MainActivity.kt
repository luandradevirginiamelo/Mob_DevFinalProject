package com.example.finalproject_mobdev.screen

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
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
        startDestination = "login" // Define a tela inicial como Login
    ) {
        // Tela de Login
        composable("login") {
            LoginScreen(
                onRegisterClick = { navController.navigate("register") },
                onCraicClick = {
                    // Após login, remove as telas de registro e login do back stack
                    navController.navigate("home") {
                        popUpTo("login") { inclusive = true }
                    }
                }
            )
        }

        // Tela de Registro
        composable("register") {
            RegisterScreen(
                onDismiss = { navController.popBackStack() }, // Voltar para a tela anterior
                onRegisterSuccess = {
                    // Após registro, vá para o login
                    navController.navigate("login") {
                        popUpTo("register") { inclusive = true }
                    }
                }
            )
        }

        // Tela Principal (Home)
        composable("home") {
            HomeScreen(
                onLogout = { navController.navigate("login") { popUpTo(0) } },
                onNavigateToPubDetails = { pubId ->
                    navController.navigate("pubDetails/$pubId") // Navega para a tela de detalhes de pubs
                },
                onProfileClick = {
                    navController.navigate("profile") // Navega para a tela de perfil
                },
                onSettingsClick = {
                    navController.navigate("settings") // Navega para a tela de configurações
                },
                navController = navController // Passa o navController aqui
            )
        }



        // Tela de Detalhes do Pub
        composable(
            route = "pubDetails/{pubId}",
            arguments = listOf(navArgument("pubId") { type = NavType.StringType }) // Argumento: pubId
        ) { backStackEntry ->
            val pubId = backStackEntry.arguments?.getString("pubId") ?: ""
            PubDetailsScreen(
                pubId = pubId,
                onBack = { navController.popBackStack() },
                onNavigateToPubRate = {
                    navController.navigate("pubRate/$pubId") // Navega para a tela de avaliação do pub
                }
            )
        }

        // Tela de Avaliação do Pub (PubRateScreen)
        composable(
            route = "pubRate/{pubId}",
            arguments = listOf(navArgument("pubId") { type = NavType.StringType }) // Argumento: pubId
        ) { backStackEntry ->
            val pubId = backStackEntry.arguments?.getString("pubId") ?: ""
            PubRateScreen(
                pubId = pubId,
                navController = navController, // Passa o NavHostController aqui
                onBack = { navController.popBackStack() }
            )
        }

        // Tela de Upload de Fotos (PhotoUploadScreen)
        composable("photo_upload") { // Rota para a tela de upload de fotos
            PhotoUploadScreen(
                onBack = { navController.popBackStack() } // Volta para a tela anterior
            )
        }

        // Tela de Perfil
        composable("profile") {
            ProfileScreen(
                onBack = { navController.popBackStack() } // Volta para a tela anterior
            )
        }

        // Tela de Configurações
        composable("settings") {
            SettingsScreen(
                onBack = { navController.popBackStack() } // Volta para a tela anterior
            )
        }
    }
}

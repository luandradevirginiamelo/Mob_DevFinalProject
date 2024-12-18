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
                    navController.navigate("home") {
                        popUpTo("login") { inclusive = true }
                    }
                }
            )
        }

        // Tela de Registro
        composable("register") {
            Finalproject_MOBDEVTheme {
                RegisterScreen(
                    onDismiss = { navController.popBackStack() },
                    onRegisterSuccess = { navController.navigate("login") }
                )
            }
        }

        // Tela Principal (Home)
        composable("home") {
            Finalproject_MOBDEVTheme {
                HomeScreen(
                    onLogout = {
                        navController.navigate("login") {
                            popUpTo(0)
                        }
                    },
                    onNavigateToPubDetails = { pubId ->
                        navController.navigate("pubDetails/$pubId")
                    },
                    onProfileClick = {
                        navController.navigate("profile")
                    },
                    onSettingsClick = {
                        navController.navigate("settings")
                    },
                    navController = navController
                )
            }
        }

        // Tela de Detalhes do Pub
        composable(
            route = "pubDetails/{pubId}",
            arguments = listOf(navArgument("pubId") { type = NavType.StringType })
        ) { backStackEntry ->
            val pubId = backStackEntry.arguments?.getString("pubId") ?: ""
            PubDetailsScreen(
                pubId = pubId,
                onBack = { navController.popBackStack() },
                onNavigateToPubRate = {
                    navController.navigate("pubRate/$pubId")
                },
                onNavigateToGallery = {
                    navController.navigate("galleryScreen/$pubId") // Navega para a tela de Gallery Pictures
                }
            )
        }

        // Tela de Avaliação do Pub
        composable(
            route = "pubRate/{pubId}",
            arguments = listOf(navArgument("pubId") { type = NavType.StringType })
        ) { backStackEntry ->
            val pubId = backStackEntry.arguments?.getString("pubId") ?: ""
            PubRateScreen(
                pubId = pubId,
                navController = navController,
                onBack = { navController.popBackStack() }
            )
        }

               // Tela de Galeria (GalleryScreen)
        composable(
            route = "galleryScreen/{pubId}",
            arguments = listOf(navArgument("pubId") { type = NavType.StringType })
        ) { backStackEntry ->
            val pubId = backStackEntry.arguments?.getString("pubId") ?: ""
            GalleryScreen(
                pubId = pubId,
                onBack = { navController.popBackStack() } // Volta para a tela anterior
            )
        }

        composable("profile") {
            ProfileScreen(
                onBack = { navController.popBackStack() },
                onEditProfile = { /* TODO: Adicione lógica para editar o perfil */ },
                onLogout = { /* TODO: Adicione lógica para logout */ }
            )
        }

        // Tela de Configurações
        composable("settings") {
            SettingsScreen(
                onBack = { navController.popBackStack() }
            )
        }
    }
}


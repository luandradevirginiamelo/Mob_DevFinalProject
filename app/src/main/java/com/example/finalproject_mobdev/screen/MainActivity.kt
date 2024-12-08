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
        startDestination = "login"
    ) {
        // Login Screen
        composable("login") {
            LoginScreen(
                onRegisterClick = { navController.navigate("register") },
                onCraicClick = { navController.navigate("home") }
            )
        }

        // Register Screen
        composable("register") {
            RegisterScreen(
                onDismiss = { navController.popBackStack() },
                onRegisterSuccess = { navController.navigate("home") }
            )
        }

        // Home Screen
        composable("home") {
            HomeScreen(
                onLogout = { navController.navigate("login") { popUpTo(0) } },
                onNavigateToPubDetails = { pubId ->
                    navController.navigate("pubDetails/$pubId")
                },
                onProfileClick = {
                    navController.navigate("profile")
                },
                onSettingsClick = {
                    navController.navigate("settings")
                }
            )
        }

        // Pub Details Screen
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
                }
            )
        }

        // Pub Rate Screen
        composable(
            route = "pubRate/{pubId}",
            arguments = listOf(navArgument("pubId") { type = NavType.StringType })
        ) { backStackEntry ->
            val pubId = backStackEntry.arguments?.getString("pubId") ?: ""
            PubRateScreen(
                pubId = pubId,
                onBack = { navController.popBackStack() }
            )
        }

        // Profile Screen
        composable("profile") {
            ProfileScreen(
                onBack = { navController.popBackStack() }
            )
        }

        // Settings Screen
        composable("settings") {
            SettingsScreen(
                onBack = { navController.popBackStack() }
            )
        }
    }
}




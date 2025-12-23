package com.devin.giftguide.ui.main

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController

object Routes {
    const val Login = "login"
    const val Main = "main"
    const val Saved = "saved"
}

@Composable
fun SmartGiftApp(
    viewModel: MainViewModel,
    navController: NavHostController = rememberNavController()
) {
    NavHost(
        navController = navController,
        startDestination = Routes.Login
    ) {
        composable(Routes.Login) {
            LoginScreen(
                onContinue = {
                    navController.navigate(Routes.Main) {
                        popUpTo(Routes.Login) { inclusive = true }
                    }
                }
            )
        }

        composable(Routes.Main) {
            MainScreen(
                viewModel = viewModel,
                onOpenSaved = { navController.navigate(Routes.Saved) }
            )
        }

        composable(Routes.Saved) {
            SavedScreen(
                viewModel = viewModel,
                onBack = { navController.popBackStack() }
            )
        }
    }
}

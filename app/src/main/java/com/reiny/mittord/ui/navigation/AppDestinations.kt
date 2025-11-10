package com.reiny.mittord.ui.navigation

import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.reiny.mittord.ui.screens.home.MainScreen
import com.reiny.mittord.ui.screens.settings.SettingsScreen

object AppDestinations {
    const val MAIN = "main"
    const val SETTINGS = "settings"
}


@Composable
fun AppNavHost(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = AppDestinations.MAIN,
        enterTransition = {
            slideInHorizontally(initialOffsetX = { it })
        },
        exitTransition = {
            slideOutHorizontally(targetOffsetX = { -it })
        },
        popEnterTransition = {
            slideInHorizontally(initialOffsetX = { -it })
        },
        popExitTransition = {
            slideOutHorizontally(targetOffsetX = { it })
        }
    ) {
        composable(AppDestinations.MAIN) {
            MainScreen(onSettingsClick = { navController.navigate(AppDestinations.SETTINGS) })
        }
        composable(AppDestinations.SETTINGS) {
            SettingsScreen(onBackClick = { navController.popBackStack() })
        }
    }
}
package com.reiny.mittord.ui.navigation

import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.reiny.mittord.ui.screens.home.MainScreen
import com.reiny.mittord.ui.screens.settings.SettingsScreen
import com.reiny.mittord.ui.screens.wordDetail.WordDetailScreen
import com.reiny.mittord.ui.splash.SplashScreen

object AppDestinations {
    const val MAIN = "main"
    const val SETTINGS = "settings"
    const val WORD_DETAIL = "word/{wordId}"

    fun wordDetail(wordId: Long) = "word/$wordId"
}

@Composable
fun AppNavHost(
    navController: NavHostController,
    isDarkTheme: Boolean,
    onDarkThemeChange: (Boolean) -> Unit
) {
    var splashVisible by remember { mutableStateOf(true) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        NavHost(
            navController = navController,
            startDestination = AppDestinations.MAIN,
            enterTransition = {
                slideInHorizontally(tween(300), initialOffsetX = { it / 4 }) + fadeIn(tween(300))
            },
            exitTransition = {
                slideOutHorizontally(tween(300), targetOffsetX = { -it / 4 }) + fadeOut(tween(200))
            },
            popEnterTransition = {
                slideInHorizontally(tween(300), initialOffsetX = { -it / 4 }) + fadeIn(tween(300))
            },
            popExitTransition = {
                slideOutHorizontally(tween(300), targetOffsetX = { it / 4 }) + fadeOut(tween(200))
            }
        ) {
            composable(AppDestinations.MAIN) {
                MainScreen(
                    onSettingsClick = { navController.navigate(AppDestinations.SETTINGS) },
                    onWordClick = { wordId -> navController.navigate(AppDestinations.wordDetail(wordId)) }
                )
            }
            composable(AppDestinations.SETTINGS) {
                SettingsScreen(
                    onBackClick = { navController.popBackStack() },
                    isDarkTheme = isDarkTheme,
                    onDarkThemeChange = onDarkThemeChange
                )
            }
            composable(
                route = AppDestinations.WORD_DETAIL,
                arguments = listOf(navArgument("wordId") { type = NavType.LongType })
            ) {
                WordDetailScreen(onBack = { navController.popBackStack() })
            }
        }

        if (splashVisible) {
            SplashScreen(onFinished = { splashVisible = false })
        }
    }
}

package com.reiny.mittord

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalView
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.WindowCompat
import androidx.navigation.compose.rememberNavController
import com.reiny.mittord.ui.navigation.AppNavHost
import com.reiny.mittord.ui.theme.MittOrdTheme
import com.reiny.mittord.util.AppPreferences
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject lateinit var appPrefs: AppPreferences

    private var processTextWord by mutableStateOf<String?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        installSplashScreen()
        enableEdgeToEdge()
        processTextWord = intent.extractProcessText()

        setContent {
            val systemDark = isSystemInDarkTheme()
            var themeOverride by remember { mutableStateOf(appPrefs.darkThemeOverride) }
            val isDarkTheme = themeOverride ?: systemDark
            MittOrdTheme(darkTheme = isDarkTheme) {
                val view = LocalView.current
                if (!view.isInEditMode) {
                    SideEffect {
                        val window = (view.context as Activity).window
                        WindowCompat.getInsetsController(window, view)
                            .isAppearanceLightStatusBars = !isDarkTheme
                    }
                }
                val navController = rememberNavController()
                AppNavHost(
                    navController = navController,
                    isDarkTheme = isDarkTheme,
                    onDarkThemeChange = {
                        themeOverride = it
                        appPrefs.darkThemeOverride = it
                    },
                    processTextWord = processTextWord,
                    onProcessTextConsumed = { processTextWord = null }
                )
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        val word = intent.extractProcessText()
        if (word != null) processTextWord = word
    }

    private fun Intent.extractProcessText(): String? =
        getStringExtra(Intent.EXTRA_PROCESS_TEXT)?.trim()?.takeIf { it.isNotBlank() }
}

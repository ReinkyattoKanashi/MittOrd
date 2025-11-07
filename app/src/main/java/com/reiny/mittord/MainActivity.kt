package com.reiny.mittord

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.navigation.compose.rememberNavController
import com.reiny.mittord.ui.navigation.AppNavHost
import com.reiny.mittord.ui.theme.MittOrdTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            MittOrdTheme {
                val navController = rememberNavController()
                AppNavHost(navController = navController)
            }
        }
    }
}
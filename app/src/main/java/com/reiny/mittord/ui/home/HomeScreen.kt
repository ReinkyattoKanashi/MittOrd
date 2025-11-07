package com.reiny.mittord.ui.home

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.reiny.mittord.ui.home.components.AppLogoToolbar
import com.reiny.mittord.ui.home.components.EmptyListPlaceholder
import com.reiny.mittord.ui.home.components.FloatingBottomNavigation

@Composable
fun MainScreen(onSettingsClick: () -> Unit) {
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            Column(
                Modifier.padding(
                    top = WindowInsets.statusBars.asPaddingValues().calculateTopPadding() + 24.dp
                ),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                AppLogoToolbar(Modifier.fillMaxWidth())
            }
        }
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding)) {
            EmptyListPlaceholder(Modifier.align(Alignment.Center).padding(bottom = 70.dp))
            FloatingBottomNavigation(
                expanded = false,
                onExpandToggle = {},
                onLeftClick = {},
                onRightClick = onSettingsClick
            )
        }
    }
}

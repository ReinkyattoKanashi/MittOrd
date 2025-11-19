package com.reiny.mittord.ui.screens.home

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.reiny.mittord.ui.screens.home.components.AppLogoToolbar
import com.reiny.mittord.ui.screens.home.components.BottomNavState
import com.reiny.mittord.ui.screens.home.components.EmptyListPlaceholder
import com.reiny.mittord.ui.screens.home.components.FloatingBottomNavigationDefault

@Composable
fun MainScreen(onSettingsClick: () -> Unit) {
    var state by remember { mutableStateOf<BottomNavState>(BottomNavState.Default) }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            AppLogoToolbar(
                Modifier
                    .fillMaxWidth()
                    .padding(
                        top = WindowInsets.statusBars.asPaddingValues()
                            .calculateTopPadding() + 24.dp
                    )
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
        ) {
            EmptyListPlaceholder(Modifier.align(Alignment.Center))
            FloatingBottomNavigationDefault(
                state = state,
                onLeftClick = {
                    if (state == BottomNavState.Default) {
                        state = BottomNavState.Search
                    }
                },
                onMiddleClick = {
                    if (state == BottomNavState.Search) {
                        state = BottomNavState.Default
                    } else {
                        // open Add Word screen
                        if (state == BottomNavState.Default) {
                            state = BottomNavState.Search
                        }
                    }
                },
                onRightClick = {
                    onSettingsClick()
                },
                modifier = Modifier.align(Alignment.BottomCenter)
            )
        }
    }
}

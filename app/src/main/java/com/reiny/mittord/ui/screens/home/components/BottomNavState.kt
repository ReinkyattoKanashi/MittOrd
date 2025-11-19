package com.reiny.mittord.ui.screens.home.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview

sealed interface BottomNavState {

    @Composable
    fun Show(
        onChangeState: (BottomNavState) -> Unit,
        onLeftClick: () -> Unit,
        onRightClick: () -> Unit
    )

    // ---------------------- Default ----------------------
    object Default : BottomNavState {
        @Composable
        override fun Show(
            onChangeState: (BottomNavState) -> Unit,
            onLeftClick: () -> Unit,
            onRightClick: () -> Unit
        ) {

        }
    }

    // ---------------------- Search ----------------------
    object Search : BottomNavState {
        @Composable
        override fun Show(
            onChangeState: (BottomNavState) -> Unit,
            onLeftClick: () -> Unit,
            onRightClick: () -> Unit
        ) {

        }
    }

    // ---------------------- AddWord ----------------------
    object AddWord : BottomNavState {
        @Composable
        override fun Show(
            onChangeState: (BottomNavState) -> Unit,
            onLeftClick: () -> Unit,
            onRightClick: () -> Unit
        ) {
            // todo
        }
    }
}

@Preview
@Composable
fun Preview() {
//    BottomNavState.Default.Show {  }
}

package com.reiny.mittord.ui.screens.addWord

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.reiny.mittord.ui.screens.addWord.components.AddWordContent

@Composable
fun NewWordScreen(
    onBackClick: () -> Unit
) {

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {

            AddWordContent(onBackClick)
        }
    }
}
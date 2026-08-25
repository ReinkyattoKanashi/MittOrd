package com.reiny.mittord.ui.screens.home.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.reiny.mittord.ui.screens.home.WordListItem
import com.reiny.mittord.ui.theme.MittOrdTheme
import com.reiny.mittord.ui.theme.Theme
import com.reiny.mittord.ui.theme.typography
import com.reiny.mittord.util.AppConstants

@Composable
fun WordItem(item: WordListItem, onClick: () -> Unit) {
    val onSurface = MaterialTheme.colorScheme.onSurface

    Surface(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 5.dp),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 2.dp,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = item.wordFlag ?: AppConstants.DEFAULT_FLAG_EMOJI,
                style = Theme.typography.h2,
                modifier = Modifier.padding(end = 10.dp),
                color = if (item.wordFlag == null) onSurface.copy(alpha = 0.25f) else Color.Unspecified
            )
            Column {
                Text(text = item.word, style = Theme.typography.h2)
                if (item.translation != null) {
                    Spacer(Modifier.height(3.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (item.translationFlag != null) {
                            Text(text = item.translationFlag, style = Theme.typography.caption)
                            Spacer(Modifier.width(5.dp))
                        }
                        Text(
                            text = item.translation,
                            style = Theme.typography.caption,
                            color = onSurface.copy(alpha = 0.55f)
                        )
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun WordItemPreview() {
    MittOrdTheme {
        Column(Modifier.padding(16.dp)) {
            WordItem(
                item = WordListItem(
                    id = 1,
                    word = "hund",
                    wordFlag = "🇳🇴",
                    translation = "собака",
                    translationFlag = "🇷🇺"
                ),
                onClick = {}
            )
            WordItem(
                item = WordListItem(
                    id = 2,
                    word = "katt",
                    wordFlag = null,
                    translation = null,
                    translationFlag = null
                ),
                onClick = {}
            )
        }
    }
}

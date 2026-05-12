package com.reiny.mittord.ui.screens.home.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.reiny.mittord.R
import com.reiny.mittord.domain.util.flagForCode
import com.reiny.mittord.ui.theme.Theme
import com.reiny.mittord.ui.theme.typography

@Composable
fun LanguageFlagButton(
    languageCode: String?,
    isAuto: Boolean = false,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val flag = languageCode?.let { flagForCode(it) }

    val primaryColor = MaterialTheme.colorScheme.primary
    val onSurfaceColor = MaterialTheme.colorScheme.onSurface

    Box(
        modifier = modifier
            .size(44.dp)
            .clip(RoundedCornerShape(10.dp))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        if (flag != null) {
            Text(
                text = flag,
                style = Theme.typography.h2
            )
        } else {
            Icon(
                painter = painterResource(R.drawable.ic_globe),
                contentDescription = null,
                modifier = Modifier.size(22.dp),
                tint = onSurfaceColor.copy(alpha = 0.5f)
            )
        }

        if (isAuto) {
            Icon(
                painter = painterResource(R.drawable.ic_sparkles),
                contentDescription = null,
                modifier = Modifier
                    .size(13.dp)
                    .align(Alignment.BottomEnd)
                    .offset((-2).dp, (-2).dp),
                tint = primaryColor
            )
        }
    }
}

@Composable
fun TranslateButton(
    onClick: () -> Unit,
    isLoading: Boolean = false,
    modifier: Modifier = Modifier
) {
    IconButton(onClick = onClick, modifier = modifier) {
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.size(18.dp),
                strokeWidth = 2.dp,
                color = MaterialTheme.colorScheme.primary
            )
        } else {
            Icon(
                painter = painterResource(R.drawable.ic_translate),
                contentDescription = null,
                modifier = Modifier.size(22.dp),
                tint = MaterialTheme.colorScheme.primary
            )
        }
    }
}

package com.reiny.mittord.ui.screens.home

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.reiny.mittord.domain.model.LANGUAGES
import com.reiny.mittord.domain.model.Language
import com.reiny.mittord.ui.animations.NavBarAnimation
import com.reiny.mittord.ui.screens.home.addword.AddWordPanel
import com.reiny.mittord.ui.screens.home.components.AddWordState
import com.reiny.mittord.ui.screens.home.components.BottomNavState
import com.reiny.mittord.ui.screens.home.components.NavCenterButton
import com.reiny.mittord.ui.screens.home.components.NavIconsRow
import com.reiny.mittord.ui.screens.home.components.NavSearchField
import com.reiny.mittord.ui.screens.home.components.SearchState
import com.reiny.mittord.ui.screens.home.components.rememberBottomNavTint
import com.reiny.mittord.ui.theme.MittOrdTheme
import com.reiny.mittord.util.MeasureAvailableWidth
import com.reiny.mittord.util.height
import com.reiny.mittord.util.paddingLayout

private val COLLAPSED_HEIGHT = 60.dp
private val CENTER_BUTTON_SIZE = 70.dp
private val PANEL_CORNER = RoundedCornerShape(30.dp)

/** Share of the screen width the bar occupies. */
private const val BAR_WIDTH_FRACTION = 0.85f

/** Room the expanded panel leaves above itself for the center button to sit in. */
private val PANEL_TOP_INSET = 35.dp

/**
 * Bottom bar with three states: plain navigation, an inline search field, and the
 * expanded add-word panel.
 *
 * Every animated value below is handed to its child as a lambda rather than a value.
 * That keeps the reads in the layout and draw phases, so a state change costs one
 * recomposition and the following 400 ms of animation cost none. Passing any of them
 * as a plain value re-introduces a recomposition per frame - verify with the Layout
 * Inspector after touching this file.
 */
@Composable
fun FloatingBottomNavigationDefault(
    state: BottomNavState,
    onLeftClick: () -> Unit,
    onMiddleClick: () -> Unit,
    onRightClick: () -> Unit,
    addWord: AddWordState,
    search: SearchState,
    orderedLanguages: List<Language>,
    addRecentLanguage: (String) -> Unit,
    modifier: Modifier = Modifier,
    parentHeight: Dp
) {
    val backgroundBoxHeight by animateDpAsState(
        targetValue = if (state == BottomNavState.AddWord) parentHeight else COLLAPSED_HEIGHT,
        animationSpec = NavBarAnimation.slideDpSpec,
        label = "backgroundBoxHeight"
    )
    val boxTopPadding by animateDpAsState(
        targetValue = if (state == BottomNavState.AddWord) PANEL_TOP_INSET else 0.dp,
        animationSpec = NavBarAnimation.slideDpSpec,
        label = "boxTopPadding"
    )
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(bottom = 24.dp)
            .height { backgroundBoxHeight + 10.dp },
        contentAlignment = Alignment.BottomCenter
    ) {
        var targetWidth by remember { mutableStateOf(CENTER_BUTTON_SIZE) }
        MeasureAvailableWidth(fraction = BAR_WIDTH_FRACTION) { width -> targetWidth = width }

        val width by animateDpAsState(
            targetValue = if (state == BottomNavState.Search) targetWidth else CENTER_BUTTON_SIZE,
            animationSpec = NavBarAnimation.defaultDpTween,
            label = "centerWidth"
        )
        val bias by animateFloatAsState(
            targetValue = if (state == BottomNavState.Search) 1f else 0f,
            animationSpec = NavBarAnimation.tweenFloatSpec,
            label = "centerBias"
        )
        val rotation by animateFloatAsState(
            targetValue = if (state == BottomNavState.Search) 45f else 0f,
            animationSpec = NavBarAnimation.tweenFloatSpec,
            label = "centerRotation"
        )
        val offsetY by animateDpAsState(
            targetValue = if (state == BottomNavState.AddWord) 5.dp else 0.dp,
            animationSpec = NavBarAnimation.slideDpSpec,
            label = "centerOffsetY"
        )
        val addIconAlpha by animateFloatAsState(
            targetValue = if (state == BottomNavState.AddWord) 0f else 1f,
            animationSpec = NavBarAnimation.tweenFloatSpec,
            label = "addIconAlpha"
        )
        val iconsTint = rememberBottomNavTint(state)

        PanelSurface(
            height = { backgroundBoxHeight },
            topPadding = { boxTopPadding },
            bottomPadding = { offsetY }
        ) {
            AddWordPanel(
                isExpanded = state == BottomNavState.AddWord,
                addWord = addWord,
                orderedLanguages = orderedLanguages,
                addRecentLanguage = addRecentLanguage
            )
        }

        Box(
            modifier = Modifier
                .fillMaxWidth(BAR_WIDTH_FRACTION)
                .height { backgroundBoxHeight + 10.dp }
        ) {
            NavCenterButton(
                width = { width },
                rotation = { rotation },
                bias = { bias },
                addIconAlpha = { addIconAlpha },
                onMiddleClick = onMiddleClick
            )

            Box(
                Modifier
                    .fillMaxSize()
                    .align(Alignment.TopCenter),
                contentAlignment = Alignment.BottomCenter
            ) {
                NavIconsRow(
                    state = { state },
                    iconsTint = { iconsTint.value },
                    onLeftClick = onLeftClick,
                    onRightClick = onRightClick
                )

                NavSearchField(
                    state = { state },
                    search = search
                )
            }
        }
    }
}

/** Rounded card behind the bar; grows to full height when the add-word panel opens. */
@Composable
private fun BoxScope.PanelSurface(
    height: () -> Dp,
    topPadding: () -> Dp,
    bottomPadding: () -> Dp,
    content: @Composable () -> Unit
) {
    Surface(
        shape = PANEL_CORNER,
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 4.dp,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        modifier = Modifier
            .fillMaxWidth(BAR_WIDTH_FRACTION)
            .align(Alignment.Center)
            .paddingLayout(top = { topPadding() }, bottom = { bottomPadding() })
            .height { height() }
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = PANEL_TOP_INSET)
        ) {
            content()
        }
    }
}

private fun previewAddWord(
    wordInput: String = "",
    wordLanguageCode: String? = null
) = AddWordState(
    wordInput = wordInput,
    translationInput = "",
    wordLanguageCode = wordLanguageCode,
    translationLanguageCode = null,
    wordLanguageIsAuto = true,
    translationLanguageIsAuto = true,
    isTranslating = false,
    onWordChange = {}, onTranslationChange = {},
    onWordLanguageSelected = {}, onTranslationLanguageSelected = {},
    onTranslateTranslation = {},
    onAddWord = {}
)

@Preview
@Composable
private fun PreviewBottomNav() {
    MittOrdTheme {
        FloatingBottomNavigationDefault(
            state = BottomNavState.Default,
            onLeftClick = {}, onMiddleClick = {}, onRightClick = {},
            addWord = previewAddWord(),
            search = SearchState("", {}, {}),
            orderedLanguages = LANGUAGES,
            addRecentLanguage = {},
            parentHeight = 700.dp
        )
    }
}

@Preview
@Composable
private fun PreviewBottomNavSearch() {
    MittOrdTheme {
        FloatingBottomNavigationDefault(
            state = BottomNavState.Search,
            onLeftClick = {}, onMiddleClick = {}, onRightClick = {},
            addWord = previewAddWord(),
            search = SearchState("hund", {}, {}),
            orderedLanguages = LANGUAGES,
            addRecentLanguage = {},
            parentHeight = 700.dp
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF000000)
@Composable
private fun PreviewBottomNavAddWord() {
    MittOrdTheme {
        FloatingBottomNavigationDefault(
            state = BottomNavState.AddWord,
            onLeftClick = {}, onMiddleClick = {}, onRightClick = {},
            addWord = previewAddWord(wordInput = "hund", wordLanguageCode = "no"),
            search = SearchState("", {}, {}),
            orderedLanguages = LANGUAGES,
            addRecentLanguage = {},
            parentHeight = 490.dp
        )
    }
}

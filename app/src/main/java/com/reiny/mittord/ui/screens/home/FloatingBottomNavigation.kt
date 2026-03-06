package com.reiny.mittord.ui.screens.home

import androidx.compose.animation.Animatable
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationVector4D
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import kotlinx.coroutines.delay
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.layout
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.offset
import com.reiny.mittord.R
import com.reiny.mittord.ui.animations.NavBarAnimation
import com.reiny.mittord.ui.screens.home.components.AddWordState
import com.reiny.mittord.ui.screens.home.components.BottomNavState
import com.reiny.mittord.ui.screens.home.components.SearchState
import com.reiny.mittord.ui.screens.home.components.PrimaryTextField
import com.reiny.mittord.ui.screens.home.components.RoundedPrimaryButton
import com.reiny.mittord.ui.screens.home.components.WordInputField
import com.reiny.mittord.ui.screens.settings.Language
import com.reiny.mittord.ui.screens.settings.LanguagePickerSheet
import com.reiny.mittord.ui.screens.settings.LANGUAGES
import com.reiny.mittord.ui.screens.wordDetail.LANG_NAME_TO_BCP47
import com.reiny.mittord.ui.screens.wordDetail.flagForCode
import com.reiny.mittord.ui.screens.wordDetail.langNameForCode
import com.reiny.mittord.ui.theme.MittOrdTheme
import com.reiny.mittord.ui.theme.Theme
import com.reiny.mittord.ui.theme.colors
import com.reiny.mittord.ui.theme.typography
import com.reiny.mittord.util.AppConstants
import com.reiny.mittord.util.height
import com.reiny.mittord.util.paddingLayout
import com.reiny.mittord.util.size

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
        targetValue = if (state == BottomNavState.AddWord) parentHeight else 60.dp,
        animationSpec = NavBarAnimation.slideDpSpec,
        label = "backgroundBoxHeight"
    )
    val boxTopPadding by animateDpAsState(
        targetValue = if (state == BottomNavState.AddWord) 35.dp else 0.dp,
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

        var targetWidth by remember { mutableStateOf(70.dp) }
        MeasureAvailableWidth(fraction = 0.85f) { width ->
            targetWidth = width
        }
        val width by animateDpAsState(
            targetValue =
                when (state) {
                    BottomNavState.Default -> 70.dp
                    BottomNavState.Search -> targetWidth
                    BottomNavState.AddWord -> 70.dp
                },
            animationSpec = NavBarAnimation.defaultDpTween,
            label = "centerWidth"
        )
        val bias by animateFloatAsState(
            targetValue =
                when (state) {
                    BottomNavState.Default -> 0f
                    BottomNavState.Search -> 1f
                    BottomNavState.AddWord -> 0f
                },
            animationSpec = NavBarAnimation.tweenFloatSpec,
            label = "centerBias"
        )
        val rotation by animateFloatAsState(
            targetValue = when (state) {
                BottomNavState.Default -> 0f
                BottomNavState.Search -> 45f
                BottomNavState.AddWord -> 0f
            },
            animationSpec = NavBarAnimation.tweenFloatSpec,
            label = "centerRotation"
        )
        val offsetY by animateDpAsState(
            targetValue = when (state) {
                BottomNavState.Default -> 0.dp
                BottomNavState.Search -> 0.dp
                BottomNavState.AddWord -> 5.dp
            },
            animationSpec = NavBarAnimation.slideDpSpec,
            label = "centerOffsetY"
        )
        val addIconAlpha by animateFloatAsState(
            targetValue = if (state == BottomNavState.AddWord) 0f else 1f,
            animationSpec = NavBarAnimation.tweenFloatSpec,
            label = "addIconAlpha"
        )
        val iconsTint = rememberBottomNavTint(state)

        BackgroundSurface(
            state = { state },
            height = { backgroundBoxHeight },
            topPadding = { boxTopPadding },
            bottomPadding = { offsetY },
            addWord = addWord,
            orderedLanguages = orderedLanguages,
            addRecentLanguage = addRecentLanguage
        )
        Box(
            modifier = Modifier
                .fillMaxWidth(0.85f)
                .height { backgroundBoxHeight + 10.dp }
        ) {
            AnimatedCenterButton(
                width = { width },
                rotation = { rotation },
                bias = { bias },
                addIconAlpha = { addIconAlpha },
                onMiddleClick = onMiddleClick
            )

            Box(
                Modifier
                    .fillMaxSize()
                    .align(Alignment.TopCenter), contentAlignment = Alignment.BottomCenter
            ) {
                NavIconsRow(
                    state = { state },
                    iconsTint = { iconsTint.value },
                    onLeftClick = onLeftClick,
                    onRightClick = onRightClick
                )

                StaticSearchField(
                    state = { state },
                    search = search
                )
            }

        }
    }
}

@Composable
private fun BoxScope.BackgroundSurface(
    state: () -> BottomNavState,
    height: () -> Dp,
    topPadding: () -> Dp,
    bottomPadding: () -> Dp,
    addWord: AddWordState,
    orderedLanguages: List<Language>,
    addRecentLanguage: (String) -> Unit
) {
    Surface(
        shape = RoundedCornerShape(30.dp),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 4.dp,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        modifier = Modifier
            .fillMaxWidth(0.85f)
            .align(Alignment.Center)
            .paddingLayout(top = { topPadding() }, bottom = { bottomPadding() })
            .height { height() }
            .clip(RoundedCornerShape(30.dp))
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 35.dp)
        ) {
            AddWordContent(
                isExpanded = state() == BottomNavState.AddWord,
                addWord = addWord,
                orderedLanguages = orderedLanguages,
                addRecentLanguage = addRecentLanguage
            )
        }
    }
}

@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
private fun AddWordContent(
    isExpanded: Boolean,
    addWord: AddWordState,
    orderedLanguages: List<Language>,
    addRecentLanguage: (String) -> Unit
) {
    val wordFocusRequester = remember { FocusRequester() }
    var showLanguagePicker by remember { mutableStateOf(false) }
    var showTranslationPicker by remember { mutableStateOf(false) }

    val titleAddWord = stringResource(R.string.nav_add_word_title)
    val placeholderWord = stringResource(R.string.placeholder_word)
    val placeholderTranslation = stringResource(R.string.placeholder_translation)
    val btnAdd = stringResource(R.string.btn_add)
    val pickerWordLanguage = stringResource(R.string.picker_word_language)
    val pickerTranslationLanguage = stringResource(R.string.picker_translation_language)

    LaunchedEffect(isExpanded) {
        if (isExpanded) {
            delay(420)
            try { wordFocusRequester.requestFocus() } catch (_: Exception) {}
        }
    }

    Column(
        modifier = Modifier.fillMaxHeight(),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        AnimatedVisibility(
            visible = isExpanded,
            enter = fadeIn(tween(280, 150)) + slideInVertically(tween(280, 150)) { it / 3 },
            exit = fadeOut(tween(80))
        ) {
            Text(
                titleAddWord,
                modifier = Modifier.padding(16.dp),
                color = MaterialTheme.colorScheme.onSurface,
                style = Theme.typography.h1
            )
        }

        AnimatedVisibility(
            visible = isExpanded,
            enter = fadeIn(tween(280, 260)) + slideInVertically(tween(280, 260)) { it / 2 },
            exit = fadeOut(tween(80))
        ) {
            WordInputField(
                modifier = Modifier
                    .padding(start = 24.dp, end = 24.dp)
                    .focusRequester(wordFocusRequester),
                value = addWord.wordInput,
                onValueChange = addWord.onWordChange,
                placeholder = placeholderWord,
                flagEmoji = flagForCode(addWord.wordLanguageCode) ?: AppConstants.DEFAULT_FLAG_EMOJI,
                isAutoLanguage = addWord.wordLanguageIsAuto,
                onIconClick = { showLanguagePicker = true }
            )
        }

        AnimatedVisibility(
            visible = isExpanded,
            enter = fadeIn(tween(280, 370)) + slideInVertically(tween(280, 370)) { it / 2 },
            exit = fadeOut(tween(80))
        ) {
            WordInputField(
                modifier = Modifier.padding(start = 24.dp, end = 24.dp, top = 12.dp),
                value = addWord.translationInput,
                onValueChange = addWord.onTranslationChange,
                placeholder = placeholderTranslation,
                flagEmoji = flagForCode(addWord.translationLanguageCode) ?: AppConstants.DEFAULT_FLAG_EMOJI,
                isAutoLanguage = addWord.translationLanguageIsAuto,
                onIconClick = { showTranslationPicker = true }
            )
        }

        Spacer(modifier = Modifier.weight(1f))

        AnimatedVisibility(
            visible = isExpanded,
            enter = fadeIn(tween(250, 460)),
            exit = fadeOut(tween(80))
        ) {
            RoundedPrimaryButton(
                modifier = Modifier.padding(bottom = 2.dp),
                text = btnAdd,
                onClick = addWord.onAddWord,
                enabled = addWord.wordInput.isNotBlank()
            )
        }
    }

    if (showLanguagePicker) {
        LanguagePickerSheet(
            title = pickerWordLanguage,
            selected = langNameForCode(addWord.wordLanguageCode) ?: "",
            isAutoSelected = addWord.wordLanguageIsAuto,
            showAutoOption = true,
            orderedLanguages = orderedLanguages,
            onSelect = { name ->
                name?.let { addRecentLanguage(it) }
                addWord.onWordLanguageSelected(name?.let { LANG_NAME_TO_BCP47[it] ?: it })
                showLanguagePicker = false
            },
            onDismiss = { showLanguagePicker = false }
        )
    }

    if (showTranslationPicker) {
        LanguagePickerSheet(
            title = pickerTranslationLanguage,
            selected = langNameForCode(addWord.translationLanguageCode) ?: "",
            isAutoSelected = addWord.translationLanguageIsAuto,
            showAutoOption = true,
            orderedLanguages = orderedLanguages,
            onSelect = { name ->
                name?.let { addRecentLanguage(it) }
                addWord.onTranslationLanguageSelected(name?.let { LANG_NAME_TO_BCP47[it] ?: it })
                showTranslationPicker = false
            },
            onDismiss = { showTranslationPicker = false }
        )
    }
}

@Composable
fun BoxScope.AnimatedCenterButton(
    width: () -> Dp,
    rotation: () -> Float,
    bias: () -> Float,
    addIconAlpha: () -> Float,
    onMiddleClick: () -> Unit
) {
    val cdAdd = stringResource(R.string.btn_add)
    val cdCollapse = stringResource(R.string.cd_collapse)
    Box(
        modifier = Modifier
            .height(70.dp)
            .size { width() }
            .align(Alignment.TopCenter)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.primary)
            .clickable { onMiddleClick() },
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Default.Add,
            contentDescription = cdAdd,
            modifier = Modifier
                .size(36.dp)
                .graphicsLayer {
                    alpha = addIconAlpha()
                    rotationZ = rotation()
                    val max = width().roundToPx() / 2 - size.width
                    translationX = max * bias()
                },
            tint = MaterialTheme.colorScheme.onPrimary
        )
        Icon(
            imageVector = Icons.Default.KeyboardArrowDown,
            contentDescription = cdCollapse,
            modifier = Modifier
                .size(36.dp)
                .graphicsLayer { alpha = 1f - addIconAlpha() },
            tint = MaterialTheme.colorScheme.onPrimary
        )
    }
}

@Composable
private fun NavIconsRow(
    state: () -> BottomNavState,
    iconsTint: () -> Color,
    onLeftClick: () -> Unit,
    onRightClick: () -> Unit
) {
    val cdSearch = stringResource(R.string.cd_search)
    val cdProfile = stringResource(R.string.cd_profile)

    val animatedPadding by animateDpAsState(
        if (state() == BottomNavState.Default) 32.dp else 20.dp, NavBarAnimation.defaultDpTween
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height { 70.dp }
            .layout { measurable, constraints ->
                val paddingPx = animatedPadding.roundToPx()
                val newConstraints = constraints.offset(
                    horizontal = -paddingPx * 2
                )
                val placeable = measurable.measure(newConstraints)
                layout(
                    width = placeable.width + paddingPx * 2, height = placeable.height
                ) {
                    placeable.placeRelative(paddingPx, 0)
                }
            },
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        AnimatedVisibility(
            visible = state() != BottomNavState.AddWord, enter = fadeIn(), exit = fadeOut()
        ) {
            IconButton(onClick = onLeftClick) {
                Icon(
                    Icons.Default.Search, cdSearch, modifier = Modifier
                        .size(34.dp)
                        .graphicsLayer {
                            colorFilter = ColorFilter.tint(iconsTint())
                        })
            }
        }

        Spacer(Modifier.weight(1f))

        Spacer(Modifier.width(16.dp))

        AnimatedVisibility(
            visible = state() == BottomNavState.Default, enter = fadeIn(), exit = fadeOut()
        ) {
            IconButton(onClick = onRightClick) {
                Icon(
                    Icons.Default.Person, cdProfile, modifier = Modifier.size(34.dp)
                )
            }
        }
    }
}

@Composable
private fun BoxScope.StaticSearchField(
    state: () -> BottomNavState,
    search: SearchState
) {
    val focusRequester = remember { FocusRequester() }
    val isSearch = state() == BottomNavState.Search
    val placeholderSearch = stringResource(R.string.placeholder_search)
    val cdClear = stringResource(R.string.cd_clear)

    LaunchedEffect(isSearch) {
        if (isSearch) {
            delay(100)
            try { focusRequester.requestFocus() } catch (_: Exception) {}
        }
    }

    AnimatedVisibility(isSearch) {
        Row(
            modifier = Modifier.fillMaxSize(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Spacer(Modifier.width(70.dp))
            PrimaryTextField(
                value = search.query,
                onValueChange = search.onQueryChange,
                placeholder = placeholderSearch,
                modifier = Modifier
                    .weight(1f)
                    .focusRequester(focusRequester),
                style = Theme.typography.h2,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                cursorColor = MaterialTheme.colorScheme.onPrimary
            )
            if (search.query.isNotEmpty()) {
                IconButton(onClick = search.onClear) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = cdClear,
                        tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                }
            } else {
                Spacer(Modifier.width(48.dp))
            }
        }
    }
}

@Composable
fun MeasureAvailableWidth(fraction: Float = 1f, onWidthMeasured: (Dp) -> Unit) {
    Layout(content = {}, modifier = Modifier.fillMaxWidth(fraction)) { _, c ->
        onWidthMeasured(c.maxWidth.toDp())
        layout(c.maxWidth, 0) {}
    }
}

@Composable
fun MeasureAvailableHeight(fraction: Float = 1f, onHeightMeasured: (Dp) -> Unit) {
    Layout(content = {}, modifier = Modifier.fillMaxHeight(fraction)) { _, c ->
        onHeightMeasured(c.maxHeight.toDp())
        layout(0, c.maxHeight) {}
    }
}

@Composable
fun rememberBottomNavTint(state: BottomNavState): Animatable<Color, AnimationVector4D> {
    val onSurface = MaterialTheme.colorScheme.onSurface
    val onPrimary = MaterialTheme.colorScheme.onPrimary

    val anim = remember { Animatable(onSurface) }

    LaunchedEffect(state) {
        val target = if (state == BottomNavState.Default) onSurface else onPrimary
        anim.animateTo(
            targetValue = target, animationSpec = NavBarAnimation.defaultTweenColor
        )
    }

    return anim
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
    onWordChange = {}, onTranslationChange = {},
    onWordLanguageSelected = {}, onTranslationLanguageSelected = {},
    onAddWord = {}
)

@Preview
@Composable
fun PreviewBottomNav() {
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
fun PreviewBottomNavSearch() {
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
fun PreviewBottomNavAddWord() {
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

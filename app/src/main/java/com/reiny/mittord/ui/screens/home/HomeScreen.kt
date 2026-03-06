package com.reiny.mittord.ui.screens.home

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import android.app.Activity
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import com.reiny.mittord.ui.screens.wordDetail.flagForCode
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.reiny.mittord.R
import com.reiny.mittord.database.entity.SemanticObjectWithTranslations
import com.reiny.mittord.ui.screens.home.components.AppLogoToolbar
import com.reiny.mittord.ui.screens.home.components.AddWordState
import com.reiny.mittord.ui.screens.home.components.BottomNavState
import com.reiny.mittord.ui.screens.home.components.SearchState
import com.reiny.mittord.ui.screens.home.components.EmptyListPlaceholder
import com.reiny.mittord.ui.theme.MittOrdTheme
import com.reiny.mittord.ui.theme.Theme
import com.reiny.mittord.ui.theme.typography
import com.reiny.mittord.util.AppConstants

@Composable
fun MainScreen(
    onSettingsClick: () -> Unit,
    onWordClick: (Long) -> Unit,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val words by viewModel.filteredWords.collectAsState()
    val wordInput by viewModel.wordInput.collectAsState()
    val translationInput by viewModel.translationInput.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val wordLanguageCode by viewModel.wordLanguageCode.collectAsState()
    val wordLanguageIsAuto by viewModel.wordLanguageIsAuto.collectAsState()
    val translationLanguageCode by viewModel.translationLanguageCode.collectAsState()
    val translationLanguageIsAuto by viewModel.translationLanguageIsAuto.collectAsState()
    var state by remember { mutableStateOf(BottomNavState.Default) }
    val listState = rememberLazyListState()

    LaunchedEffect(Unit) {
        viewModel.scrollToTop.collect {
            listState.animateScrollToItem(0)
        }
    }
    val keyboardController = LocalSoftwareKeyboardController.current
    val context = LocalContext.current
    var lastBackMs by remember { mutableLongStateOf(0L) }
    val exitMessage = stringResource(R.string.exit_press_again)

    BackHandler {
        when (state) {
            BottomNavState.Search, BottomNavState.AddWord -> {
                keyboardController?.hide()
                viewModel.clearInputs()
                viewModel.clearSearch()
                state = BottomNavState.Default
            }
            BottomNavState.Default -> {
                val now = System.currentTimeMillis()
                if (now - lastBackMs < AppConstants.BACK_PRESS_TIMEOUT_MS) {
                    (context as? Activity)?.finish()
                } else {
                    lastBackMs = now
                    Toast.makeText(context, exitMessage, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) viewModel.reload()
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        contentWindowInsets = WindowInsets(0),
        topBar = {
            AppLogoToolbar(
                Modifier
                    .fillMaxWidth()
                    .padding(
                        top = WindowInsets.statusBars.asPaddingValues()
                            .calculateTopPadding() + 24.dp,
                        bottom = 16.dp
                    )
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .padding(innerPadding)
                .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Bottom))
                .fillMaxSize()
        ) {
            if (words.isEmpty()) {
                EmptyListPlaceholder(Modifier.align(Alignment.Center))
            } else {
                LazyColumn(
                    state = listState,
                    contentPadding = PaddingValues(
                        start = 16.dp,
                        end = 16.dp,
                        bottom = 120.dp
                    )
                ) {
                    items(words, key = { it.semanticObject.id }) { item ->
                        WordItem(item = item, onClick = { onWordClick(item.semanticObject.id) })
                    }
                }
            }

            var targetHeight by remember { mutableStateOf(0.dp) }
            MeasureAvailableHeight(fraction = 1f) { height ->
                targetHeight = height
            }
            FloatingBottomNavigationDefault(
                state = state,
                onLeftClick = {
                    if (state == BottomNavState.Default) {
                        state = BottomNavState.Search
                    }
                },
                onMiddleClick = {
                    state = when (state) {
                        BottomNavState.Search -> {
                            keyboardController?.hide()
                            viewModel.clearSearch()
                            BottomNavState.Default
                        }
                        BottomNavState.Default -> BottomNavState.AddWord
                        BottomNavState.AddWord -> {
                            keyboardController?.hide()
                            viewModel.clearInputs()
                            BottomNavState.Default
                        }
                    }
                },
                onRightClick = { onSettingsClick() },
                addWord = AddWordState(
                    wordInput = wordInput,
                    translationInput = translationInput,
                    wordLanguageCode = wordLanguageCode,
                    translationLanguageCode = translationLanguageCode,
                    wordLanguageIsAuto = wordLanguageIsAuto,
                    translationLanguageIsAuto = translationLanguageIsAuto,
                    onWordChange = viewModel::onWordChange,
                    onTranslationChange = viewModel::onTranslationChange,
                    onWordLanguageSelected = viewModel::onWordLanguageSelected,
                    onTranslationLanguageSelected = viewModel::onTranslationLanguageSelected,
                    onAddWord = {
                        viewModel.addWord()
                        keyboardController?.hide()
                        state = BottomNavState.Default
                    }
                ),
                search = SearchState(
                    query = searchQuery,
                    onQueryChange = viewModel::onSearchChange,
                    onClear = viewModel::clearSearch
                ),
                modifier = Modifier.align(Alignment.BottomCenter),
                parentHeight = targetHeight
            )
        }
    }
}

@Composable
private fun WordItem(item: SemanticObjectWithTranslations, onClick: () -> Unit) {
    val translation = item.translations.firstOrNull()?.text.orEmpty()
    val onSurface = MaterialTheme.colorScheme.onSurface
    val flag = flagForCode(item.semanticObject.wordLanguageCode)

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
                text = flag ?: AppConstants.DEFAULT_FLAG_EMOJI,
                style = Theme.typography.h2,
                modifier = Modifier.padding(end = 10.dp),
                color = if (flag == null) onSurface.copy(alpha = 0.25f) else Color.Unspecified
            )
            Column {
                Text(
                    text = item.semanticObject.baseWord,
                    style = Theme.typography.h2,
                    color = onSurface
                )
                if (translation.isNotBlank()) {
                    Spacer(Modifier.height(3.dp))
                    Text(
                        text = translation,
                        style = Theme.typography.caption,
                        color = onSurface.copy(alpha = 0.55f)
                    )
                }
            }
        }
    }
}

@Preview
@Composable
fun PreviewScreen(modifier: Modifier = Modifier) {
    MittOrdTheme {
        MainScreen(onSettingsClick = {}, onWordClick = {})
    }
}

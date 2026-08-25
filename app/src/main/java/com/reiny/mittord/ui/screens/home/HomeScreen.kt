package com.reiny.mittord.ui.screens.home

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.LocalActivity
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.reiny.mittord.R
import com.reiny.mittord.ui.screens.home.components.AddWordState
import com.reiny.mittord.ui.screens.home.components.AppLogoToolbar
import com.reiny.mittord.ui.screens.home.components.BottomNavState
import com.reiny.mittord.ui.screens.home.components.EmptyListPlaceholder
import com.reiny.mittord.ui.screens.home.components.SearchState
import com.reiny.mittord.ui.screens.home.components.WordItem
import com.reiny.mittord.util.AppConstants
import com.reiny.mittord.util.MeasureAvailableHeight
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withTimeoutOrNull

@Composable
fun HomeScreen(
    onSettingsClick: () -> Unit,
    onWordClick: (Long) -> Unit,
    sharedText: String? = null,
    onSharedTextConsumed: () -> Unit = {},
    viewModel: HomeViewModel = hiltViewModel()
) {
    val words by viewModel.words.collectAsState()
    val nav by viewModel.nav.collectAsState()
    val addWord by viewModel.addWord.collectAsState()
    val orderedLanguages by viewModel.orderedLanguages.collectAsState()

    val listState = rememberLazyListState()
    val keyboardController = LocalSoftwareKeyboardController.current
    val context = LocalContext.current
    val activity = LocalActivity.current

    // Subscriptions come first: the flows have no replay, so anything emitted
    // before a collector is attached would be lost.
    LaunchedEffect(Unit) {
        viewModel.scrollToWord.collect { id ->
            // Wait for the new word to reach the list: LazyColumn keeps its position
            // anchored to the previously first key, so scrolling before the insert
            // lands would leave the new item just above the viewport.
            withTimeoutOrNull(AppConstants.SCROLL_TO_NEW_WORD_TIMEOUT_MS) {
                snapshotFlow { words.words.firstOrNull()?.id }.first { it == id }
            }
            listState.animateScrollToItem(0)
        }
    }

    val errorTranslationFailed = stringResource(R.string.error_translation_failed)
    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                HomeEvent.TranslationFailed ->
                    Toast.makeText(context, errorTranslationFailed, Toast.LENGTH_SHORT).show()
            }
        }
    }

    LaunchedEffect(sharedText) {
        if (sharedText != null) {
            viewModel.onSharedText(sharedText)
            onSharedTextConsumed()
        }
    }

    LaunchedEffect(nav.state) {
        if (nav.state == BottomNavState.Default) keyboardController?.hide()
    }

    var lastBackMs by remember { mutableLongStateOf(0L) }
    val exitMessage = stringResource(R.string.exit_press_again)
    BackHandler {
        if (!viewModel.onBackPressed()) {
            val now = System.currentTimeMillis()
            if (now - lastBackMs < AppConstants.BACK_PRESS_TIMEOUT_MS) {
                activity?.finish()
            } else {
                lastBackMs = now
                Toast.makeText(context, exitMessage, Toast.LENGTH_SHORT).show()
            }
        }
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
                .windowInsetsPadding(WindowInsets.navigationBars.only(WindowInsetsSides.Bottom))
                .then(if (nav.state == BottomNavState.Search) Modifier.imePadding() else Modifier)
                .fillMaxSize()
        ) {
            // parentHeight is the AddWord expansion target, so it must be the height
            // measured without the keyboard. In Search the box carries imePadding(),
            // and following it would rewrite this state on every frame of the IME
            // animation, recomposing the whole box along with the navigation bar.
            var targetHeight by remember { mutableStateOf(0.dp) }
            val trackHeight = nav.state != BottomNavState.Search
            MeasureAvailableHeight(fraction = 1f) { height ->
                if (trackHeight) targetHeight = height
            }

            when {
                words.isLoading -> Unit

                words.words.isEmpty() -> EmptyListPlaceholder(
                    modifier = Modifier.align(Alignment.Center),
                    isFiltered = words.isFiltered
                )

                else -> LazyColumn(
                    state = listState,
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 120.dp)
                ) {
                    items(words.words, key = { it.id }) { item ->
                        WordItem(item = item, onClick = { onWordClick(item.id) })
                    }
                }
            }

            FloatingBottomNavigationDefault(
                state = nav.state,
                onLeftClick = viewModel::onSearchClick,
                onMiddleClick = viewModel::onCenterClick,
                onRightClick = onSettingsClick,
                addWord = AddWordState(
                    wordInput = addWord.word,
                    translationInput = addWord.translation,
                    wordLanguageCode = addWord.wordLanguageCode,
                    translationLanguageCode = addWord.translationLanguageCode,
                    wordLanguageIsAuto = addWord.wordLanguageIsAuto,
                    translationLanguageIsAuto = addWord.translationLanguageIsAuto,
                    isTranslating = addWord.isTranslating,
                    onWordChange = viewModel::onWordChange,
                    onTranslationChange = viewModel::onTranslationChange,
                    onWordLanguageSelected = viewModel::onWordLanguageSelected,
                    onTranslationLanguageSelected = viewModel::onTranslationLanguageSelected,
                    onTranslateTranslation = viewModel::translateTranslation,
                    onAddWord = viewModel::addWord
                ),
                search = SearchState(
                    query = nav.searchQuery,
                    onQueryChange = viewModel::onSearchChange,
                    onClear = viewModel::clearSearch
                ),
                orderedLanguages = orderedLanguages,
                addRecentLanguage = viewModel::addRecentLanguage,
                modifier = Modifier.align(Alignment.BottomCenter),
                parentHeight = targetHeight
            )
        }
    }
}

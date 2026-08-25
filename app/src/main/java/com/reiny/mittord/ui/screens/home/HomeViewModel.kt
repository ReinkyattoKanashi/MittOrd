package com.reiny.mittord.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.reiny.mittord.database.DictionaryRepository
import com.reiny.mittord.database.entity.SemanticObjectWithTranslations
import com.reiny.mittord.domain.model.Language
import com.reiny.mittord.domain.usecase.DetectLanguageUseCase
import com.reiny.mittord.domain.usecase.GetOrderedLanguagesUseCase
import com.reiny.mittord.domain.usecase.SeedDatabaseUseCase
import com.reiny.mittord.domain.usecase.TranslateTextUseCase
import com.reiny.mittord.domain.util.LANG_NAME_TO_BCP47
import com.reiny.mittord.domain.util.flagForCode
import com.reiny.mittord.domain.util.normalizeCode
import com.reiny.mittord.ui.screens.home.components.BottomNavState
import com.reiny.mittord.util.AppConstants
import com.reiny.mittord.util.AppPreferences
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val repository: DictionaryRepository,
    private val appPrefs: AppPreferences,
    private val detectLanguageUseCase: DetectLanguageUseCase,
    private val translateTextUseCase: TranslateTextUseCase,
    private val getOrderedLanguagesUseCase: GetOrderedLanguagesUseCase,
    private val seedDatabaseUseCase: SeedDatabaseUseCase
) : ViewModel() {

    private val _navState = MutableStateFlow(BottomNavState.Default)
    private val _searchQuery = MutableStateFlow("")

    val nav: StateFlow<NavUiState> =
        combine(_navState, _searchQuery) { state, query -> NavUiState(state, query) }
            .stateIn(viewModelScope, SharingStarted.Eagerly, NavUiState())

    private val _addWord = MutableStateFlow(AddWordUiState())
    val addWord: StateFlow<AddWordUiState> = _addWord.asStateFlow()

    private val _orderedLanguages = MutableStateFlow(appPrefs.orderedLanguages())
    val orderedLanguages: StateFlow<List<Language>> = _orderedLanguages.asStateFlow()

    val words: StateFlow<WordsUiState> =
        combine(repository.observeAll(), _searchQuery, _orderedLanguages) { all, query, languages ->
            val nativeCode = nativeLanguageCode(languages)
            val matched = if (query.isBlank()) all else all.filter { it.matches(query) }
            WordsUiState(
                words = matched.map { it.toListItem(nativeCode) },
                isLoading = false,
                isFiltered = query.isNotBlank()
            )
        }.stateIn(viewModelScope, SharingStarted.Eagerly, WordsUiState())

    /** Id of a freshly added word the list should scroll to. */
    private val _scrollToWord = MutableSharedFlow<Long>(extraBufferCapacity = 1)
    val scrollToWord = _scrollToWord.asSharedFlow()

    private val _events = MutableSharedFlow<HomeEvent>(extraBufferCapacity = 1)
    val events = _events.asSharedFlow()

    private var detectWordJob: Job? = null
    private var detectTranslationJob: Job? = null

    init {
        seedIfEmpty()
        viewModelScope.launch {
            _orderedLanguages.value = getOrderedLanguagesUseCase()
        }
    }

    // ---------- bottom navigation ----------

    fun onSearchClick() {
        if (_navState.value == BottomNavState.Default) _navState.value = BottomNavState.Search
    }

    fun onCenterClick() {
        when (_navState.value) {
            BottomNavState.Default -> _navState.value = BottomNavState.AddWord
            BottomNavState.Search, BottomNavState.AddWord -> collapse()
        }
    }

    /** @return true if the press was consumed by collapsing an open panel. */
    fun onBackPressed(): Boolean {
        if (_navState.value == BottomNavState.Default) return false
        collapse()
        return true
    }

    private fun collapse() {
        _navState.value = BottomNavState.Default
        _searchQuery.value = ""
        resetAddWord()
    }

    // ---------- search ----------

    fun onSearchChange(value: String) {
        _searchQuery.value = value
    }

    fun clearSearch() {
        _searchQuery.value = ""
    }

    // ---------- add word ----------

    fun onWordChange(value: String) {
        _addWord.update { it.copy(word = value) }
        if (!_addWord.value.wordLanguageIsAuto) return
        detectWordJob = detectLanguage(value, debounce = true, previous = detectWordJob) { code ->
            _addWord.update { it.copy(wordLanguageCode = code) }
        }
    }

    fun onTranslationChange(value: String) {
        _addWord.update { it.copy(translation = value) }
        if (!_addWord.value.translationLanguageIsAuto) return
        detectTranslationJob =
            detectLanguage(value, debounce = true, previous = detectTranslationJob) { code ->
                _addWord.update { it.copy(translationLanguageCode = code) }
            }
    }

    fun onWordLanguageSelected(code: String?) {
        if (code != null) {
            detectWordJob?.cancel()
            _addWord.update { it.copy(wordLanguageCode = code, wordLanguageIsAuto = false) }
            return
        }
        _addWord.update { it.copy(wordLanguageIsAuto = true) }
        detectWordJob =
            detectLanguage(_addWord.value.word, debounce = false, previous = detectWordJob) { detected ->
                _addWord.update { it.copy(wordLanguageCode = detected) }
            }
    }

    fun onTranslationLanguageSelected(code: String?) {
        if (code != null) {
            detectTranslationJob?.cancel()
            _addWord.update {
                it.copy(translationLanguageCode = code, translationLanguageIsAuto = false)
            }
            return
        }
        _addWord.update { it.copy(translationLanguageIsAuto = true) }
        detectTranslationJob = detectLanguage(
            _addWord.value.translation,
            debounce = false,
            previous = detectTranslationJob
        ) { detected ->
            _addWord.update { it.copy(translationLanguageCode = detected) }
        }
    }

    fun translateTranslation(targetCode: String) {
        val sourceText = _addWord.value.word.trim()
        if (sourceText.isBlank()) return
        _addWord.update {
            it.copy(
                isTranslating = true,
                translationLanguageIsAuto = false,
                translationLanguageCode = targetCode
            )
        }
        viewModelScope.launch {
            val result = translateTextUseCase(sourceText, targetCode)
            result.onSuccess { text -> _addWord.update { it.copy(translation = text) } }
            result.onFailure { _events.tryEmit(HomeEvent.TranslationFailed) }
            _addWord.update { it.copy(isTranslating = false) }
        }
    }

    /** Text handed over by another app through the PROCESS_TEXT intent. */
    fun onSharedText(text: String) {
        _addWord.value = AddWordUiState(word = text)
        _navState.value = BottomNavState.AddWord
        detectWordJob = detectLanguage(text, debounce = false, previous = detectWordJob) { code ->
            _addWord.update { it.copy(wordLanguageCode = code) }
        }
    }

    fun addWord() {
        val current = _addWord.value
        val word = current.word.trim()
        if (word.isBlank()) return
        val translation = current.translation.trim()
        val knownWordCode = current.wordLanguageCode
        val knownTranslationCode = current.translationLanguageCode
        collapse()
        viewModelScope.launch {
            val id = repository.addWord(word, translation, knownTranslationCode)
            _scrollToWord.tryEmit(id)
            val code = knownWordCode ?: detectLanguageUseCase(word)
            if (code != null) repository.updateLanguageCode(id, code)
        }
    }

    fun addRecentLanguage(name: String) = appPrefs.addRecentLanguage(name)

    // ---------- internals ----------

    /**
     * Cancels [previous], then detects the language of [text] and reports it to [onResult].
     * Text shorter than the minimum reports null right away and starts no job.
     */
    private fun detectLanguage(
        text: String,
        debounce: Boolean,
        previous: Job?,
        onResult: (String?) -> Unit
    ): Job? {
        previous?.cancel()
        val trimmed = text.trim()
        if (trimmed.length < AppConstants.LANG_DETECT_MIN_LENGTH) {
            onResult(null)
            return null
        }
        return viewModelScope.launch {
            if (debounce) delay(AppConstants.LANG_DETECT_DEBOUNCE_MS)
            onResult(detectLanguageUseCase(trimmed))
        }
    }

    private fun resetAddWord() {
        detectWordJob?.cancel()
        detectTranslationJob?.cancel()
        _addWord.value = AddWordUiState()
    }

    private fun seedIfEmpty() {
        viewModelScope.launch {
            if (repository.list().isEmpty()) seedDatabaseUseCase()
        }
    }

    private fun nativeLanguageCode(languages: List<Language>): String? {
        val name = appPrefs.nativeLanguage
        val code = languages.firstOrNull { it.name == name }?.code?.takeIf { it.isNotEmpty() }
            ?: LANG_NAME_TO_BCP47[name]
        return code?.let { normalizeCode(it) }
    }

    private fun SemanticObjectWithTranslations.matches(query: String): Boolean =
        semanticObject.baseWord.contains(query, ignoreCase = true) ||
            translations.any { it.text.contains(query, ignoreCase = true) }

    /**
     * Picks the translation shown in the list: the one in the user's native language,
     * falling back to the first stored one. The relation has no guaranteed order,
     * so it is sorted by id to keep the choice stable between openings.
     */
    private fun SemanticObjectWithTranslations.toListItem(nativeCode: String?): WordListItem {
        val ordered = translations.sortedBy { it.id }
        val chosen = nativeCode
            ?.let { code -> ordered.firstOrNull { normalizeCode(it.languageCode) == code } }
            ?: ordered.firstOrNull()
        return WordListItem(
            id = semanticObject.id,
            word = semanticObject.baseWord,
            wordFlag = flagForCode(semanticObject.wordLanguageCode),
            translation = chosen?.text?.takeIf { it.isNotBlank() },
            translationFlag = chosen?.let { flagForCode(it.languageCode) }
        )
    }
}

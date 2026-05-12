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

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    val filteredWords: StateFlow<List<SemanticObjectWithTranslations>> =
        combine(repository.observeAll(), _searchQuery) { words, query ->
            if (query.isBlank()) words
            else words.filter { item ->
                item.semanticObject.baseWord.contains(query, ignoreCase = true) ||
                item.translations.any { it.text.contains(query, ignoreCase = true) }
            }
        }.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    private val _wordInput = MutableStateFlow("")
    val wordInput: StateFlow<String> = _wordInput.asStateFlow()

    private val _translationInput = MutableStateFlow("")
    val translationInput: StateFlow<String> = _translationInput.asStateFlow()

    private val _wordLanguageCode = MutableStateFlow<String?>(null)
    val wordLanguageCode: StateFlow<String?> = _wordLanguageCode.asStateFlow()

    private val _translationLanguageCode = MutableStateFlow<String?>(null)
    val translationLanguageCode: StateFlow<String?> = _translationLanguageCode.asStateFlow()

    private val _wordLanguageIsAuto = MutableStateFlow(true)
    val wordLanguageIsAuto: StateFlow<Boolean> = _wordLanguageIsAuto.asStateFlow()

    private val _translationLanguageIsAuto = MutableStateFlow(true)
    val translationLanguageIsAuto: StateFlow<Boolean> = _translationLanguageIsAuto.asStateFlow()

    private val _isTranslatingTranslation = MutableStateFlow(false)
    val isTranslatingTranslation: StateFlow<Boolean> = _isTranslatingTranslation.asStateFlow()

    private val _scrollToTop = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
    val scrollToTop = _scrollToTop.asSharedFlow()

    private val _events = MutableSharedFlow<HomeEvent>(extraBufferCapacity = 1)
    val events = _events.asSharedFlow()

    private val _orderedLanguages = MutableStateFlow(appPrefs.orderedLanguages())
    val orderedLanguages: StateFlow<List<Language>> = _orderedLanguages.asStateFlow()

    private var detectWordJob: Job? = null
    private var detectTranslationJob: Job? = null

    init {
        seedIfEmpty()
        viewModelScope.launch {
            _orderedLanguages.value = getOrderedLanguagesUseCase()
        }
    }

    fun onWordChange(value: String) {
        _wordInput.value = value
        if (!_wordLanguageIsAuto.value) return
        detectWordJob?.cancel()
        val trimmed = value.trim()
        if (trimmed.length >= AppConstants.LANG_DETECT_MIN_LENGTH) {
            detectWordJob = viewModelScope.launch {
                delay(AppConstants.LANG_DETECT_DEBOUNCE_MS)
                _wordLanguageCode.value = detectLanguageUseCase(trimmed)
            }
        } else {
            _wordLanguageCode.value = null
        }
    }

    fun onTranslationChange(value: String) {
        _translationInput.value = value
        if (!_translationLanguageIsAuto.value) return
        detectTranslationJob?.cancel()
        val trimmed = value.trim()
        if (trimmed.length >= AppConstants.LANG_DETECT_MIN_LENGTH) {
            detectTranslationJob = viewModelScope.launch {
                delay(AppConstants.LANG_DETECT_DEBOUNCE_MS)
                _translationLanguageCode.value = detectLanguageUseCase(trimmed)
            }
        } else {
            _translationLanguageCode.value = null
        }
    }

    fun onWordLanguageSelected(code: String?) {
        detectWordJob?.cancel()
        if (code == null) {
            _wordLanguageIsAuto.value = true
            val trimmed = _wordInput.value.trim()
            if (trimmed.length >= AppConstants.LANG_DETECT_MIN_LENGTH) {
                detectWordJob = viewModelScope.launch {
                    _wordLanguageCode.value = detectLanguageUseCase(trimmed)
                }
            } else {
                _wordLanguageCode.value = null
            }
        } else {
            _wordLanguageIsAuto.value = false
            _wordLanguageCode.value = code
        }
    }

    fun onTranslationLanguageSelected(code: String?) {
        detectTranslationJob?.cancel()
        if (code == null) {
            _translationLanguageIsAuto.value = true
            val trimmed = _translationInput.value.trim()
            if (trimmed.length >= AppConstants.LANG_DETECT_MIN_LENGTH) {
                detectTranslationJob = viewModelScope.launch {
                    _translationLanguageCode.value = detectLanguageUseCase(trimmed)
                }
            } else {
                _translationLanguageCode.value = null
            }
        } else {
            _translationLanguageIsAuto.value = false
            _translationLanguageCode.value = code
        }
    }

    fun translateTranslation(targetCode: String) {
        val sourceText = _wordInput.value.trim()
        if (sourceText.isBlank()) return
        _isTranslatingTranslation.value = true
        _translationLanguageIsAuto.value = false
        _translationLanguageCode.value = targetCode
        viewModelScope.launch {
            val result = translateTextUseCase(sourceText, targetCode)
            result.onSuccess { _translationInput.value = it }
            result.onFailure { _events.tryEmit(HomeEvent.TranslationFailed) }
            _isTranslatingTranslation.value = false
        }
    }

    fun setExternalWord(word: String) {
        _wordInput.value = word
        _wordLanguageIsAuto.value = true
        _wordLanguageCode.value = null
        detectWordJob?.cancel()
        if (word.length >= AppConstants.LANG_DETECT_MIN_LENGTH) {
            detectWordJob = viewModelScope.launch {
                _wordLanguageCode.value = detectLanguageUseCase(word)
            }
        }
    }

    fun addRecentLanguage(name: String) = appPrefs.addRecentLanguage(name)

    fun onSearchChange(value: String) { _searchQuery.value = value }

    fun clearInputs() = resetInputs()

    fun clearSearch() { _searchQuery.value = "" }

    fun addWord() {
        val word = _wordInput.value.trim()
        if (word.isBlank()) return
        val translation = _translationInput.value.trim()
        val knownWordCode = _wordLanguageCode.value
        val knownTransCode = _translationLanguageCode.value
        viewModelScope.launch {
            val id = repository.addWord(word, translation, knownTransCode)
            resetInputs()
            val code = knownWordCode ?: detectLanguageUseCase(word)
            if (code != null) repository.updateLanguageCode(id, code)
            _scrollToTop.tryEmit(Unit)
        }
    }

    private fun seedIfEmpty() {
        viewModelScope.launch {
            if (repository.list().isEmpty()) seedDatabaseUseCase()
        }
    }

    private fun resetInputs() {
        _wordInput.value = ""
        _translationInput.value = ""
        _wordLanguageCode.value = null
        _translationLanguageCode.value = null
        _wordLanguageIsAuto.value = true
        _translationLanguageIsAuto.value = true
        _isTranslatingTranslation.value = false
        detectWordJob?.cancel()
        detectTranslationJob?.cancel()
    }

}

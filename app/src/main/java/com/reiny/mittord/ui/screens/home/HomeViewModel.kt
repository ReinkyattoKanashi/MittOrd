package com.reiny.mittord.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.reiny.mittord.database.DictionaryRepository
import com.reiny.mittord.database.entity.SemanticObjectWithTranslations
import com.reiny.mittord.ui.screens.wordDetail.detectLanguage
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
    private val appPrefs: AppPreferences
) : ViewModel() {

    private val _words = MutableStateFlow<List<SemanticObjectWithTranslations>>(emptyList())

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    val filteredWords: StateFlow<List<SemanticObjectWithTranslations>> =
        combine(_words, _searchQuery) { words, query ->
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

    private val _scrollToTop = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
    val scrollToTop = _scrollToTop.asSharedFlow()

    private var detectWordJob: Job? = null
    private var detectTranslationJob: Job? = null

    init {
        load()
    }

    fun onWordChange(value: String) {
        _wordInput.value = value
        if (!_wordLanguageIsAuto.value) return
        detectWordJob?.cancel()
        val trimmed = value.trim()
        if (trimmed.length >= AppConstants.LANG_DETECT_MIN_LENGTH) {
            detectWordJob = viewModelScope.launch {
                delay(AppConstants.LANG_DETECT_DEBOUNCE_MS)
                _wordLanguageCode.value = detectLanguage(trimmed)
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
                _translationLanguageCode.value = detectLanguage(trimmed)
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
                    _wordLanguageCode.value = detectLanguage(trimmed)
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
                    _translationLanguageCode.value = detectLanguage(trimmed)
                }
            } else {
                _translationLanguageCode.value = null
            }
        } else {
            _translationLanguageIsAuto.value = false
            _translationLanguageCode.value = code
        }
    }

    fun addRecentLanguage(name: String) = appPrefs.addRecentLanguage(name)
    fun orderedLanguages() = appPrefs.orderedLanguages()

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
            val code = knownWordCode ?: detectLanguage(word)
            if (code != null) repository.updateLanguageCode(id, code)
            _words.value = repository.listWithTranslations()
            _scrollToTop.tryEmit(Unit)
        }
    }

    fun reload() {
        viewModelScope.launch {
            _words.value = repository.listWithTranslations()
        }
    }

    private fun resetInputs() {
        _wordInput.value = ""
        _translationInput.value = ""
        _wordLanguageCode.value = null
        _translationLanguageCode.value = null
        _wordLanguageIsAuto.value = true
        _translationLanguageIsAuto.value = true
        detectWordJob?.cancel()
        detectTranslationJob?.cancel()
    }

    private fun load() {
        viewModelScope.launch {
            if (repository.list().isEmpty()) seedMockData()
            _words.value = repository.listWithTranslations()
        }
    }

    private suspend fun seedMockData() {
        listOf(
            "hund" to "собака", "katt" to "кошка", "hus" to "дом", "bil" to "машина",
            "bok" to "книга", "vann" to "вода", "mat" to "еда", "dag" to "день",
            "natt" to "ночь", "sol" to "солнце", "måne" to "луна", "tre" to "дерево",
            "blomst" to "цветок", "fugl" to "птица", "fisk" to "рыба", "himmel" to "небо",
            "fjell" to "гора", "hav" to "море", "elv" to "река", "vind" to "ветер"
        ).forEach { (word, translation) -> repository.addWord(word, translation) }
    }
}

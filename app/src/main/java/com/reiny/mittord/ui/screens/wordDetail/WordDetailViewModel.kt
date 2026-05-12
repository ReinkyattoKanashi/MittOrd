package com.reiny.mittord.ui.screens.wordDetail

import android.net.Uri
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.reiny.mittord.database.DictionaryRepository
import com.reiny.mittord.database.TranslationData
import com.reiny.mittord.database.WordUpdate
import com.reiny.mittord.domain.usecase.DetectLanguageUseCase
import com.reiny.mittord.domain.usecase.GetSupportedLanguagesUseCase
import com.reiny.mittord.domain.usecase.TranslateTextUseCase
import com.reiny.mittord.ui.screens.settings.Language
import com.reiny.mittord.ui.screens.settings.LANGUAGES
import com.reiny.mittord.util.AppConstants
import com.reiny.mittord.util.AppPreferences
import com.reiny.mittord.util.WordImageRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject

data class TranslationEntry(
    val id: Long = 0L,
    val text: String = "",
    val languageCode: String? = null,
    val isAuto: Boolean = true,
    val isTranslating: Boolean = false
)

data class WordDetailState(
    val wordId: Long = 0,
    val word: String = "",
    val wordLanguageCode: String? = null,
    val wordLanguageIsAuto: Boolean = false,
    val translations: List<TranslationEntry> = listOf(TranslationEntry()),
    val comment: String = "",
    val imagePath: String? = null,
    val isLoading: Boolean = true
)

@HiltViewModel
class WordDetailViewModel @Inject constructor(
    private val repository: DictionaryRepository,
    private val appPrefs: AppPreferences,
    private val wordImageRepo: WordImageRepository,
    private val detectLanguageUseCase: DetectLanguageUseCase,
    private val translateTextUseCase: TranslateTextUseCase,
    private val getSupportedLanguagesUseCase: GetSupportedLanguagesUseCase,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val wordId: Long = checkNotNull(savedStateHandle["wordId"])

    private val _state = MutableStateFlow(WordDetailState())
    val state: StateFlow<WordDetailState> = _state

    private val _focusTranslation = MutableSharedFlow<Int>(extraBufferCapacity = 1)
    val focusTranslation = _focusTranslation.asSharedFlow()

    private val _allLanguages = MutableStateFlow<List<Language>>(LANGUAGES)
    val orderedLanguages: StateFlow<List<Language>> = _allLanguages
        .map { appPrefs.orderedLanguages(it) }
        .stateIn(viewModelScope, SharingStarted.Eagerly, appPrefs.orderedLanguages())

    private var detectWordJob: Job? = null
    private val detectTranslationJobs = mutableMapOf<Int, Job>()

    private var originalWord = ""
    private var originalTranslations = listOf<TranslationEntry>()
    private var originalComment = ""
    private var originalImagePath: String? = null
    private var originalWordLanguageCode: String? = null

    val hasUnsavedChanges: Boolean
        get() {
            val s = _state.value
            if (s.isLoading) return false
            return s.word.trim() != originalWord ||
                s.comment.trim() != originalComment ||
                s.imagePath != originalImagePath ||
                s.wordLanguageCode != originalWordLanguageCode ||
                translationsChanged(s.translations)
        }

    private fun translationsChanged(current: List<TranslationEntry>): Boolean {
        if (current.size != originalTranslations.size) return true
        return current.zip(originalTranslations).any { (cur, orig) ->
            cur.text.trim() != orig.text.trim() || cur.languageCode != orig.languageCode
        }
    }

    init {
        load()
        viewModelScope.launch {
            val languages = getSupportedLanguagesUseCase()
            if (languages.isNotEmpty()) _allLanguages.value = languages
        }
    }

    fun onWordChange(value: String) {
        _state.value = _state.value.copy(word = value)
        if (!_state.value.wordLanguageIsAuto) return
        detectWordJob?.cancel()
        val trimmed = value.trim()
        if (trimmed.length >= AppConstants.LANG_DETECT_MIN_LENGTH) {
            detectWordJob = viewModelScope.launch {
                delay(AppConstants.LANG_DETECT_DEBOUNCE_MS)
                val code = detectLanguageUseCase(trimmed)
                if (code != null) _state.value = _state.value.copy(wordLanguageCode = code)
            }
        }
    }

    fun onWordLanguageSelected(code: String?) {
        detectWordJob?.cancel()
        if (code == null) {
            _state.value = _state.value.copy(wordLanguageIsAuto = true)
            val trimmed = _state.value.word.trim()
            if (trimmed.length >= AppConstants.LANG_DETECT_MIN_LENGTH) {
                detectWordJob = viewModelScope.launch {
                    val detected = detectLanguageUseCase(trimmed)
                    if (detected != null) _state.value = _state.value.copy(wordLanguageCode = detected)
                }
            }
        } else {
            _state.value = _state.value.copy(wordLanguageCode = code, wordLanguageIsAuto = false)
        }
    }

    fun onTranslationChange(index: Int, value: String) {
        val translations = _state.value.translations.toMutableList()
        if (index !in translations.indices) return
        val entry = translations[index]
        translations[index] = entry.copy(text = value)
        _state.value = _state.value.copy(translations = translations)
        if (!entry.isAuto) return
        detectTranslationJobs[index]?.cancel()
        val trimmed = value.trim()
        if (trimmed.length >= AppConstants.LANG_DETECT_MIN_LENGTH) {
            detectTranslationJobs[index] = viewModelScope.launch {
                delay(AppConstants.LANG_DETECT_DEBOUNCE_MS)
                val code = detectLanguageUseCase(trimmed)
                if (code != null) {
                    val current = _state.value.translations.toMutableList()
                    if (index in current.indices) {
                        current[index] = current[index].copy(languageCode = code)
                        _state.value = _state.value.copy(translations = current)
                    }
                }
                detectTranslationJobs.remove(index)
            }
        }
    }

    fun onTranslationLanguageSelected(index: Int, code: String?) {
        detectTranslationJobs[index]?.cancel()
        detectTranslationJobs.remove(index)
        val translations = _state.value.translations.toMutableList()
        if (index !in translations.indices) return
        if (code == null) {
            translations[index] = translations[index].copy(isAuto = true)
            _state.value = _state.value.copy(translations = translations)
            val trimmed = translations[index].text.trim()
            if (trimmed.length >= AppConstants.LANG_DETECT_MIN_LENGTH) {
                detectTranslationJobs[index] = viewModelScope.launch {
                    val detected = detectLanguageUseCase(trimmed)
                    if (detected != null) {
                        val current = _state.value.translations.toMutableList()
                        if (index in current.indices) {
                            current[index] = current[index].copy(languageCode = detected)
                            _state.value = _state.value.copy(translations = current)
                        }
                    }
                    detectTranslationJobs.remove(index)
                }
            }
        } else {
            translations[index] = translations[index].copy(languageCode = code, isAuto = false)
            _state.value = _state.value.copy(translations = translations)
        }
    }

    fun translateTranslation(index: Int, targetCode: String) {
        val sourceText = _state.value.word.trim()
        if (sourceText.isBlank()) return
        val translations = _state.value.translations.toMutableList()
        if (index !in translations.indices) return
        translations[index] = translations[index].copy(isTranslating = true)
        _state.value = _state.value.copy(translations = translations)
        viewModelScope.launch {
            val result = translateTextUseCase(sourceText, targetCode)
            val current = _state.value.translations.toMutableList()
            if (index in current.indices) {
                current[index] = current[index].copy(
                    text = result ?: current[index].text,
                    languageCode = targetCode,
                    isAuto = false,
                    isTranslating = false
                )
                _state.value = _state.value.copy(translations = current)
            }
        }
    }

    fun addRecentLanguage(name: String) = appPrefs.addRecentLanguage(name)

    fun addTranslation() {
        val translations = _state.value.translations.toMutableList()
        translations.add(TranslationEntry())
        _state.value = _state.value.copy(translations = translations)
        _focusTranslation.tryEmit(translations.lastIndex)
    }

    fun removeTranslation(index: Int) {
        if (_state.value.translations.size <= 1) return
        val translations = _state.value.translations.toMutableList()
        if (index !in translations.indices) return
        detectTranslationJobs[index]?.cancel()
        detectTranslationJobs.remove(index)
        translations.removeAt(index)
        _state.value = _state.value.copy(translations = translations)
    }

    fun onCommentChange(value: String) {
        _state.value = _state.value.copy(comment = value)
    }

    fun saveImage(uri: Uri) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val path = wordImageRepo.save(uri, _state.value.wordId)
                _state.value = _state.value.copy(imagePath = path)
            } catch (_: Exception) {}
        }
    }

    fun removeImage() {
        val path = _state.value.imagePath ?: return
        _state.value = _state.value.copy(imagePath = null)
        viewModelScope.launch(Dispatchers.IO) {
            File(path).delete()
        }
    }

    fun save(onDone: () -> Unit) {
        val s = _state.value
        viewModelScope.launch {
            repository.updateWordFull(
                id = wordId,
                update = WordUpdate(
                    baseWord = s.word.trim(),
                    translations = s.translations.map {
                        TranslationData(text = it.text.trim(), languageCode = it.languageCode)
                    },
                    comment = s.comment.trim().ifBlank { null },
                    imagePath = s.imagePath,
                    wordLanguageCode = s.wordLanguageCode
                )
            )
            onDone()
        }
    }

    fun delete(onDone: () -> Unit) {
        viewModelScope.launch {
            _state.value.imagePath?.let { path ->
                withContext(Dispatchers.IO) { File(path).delete() }
            }
            repository.deleteWord(wordId)
            onDone()
        }
    }

    private fun load() {
        viewModelScope.launch {
            val item = repository.getWordWithTranslations(wordId)
            if (item != null) {
                val loadedTranslations = item.translations.map { t ->
                    TranslationEntry(
                        id = t.id,
                        text = t.text,
                        languageCode = t.languageCode.ifBlank { null },
                        isAuto = false
                    )
                }
                val finalTranslations = loadedTranslations.ifEmpty { listOf(TranslationEntry()) }
                _state.value = WordDetailState(
                    wordId = wordId,
                    word = item.semanticObject.baseWord,
                    wordLanguageCode = item.semanticObject.wordLanguageCode,
                    wordLanguageIsAuto = false,
                    translations = finalTranslations,
                    comment = item.semanticObject.comment.orEmpty(),
                    imagePath = item.semanticObject.imagePath,
                    isLoading = false
                )
                originalWord = item.semanticObject.baseWord
                originalTranslations = finalTranslations
                originalComment = item.semanticObject.comment.orEmpty()
                originalImagePath = item.semanticObject.imagePath
                originalWordLanguageCode = item.semanticObject.wordLanguageCode
            }
        }
    }
}

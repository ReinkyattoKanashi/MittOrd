package com.reiny.mittord.ui.screens.wordDetail

import android.net.Uri
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.reiny.mittord.database.DictionaryRepository
import com.reiny.mittord.database.TranslationData
import com.reiny.mittord.database.WordUpdate
import com.reiny.mittord.domain.model.Language
import com.reiny.mittord.domain.usecase.DetectLanguageUseCase
import com.reiny.mittord.domain.usecase.GetOrderedLanguagesUseCase
import com.reiny.mittord.domain.usecase.TranslateTextUseCase
import com.reiny.mittord.domain.util.LanguageDetector
import com.reiny.mittord.util.AppPreferences
import com.reiny.mittord.util.WordImageRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import java.io.File
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

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

/** Detection key for the base word; translations are keyed by their index. */
private const val WORD_FIELD = "word"

@HiltViewModel
class WordDetailViewModel @Inject constructor(
    private val repository: DictionaryRepository,
    private val appPrefs: AppPreferences,
    private val wordImageRepo: WordImageRepository,
    private val translateTextUseCase: TranslateTextUseCase,
    private val getOrderedLanguagesUseCase: GetOrderedLanguagesUseCase,
    detectLanguageUseCase: DetectLanguageUseCase,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val wordId: Long = checkNotNull(savedStateHandle["wordId"])

    private val _state = MutableStateFlow(WordDetailState())
    val state: StateFlow<WordDetailState> = _state.asStateFlow()

    private val _focusTranslation = MutableSharedFlow<Int>(extraBufferCapacity = 1)
    val focusTranslation = _focusTranslation.asSharedFlow()

    private val _events = MutableSharedFlow<WordDetailEvent>(extraBufferCapacity = 1)
    val events = _events.asSharedFlow()

    private val _orderedLanguages = MutableStateFlow(appPrefs.orderedLanguages())
    val orderedLanguages: StateFlow<List<Language>> = _orderedLanguages.asStateFlow()

    private val detector = LanguageDetector(viewModelScope, detectLanguageUseCase)

    private var original = WordDetailState()

    /**
     * Detection may answer null for text it cannot place. Unlike the add-word form, an
     * existing word keeps the language it was saved with instead of losing the flag.
     */
    val hasUnsavedChanges: Boolean
        get() {
            val current = _state.value
            if (current.isLoading) return false
            return current.word.trim() != original.word ||
                current.comment.trim() != original.comment ||
                current.imagePath != original.imagePath ||
                current.wordLanguageCode != original.wordLanguageCode ||
                translationsChanged(current.translations)
        }

    private fun translationsChanged(current: List<TranslationEntry>): Boolean {
        if (current.size != original.translations.size) return true
        return current.zip(original.translations).any { (now, before) ->
            now.text.trim() != before.text.trim() || now.languageCode != before.languageCode
        }
    }

    init {
        load()
        viewModelScope.launch {
            _orderedLanguages.value = getOrderedLanguagesUseCase()
        }
    }

    // ---------- base word ----------

    fun onWordChange(value: String) {
        _state.update { it.copy(word = value) }
        if (!_state.value.wordLanguageIsAuto) return
        detectWord(value, debounce = true)
    }

    fun onWordLanguageSelected(code: String?) {
        if (code != null) {
            detector.cancel(WORD_FIELD)
            _state.update { it.copy(wordLanguageCode = code, wordLanguageIsAuto = false) }
            return
        }
        _state.update { it.copy(wordLanguageIsAuto = true) }
        detectWord(_state.value.word, debounce = false)
    }

    private fun detectWord(text: String, debounce: Boolean) {
        detector.request(WORD_FIELD, text, debounce) { code ->
            if (code != null) _state.update { it.copy(wordLanguageCode = code) }
        }
    }

    // ---------- translations ----------

    fun onTranslationChange(index: Int, value: String) {
        val entry = _state.value.translations.getOrNull(index) ?: return
        updateTranslation(index) { it.copy(text = value) }
        if (!entry.isAuto) return
        detectTranslation(index, value, debounce = true)
    }

    fun onTranslationLanguageSelected(index: Int, code: String?) {
        if (index !in _state.value.translations.indices) return
        if (code != null) {
            detector.cancel(index)
            updateTranslation(index) { it.copy(languageCode = code, isAuto = false) }
            return
        }
        updateTranslation(index) { it.copy(isAuto = true) }
        detectTranslation(index, _state.value.translations[index].text, debounce = false)
    }

    private fun detectTranslation(index: Int, text: String, debounce: Boolean) {
        detector.request(index, text, debounce) { code ->
            if (code != null) updateTranslation(index) { it.copy(languageCode = code) }
        }
    }

    fun translateTranslation(index: Int, targetCode: String) {
        val sourceText = _state.value.word.trim()
        if (sourceText.isBlank() || index !in _state.value.translations.indices) return
        updateTranslation(index) { it.copy(isTranslating = true) }
        viewModelScope.launch {
            val result = translateTextUseCase(sourceText, targetCode)
            updateTranslation(index) {
                it.copy(
                    text = result.getOrNull() ?: it.text,
                    languageCode = targetCode,
                    isAuto = false,
                    isTranslating = false
                )
            }
            if (result.isFailure) _events.tryEmit(WordDetailEvent.TranslationFailed)
        }
    }

    fun addTranslation() {
        _state.update { it.copy(translations = it.translations + TranslationEntry()) }
        _focusTranslation.tryEmit(_state.value.translations.lastIndex)
    }

    fun removeTranslation(index: Int) {
        val translations = _state.value.translations
        if (translations.size <= 1 || index !in translations.indices) return
        detector.cancel(index)
        _state.update { it.copy(translations = it.translations.filterIndexed { i, _ -> i != index }) }
    }

    /** Applies [transform] to one entry, ignoring indices that no longer exist. */
    private fun updateTranslation(index: Int, transform: (TranslationEntry) -> TranslationEntry) {
        _state.update { current ->
            if (index !in current.translations.indices) return@update current
            val updated = current.translations.toMutableList()
            updated[index] = transform(updated[index])
            current.copy(translations = updated)
        }
    }

    // ---------- comment, image ----------

    fun onCommentChange(value: String) {
        _state.update { it.copy(comment = value) }
    }

    fun saveImage(uri: Uri) {
        viewModelScope.launch {
            val path = runCatching {
                withContext(Dispatchers.IO) { wordImageRepo.save(uri, _state.value.wordId) }
            }.getOrNull()
            if (path == null) {
                _events.tryEmit(WordDetailEvent.ImageSaveFailed)
            } else {
                _state.update { it.copy(imagePath = path) }
            }
        }
    }

    fun removeImage() {
        val path = _state.value.imagePath ?: return
        _state.update { it.copy(imagePath = null) }
        viewModelScope.launch {
            withContext(Dispatchers.IO) { File(path).delete() }
        }
    }

    // ---------- persistence ----------

    fun addRecentLanguage(name: String) = appPrefs.addRecentLanguage(name)

    fun save() {
        val current = _state.value
        viewModelScope.launch {
            repository.updateWordFull(
                id = wordId,
                update = WordUpdate(
                    baseWord = current.word.trim(),
                    translations = current.translations.map {
                        TranslationData(text = it.text.trim(), languageCode = it.languageCode)
                    },
                    comment = current.comment.trim().ifBlank { null },
                    imagePath = current.imagePath,
                    wordLanguageCode = current.wordLanguageCode
                )
            )
            _events.emit(WordDetailEvent.Saved)
        }
    }

    fun delete() {
        viewModelScope.launch {
            _state.value.imagePath?.let { path ->
                withContext(Dispatchers.IO) { File(path).delete() }
            }
            repository.deleteWord(wordId)
            _events.emit(WordDetailEvent.Deleted)
        }
    }

    private fun load() {
        viewModelScope.launch {
            val item = repository.getWordWithTranslations(wordId) ?: return@launch
            // The relation has no guaranteed order; sorting by id keeps the fields from
            // swapping places between openings, and matches the order the list uses.
            val loaded = item.translations
                .sortedBy { it.id }
                .map { entry ->
                    TranslationEntry(
                        id = entry.id,
                        text = entry.text,
                        languageCode = entry.languageCode.ifBlank { null },
                        isAuto = false
                    )
                }
                .ifEmpty { listOf(TranslationEntry()) }

            val loadedState = WordDetailState(
                wordId = wordId,
                word = item.semanticObject.baseWord,
                wordLanguageCode = item.semanticObject.wordLanguageCode,
                wordLanguageIsAuto = false,
                translations = loaded,
                comment = item.semanticObject.comment.orEmpty(),
                imagePath = item.semanticObject.imagePath,
                isLoading = false
            )
            _state.value = loadedState
            original = loadedState
        }
    }
}

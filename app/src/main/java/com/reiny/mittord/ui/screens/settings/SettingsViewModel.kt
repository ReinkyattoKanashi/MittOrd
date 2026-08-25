package com.reiny.mittord.ui.screens.settings

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.reiny.mittord.database.DictionaryRepository
import com.reiny.mittord.domain.model.Language
import com.reiny.mittord.domain.usecase.GetOrderedLanguagesUseCase
import com.reiny.mittord.domain.usecase.SeedDatabaseUseCase
import com.reiny.mittord.util.AppConstants
import com.reiny.mittord.util.AppPreferences
import com.reiny.mittord.util.AvatarRepository
import com.reiny.mittord.util.WordImageRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

data class SettingsUiState(
    val avatarPath: String? = null,
    /** Bumped on every avatar write so Coil reloads instead of serving the cached file. */
    val avatarVersion: Int = 0,
    val learningLanguage: String = "",
    val nativeLanguage: String = "",
    val languages: List<Language> = emptyList()
)

sealed interface SettingsEvent {
    data object MockDataAdded : SettingsEvent
    data object WordsCleared : SettingsEvent
    data object AvatarFailed : SettingsEvent
}

@HiltViewModel
class SettingsViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val appPrefs: AppPreferences,
    private val avatarRepo: AvatarRepository,
    private val repository: DictionaryRepository,
    private val wordImageRepo: WordImageRepository,
    private val seedDatabaseUseCase: SeedDatabaseUseCase,
    private val getOrderedLanguagesUseCase: GetOrderedLanguagesUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(
        SettingsUiState(
            avatarPath = appPrefs.avatarPath,
            learningLanguage = appPrefs.learningLanguage,
            nativeLanguage = appPrefs.nativeLanguage,
            languages = appPrefs.orderedLanguages()
        )
    )
    val state: StateFlow<SettingsUiState> = _state.asStateFlow()

    private val _events = MutableSharedFlow<SettingsEvent>(extraBufferCapacity = 1)
    val events = _events.asSharedFlow()

    /** Cropping happens in the dialog, so the picked image is handed over decoded. */
    private val _imageToCrop = MutableStateFlow<Bitmap?>(null)
    val imageToCrop: StateFlow<Bitmap?> = _imageToCrop.asStateFlow()

    init {
        viewModelScope.launch {
            _state.update { it.copy(languages = getOrderedLanguagesUseCase()) }
        }
    }

    // ---------- languages ----------

    /**
     * The counterpart language is filtered out of the picker, so the two can never be
     * set to the same value - which used to produce duplicate keys in the picker list
     * and crash it.
     */
    fun languagesFor(target: LanguageTarget): List<Language> {
        val taken = when (target) {
            LanguageTarget.Learning -> _state.value.nativeLanguage
            LanguageTarget.Native -> _state.value.learningLanguage
        }
        return _state.value.languages.filter { it.name != taken }
    }

    fun setLanguage(target: LanguageTarget, name: String) {
        when (target) {
            LanguageTarget.Learning -> {
                if (name == _state.value.nativeLanguage) return
                appPrefs.learningLanguage = name
                _state.update { it.copy(learningLanguage = name) }
            }

            LanguageTarget.Native -> {
                if (name == _state.value.learningLanguage) return
                appPrefs.nativeLanguage = name
                _state.update { it.copy(nativeLanguage = name) }
            }
        }
    }

    // ---------- avatar ----------

    /** Decodes the picked image off the main thread before the crop dialog opens. */
    fun onAvatarPicked(uri: Uri) {
        if (context.contentResolver.getType(uri) == AppConstants.MIME_IMAGE_GIF) {
            saveAvatarGif(uri)
            return
        }
        viewModelScope.launch {
            val bitmap = runCatching {
                withContext(Dispatchers.IO) {
                    context.contentResolver.openInputStream(uri)?.use(BitmapFactory::decodeStream)
                }
            }.getOrNull()
            if (bitmap == null) _events.tryEmit(SettingsEvent.AvatarFailed)
            else _imageToCrop.value = bitmap
        }
    }

    fun onCropCancelled() {
        _imageToCrop.value = null
    }

    fun saveAvatarBitmap(bitmap: Bitmap) {
        _imageToCrop.value = null
        storeAvatar { avatarRepo.saveJpeg(bitmap) }
    }

    private fun saveAvatarGif(uri: Uri) = storeAvatar { avatarRepo.saveGif(uri) }

    private fun storeAvatar(write: suspend () -> String) {
        viewModelScope.launch {
            val path = runCatching { withContext(Dispatchers.IO) { write() } }.getOrNull()
            if (path == null) {
                _events.tryEmit(SettingsEvent.AvatarFailed)
                return@launch
            }
            appPrefs.avatarPath = path
            _state.update { it.copy(avatarPath = path, avatarVersion = it.avatarVersion + 1) }
        }
    }

    fun deleteAvatar() {
        viewModelScope.launch {
            withContext(Dispatchers.IO) { avatarRepo.delete() }
            appPrefs.avatarPath = null
            _state.update { it.copy(avatarPath = null, avatarVersion = it.avatarVersion + 1) }
        }
    }

    // ---------- developer ----------

    fun seedMockData() {
        viewModelScope.launch {
            seedDatabaseUseCase()
            _events.emit(SettingsEvent.MockDataAdded)
        }
    }

    /** Wipes the dictionary along with the photos its words pointed at. */
    fun clearAllWords() {
        viewModelScope.launch {
            repository.deleteAllWords()
            withContext(Dispatchers.IO) { wordImageRepo.deleteAll() }
            _events.emit(SettingsEvent.WordsCleared)
        }
    }
}

/** Which of the two language settings a picker is editing. */
enum class LanguageTarget { Learning, Native }

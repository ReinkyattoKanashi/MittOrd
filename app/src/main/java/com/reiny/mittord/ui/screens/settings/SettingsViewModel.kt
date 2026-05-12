package com.reiny.mittord.ui.screens.settings

import android.graphics.Bitmap
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.reiny.mittord.database.DictionaryRepository
import com.reiny.mittord.util.AppPreferences
import com.reiny.mittord.util.AvatarRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val appPrefs: AppPreferences,
    private val avatarRepo: AvatarRepository,
    private val dictionaryRepository: DictionaryRepository
) : ViewModel() {

    private val _seedDoneEvent = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
    val seedDoneEvent = _seedDoneEvent.asSharedFlow()

    private val _avatarPath = MutableStateFlow<String?>(appPrefs.avatarPath)
    val avatarPath: StateFlow<String?> = _avatarPath.asStateFlow()

    private val _avatarVersion = MutableStateFlow(0)
    val avatarVersion: StateFlow<Int> = _avatarVersion.asStateFlow()

    private val _learningLanguage = MutableStateFlow(appPrefs.learningLanguage)
    val learningLanguage: StateFlow<String> = _learningLanguage.asStateFlow()

    private val _nativeLanguage = MutableStateFlow(appPrefs.nativeLanguage)
    val nativeLanguage: StateFlow<String> = _nativeLanguage.asStateFlow()

    fun saveAvatarBitmap(bitmap: Bitmap) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val path = avatarRepo.saveJpeg(bitmap)
                appPrefs.avatarPath = path
                _avatarPath.value = path
                _avatarVersion.value++
            } catch (_: Exception) {}
        }
    }

    fun saveAvatarGif(uri: Uri) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val path = avatarRepo.saveGif(uri)
                appPrefs.avatarPath = path
                _avatarPath.value = path
                _avatarVersion.value++
            } catch (_: Exception) {}
        }
    }

    fun deleteAvatar() {
        viewModelScope.launch(Dispatchers.IO) {
            avatarRepo.delete()
            appPrefs.avatarPath = null
            _avatarPath.value = null
            _avatarVersion.value++
        }
    }

    fun setLearningLanguage(language: String) {
        appPrefs.learningLanguage = language
        _learningLanguage.value = language
    }

    fun setNativeLanguage(language: String) {
        appPrefs.nativeLanguage = language
        _nativeLanguage.value = language
    }

    fun orderedLanguages() = appPrefs.orderedLanguages()

    fun seedMockData() {
        viewModelScope.launch {
            listOf(
                "hund" to "dog", "katt" to "cat", "hus" to "house", "bil" to "car",
                "bok" to "book", "vann" to "water", "mat" to "food", "dag" to "day",
                "natt" to "night", "sol" to "sun", "måne" to "moon", "tre" to "tree",
                "blomst" to "flower", "fugl" to "bird", "fisk" to "fish", "himmel" to "sky",
                "fjell" to "mountain", "hav" to "sea", "elv" to "river", "vind" to "wind"
            ).forEach { (word, translation) ->
                val id = dictionaryRepository.addWord(word, translation, "en")
                dictionaryRepository.updateLanguageCode(id, "no")
            }
            _seedDoneEvent.emit(Unit)
        }
    }
}

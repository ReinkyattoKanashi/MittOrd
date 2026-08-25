package com.reiny.mittord.util

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import com.reiny.mittord.domain.model.LANGUAGES
import com.reiny.mittord.domain.model.Language
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update

@Singleton
class AppPreferences @Inject constructor(@ApplicationContext context: Context) {

    private val prefs = context.getSharedPreferences(AppConstants.PREFS_NAME, Context.MODE_PRIVATE)

    // Bumped on every write so screens can react to settings changed elsewhere.
    // The listener is held in a field on purpose: SharedPreferences keeps only a weak
    // reference, and an anonymous one would be collected and silently stop firing.
    private val revision = MutableStateFlow(0)
    private val changeListener = SharedPreferences.OnSharedPreferenceChangeListener { _, _ ->
        revision.update { it + 1 }
    }

    init {
        prefs.registerOnSharedPreferenceChangeListener(changeListener)
    }

    /** Current native language, and again whenever it is changed. */
    val nativeLanguageChanges: Flow<String> =
        revision.map { nativeLanguage }.distinctUntilChanged()

    var avatarPath: String?
        get() = prefs.getString(AppConstants.PREF_AVATAR_PATH, null)?.takeIf { File(it).exists() }
        set(value) = prefs.edit {
            if (value != null) putString(AppConstants.PREF_AVATAR_PATH, value)
            else remove(AppConstants.PREF_AVATAR_PATH)
        }

    var learningLanguage: String
        get() = prefs.getString(AppConstants.PREF_LEARNING_LANG, AppConstants.DEFAULT_LEARNING_LANG)
            ?: AppConstants.DEFAULT_LEARNING_LANG
        set(value) = prefs.edit { putString(AppConstants.PREF_LEARNING_LANG, value) }

    var nativeLanguage: String
        get() = prefs.getString(AppConstants.PREF_NATIVE_LANG, AppConstants.DEFAULT_NATIVE_LANG)
            ?: AppConstants.DEFAULT_NATIVE_LANG
        set(value) = prefs.edit { putString(AppConstants.PREF_NATIVE_LANG, value) }

    var darkThemeOverride: Boolean?
        get() = if (prefs.contains(AppConstants.PREF_DARK_THEME))
            prefs.getBoolean(AppConstants.PREF_DARK_THEME, false) else null
        set(value) = prefs.edit {
            if (value != null) putBoolean(AppConstants.PREF_DARK_THEME, value)
            else remove(AppConstants.PREF_DARK_THEME)
        }

    fun addRecentLanguage(name: String) {
        val current = recentLanguageNames().filter { it != name }
        val updated = (listOf(name) + current).take(5)
        prefs.edit { putString(AppConstants.PREF_RECENT_LANGS, updated.joinToString(",")) }
    }

    // distinctBy: language name is used as a LazyColumn key in the picker,
    // so duplicates (e.g. learning == native) would crash it.
    fun orderedLanguages(allLanguages: List<Language> = LANGUAGES): List<Language> {
        val priority = listOf(learningLanguage, nativeLanguage).distinct()
        val recentFiltered = recentLanguageNames().filter { it !in priority }.distinct().take(5)
        val pinnedNames = priority + recentFiltered
        val pinnedSet = pinnedNames.toSet()
        return (pinnedNames.mapNotNull { name -> allLanguages.find { it.name == name } } +
            allLanguages.filter { it.name !in pinnedSet })
            .distinctBy { it.name }
    }

    private fun recentLanguageNames(): List<String> =
        prefs.getString(AppConstants.PREF_RECENT_LANGS, null)
            ?.split(",")?.filter { it.isNotBlank() }
            ?: emptyList()
}

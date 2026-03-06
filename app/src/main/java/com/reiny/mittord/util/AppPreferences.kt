package com.reiny.mittord.util

import android.content.Context
import androidx.core.content.edit
import com.reiny.mittord.ui.screens.settings.Language
import com.reiny.mittord.ui.screens.settings.LANGUAGES
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AppPreferences @Inject constructor(@ApplicationContext context: Context) {

    private val prefs = context.getSharedPreferences(AppConstants.PREFS_NAME, Context.MODE_PRIVATE)

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

    fun addRecentLanguage(name: String) {
        val current = recentLanguageNames().filter { it != name }
        val updated = (listOf(name) + current).take(5)
        prefs.edit { putString(AppConstants.PREF_RECENT_LANGS, updated.joinToString(",")) }
    }

    fun orderedLanguages(): List<Language> {
        val learning = learningLanguage
        val native = nativeLanguage
        val recent = recentLanguageNames()
        val priority = listOf(learning, native)
        val prioritySet = priority.toSet()
        val recentFiltered = recent.filter { it !in prioritySet }.take(5)
        val pinnedNames = (priority + recentFiltered).toSet()
        return (priority + recentFiltered).mapNotNull { name -> LANGUAGES.find { it.name == name } } +
            LANGUAGES.filter { it.name !in pinnedNames }
    }

    private fun recentLanguageNames(): List<String> =
        prefs.getString(AppConstants.PREF_RECENT_LANGS, null)
            ?.split(",")?.filter { it.isNotBlank() }
            ?: emptyList()
}

package com.reiny.mittord.ui.screens.wordDetail

import android.util.Log
import com.reiny.mittord.BuildConfig
import com.reiny.mittord.ui.screens.settings.LANGUAGES
import com.reiny.mittord.util.AppConstants
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder

private const val TAG = "LangDetect"

val BCP47_TO_LANG_NAME = mapOf(
    "en" to "English", "ru" to "Russian", "no" to "Norwegian",
    "es" to "Spanish", "fr" to "French", "de" to "German",
    "it" to "Italian", "pt" to "Portuguese", "zh" to "Chinese",
    "ja" to "Japanese", "ko" to "Korean", "ar" to "Arabic",
    "tr" to "Turkish", "pl" to "Polish", "nl" to "Dutch",
    "sv" to "Swedish", "da" to "Danish", "fi" to "Finnish",
    "uk" to "Ukrainian", "cs" to "Czech", "ro" to "Romanian",
    "hu" to "Hungarian", "el" to "Greek", "he" to "Hebrew",
    "fa" to "Persian", "hi" to "Hindi", "bn" to "Bengali",
    "id" to "Indonesian", "vi" to "Vietnamese", "th" to "Thai"
)

val LANG_NAME_TO_BCP47 = BCP47_TO_LANG_NAME.entries.associate { (k, v) -> v to k }

fun flagForCode(code: String?): String? =
    BCP47_TO_LANG_NAME[code]?.let { name -> LANGUAGES.find { it.name == name }?.flag }

fun langNameForCode(code: String?): String? =
    BCP47_TO_LANG_NAME[code]

private val CODE_OVERRIDES = mapOf(
    "nb" to "no",
    "nn" to "no",
)

internal fun normalizeCode(raw: String): String =
    CODE_OVERRIDES[raw] ?: if ('-' in raw) raw.substringBefore('-') else raw

suspend fun detectLanguage(text: String): String? {
    val trimmed = text.trim()
    if (trimmed.length < AppConstants.LANG_DETECT_MIN_LENGTH) return null
    return withContext(Dispatchers.IO) {
        try {
            val encoded = URLEncoder.encode(trimmed, "UTF-8")
            val url = URL(AppConstants.TRANSLATE_API_URL + encoded)
            val conn = url.openConnection() as HttpURLConnection
            conn.setRequestProperty("User-Agent", AppConstants.TRANSLATE_USER_AGENT)
            conn.connectTimeout = AppConstants.NETWORK_TIMEOUT_MS
            conn.readTimeout = AppConstants.NETWORK_TIMEOUT_MS
            val response = conn.inputStream.bufferedReader().readText()
            conn.disconnect()
            val raw = Regex("""\],null,"([a-z]{2,3})"""").find(response)
                ?.groupValues?.get(1)
            if (raw == null || raw == AppConstants.LANG_CODE_UNDETERMINED) return@withContext null
            normalizeCode(raw)
        } catch (e: Exception) {
            if (BuildConfig.DEBUG) Log.e(TAG, "detectLanguage: ${e.message}")
            null
        }
    }
}

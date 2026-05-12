package com.reiny.mittord.data.repository

import android.util.Log
import com.reiny.mittord.BuildConfig
import com.reiny.mittord.data.api.TranslateApiService
import com.reiny.mittord.domain.model.Language
import com.reiny.mittord.domain.model.LANGUAGES
import com.reiny.mittord.domain.util.flagForCode
import com.reiny.mittord.domain.util.normalizeCode
import com.reiny.mittord.util.AppConstants
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import javax.inject.Inject

private const val TAG = "TranslateRepo"

interface TranslateRepository {
    suspend fun detectLanguage(text: String): String?
    suspend fun translateText(text: String, targetLanguageCode: String): String?
    suspend fun getSupportedLanguages(): List<Language>
}

class TranslateRepositoryImpl @Inject constructor(
    private val apiService: TranslateApiService
) : TranslateRepository {

    private var cachedLanguages: List<Language>? = null

    override suspend fun detectLanguage(text: String): String? = withContext(Dispatchers.IO) {
        try {
            val body = apiService.detectAndTranslate(
                client = "gtx", sl = "auto", tl = "en", dt = "t", q = text
            )
            val json = JSONArray(body.string())
            val raw = json.optString(2)
            if (raw.isBlank() || raw == AppConstants.LANG_CODE_UNDETERMINED) null
            else normalizeCode(raw)
        } catch (e: Exception) {
            if (BuildConfig.DEBUG) Log.e(TAG, "detectLanguage: ${e.message}")
            null
        }
    }

    override suspend fun translateText(text: String, targetLanguageCode: String): String? =
        withContext(Dispatchers.IO) {
            try {
                val body = apiService.detectAndTranslate(
                    client = "gtx", sl = "auto", tl = targetLanguageCode, dt = "t", q = text
                )
                val json = JSONArray(body.string())
                json.getJSONArray(0).getJSONArray(0).getString(0)
            } catch (e: Exception) {
                if (BuildConfig.DEBUG) Log.e(TAG, "translateText: ${e.message}")
                null
            }
        }

    override suspend fun getSupportedLanguages(): List<Language> {
        cachedLanguages?.let { return it }
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.getSupportedLanguages(client = "gtx", tl = "en")
                val result = response.targetLanguages
                    .filter { it.key != "auto" }
                    .map { (code, name) ->
                        Language(
                            name = name,
                            flag = flagForCode(code) ?: "🌐",
                            code = code
                        )
                    }
                    .sortedBy { it.name }
                if (result.isNotEmpty()) cachedLanguages = result
                result.ifEmpty { LANGUAGES }
            } catch (e: Exception) {
                if (BuildConfig.DEBUG) Log.e(TAG, "getSupportedLanguages: ${e.message}")
                LANGUAGES
            }
        }
    }
}
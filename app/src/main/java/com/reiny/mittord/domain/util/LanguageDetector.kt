package com.reiny.mittord.domain.util

import com.reiny.mittord.domain.usecase.DetectLanguageUseCase
import com.reiny.mittord.util.AppConstants
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Runs language detection for several independent text fields.
 *
 * Every field is identified by a [key], and a new request for the same key cancels the
 * one in flight - so a field never gets the answer for text the user has already
 * changed. Text shorter than [AppConstants.LANG_DETECT_MIN_LENGTH] never reaches the
 * network and reports null right away.
 *
 * Callers decide what null means: the add-word form clears the flag, while the editor
 * keeps the language already stored for the word. That difference used to be buried in
 * nine near-identical copies of this logic across two ViewModels.
 */
class LanguageDetector(
    private val scope: CoroutineScope,
    private val detectLanguage: DetectLanguageUseCase
) {
    private val jobs = mutableMapOf<Any, Job>()

    /**
     * @param debounce true while the user is typing, false for an immediate answer
     *   (picking "auto" in the language sheet, or text handed over by another app).
     * @param onResult receives the detected code, or null when it is unknown.
     */
    fun request(key: Any, text: String, debounce: Boolean, onResult: (String?) -> Unit) {
        cancel(key)
        val trimmed = text.trim()
        if (trimmed.length < AppConstants.LANG_DETECT_MIN_LENGTH) {
            onResult(null)
            return
        }
        jobs[key] = scope.launch {
            if (debounce) delay(AppConstants.LANG_DETECT_DEBOUNCE_MS)
            val code = detectLanguage(trimmed)
            jobs.remove(key)
            onResult(code)
        }
    }

    fun cancel(key: Any) {
        jobs.remove(key)?.cancel()
    }

    fun cancelAll() {
        jobs.values.forEach { it.cancel() }
        jobs.clear()
    }
}

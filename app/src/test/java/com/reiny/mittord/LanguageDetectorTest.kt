package com.reiny.mittord

import com.reiny.mittord.data.repository.TranslateRepository
import com.reiny.mittord.domain.model.Language
import com.reiny.mittord.domain.usecase.DetectLanguageUseCase
import com.reiny.mittord.domain.util.LanguageDetector
import com.reiny.mittord.util.AppConstants
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * The detector guards a network call, so the things worth pinning down are:
 * when it fires, when it does not, and that two fields never overwrite each other.
 *
 * runTest gives a virtual clock: advanceTimeBy moves it without really waiting, so
 * the debounce can be tested in milliseconds of wall time.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class LanguageDetectorTest {

    /** Records every text it was asked about and answers with a fixed code. */
    private class FakeTranslateRepository(private val answer: String? = "no") : TranslateRepository {
        val requested = mutableListOf<String>()
        override suspend fun detectLanguage(text: String): String? {
            requested += text
            return answer
        }

        override suspend fun translateText(text: String, targetLanguageCode: String) = ""
        override suspend fun getSupportedLanguages(): List<Language> = emptyList()
    }

    @Test
    fun `text shorter than the minimum never reaches the network`() = runTest {
        val api = FakeTranslateRepository()
        val detector = LanguageDetector(this, DetectLanguageUseCase(api))
        var result: String? = "untouched"

        detector.request(key = "word", text = "h", debounce = false) { result = it }
        advanceUntilIdle()

        assertTrue(api.requested.isEmpty())
        // Reports null right away so the caller can clear the flag.
        assertNull(result)
    }

    @Test
    fun `typing quickly produces a single request for the last text`() = runTest {
        val api = FakeTranslateRepository()
        val detector = LanguageDetector(this, DetectLanguageUseCase(api))
        var result: String? = null

        detector.request("word", "hu", debounce = true) { result = it }
        advanceTimeBy(AppConstants.LANG_DETECT_DEBOUNCE_MS / 2)
        detector.request("word", "hun", debounce = true) { result = it }
        advanceTimeBy(AppConstants.LANG_DETECT_DEBOUNCE_MS / 2)
        detector.request("word", "hund", debounce = true) { result = it }
        advanceUntilIdle()

        assertEquals(listOf("hund"), api.requested)
        assertEquals("no", result)
    }

    @Test
    fun `debounce false answers without waiting`() = runTest {
        val api = FakeTranslateRepository()
        val detector = LanguageDetector(this, DetectLanguageUseCase(api))
        var result: String? = null

        detector.request("word", "hund", debounce = false) { result = it }
        advanceTimeBy(1)

        assertEquals("no", result)
    }

    @Test
    fun `separate fields do not cancel each other`() = runTest {
        val api = FakeTranslateRepository()
        val detector = LanguageDetector(this, DetectLanguageUseCase(api))
        val answers = mutableMapOf<String, String?>()

        detector.request("word", "hund", debounce = true) { answers["word"] = it }
        detector.request("translation", "dog", debounce = true) { answers["translation"] = it }
        advanceUntilIdle()

        assertEquals(setOf("hund", "dog"), api.requested.toSet())
        assertEquals(2, answers.size)
    }

    @Test
    fun `cancel stops an answer that is still pending`() = runTest {
        val api = FakeTranslateRepository()
        val detector = LanguageDetector(this, DetectLanguageUseCase(api))
        var result: String? = null

        detector.request("word", "hund", debounce = true) { result = it }
        detector.cancel("word")
        advanceUntilIdle()

        assertNull(result)
    }

    @Test
    fun `an unknown language is reported as null, not as an error`() = runTest {
        val api = FakeTranslateRepository(answer = null)
        val detector = LanguageDetector(this, DetectLanguageUseCase(api))
        var called = false
        var result: String? = "untouched"

        detector.request("word", "qqqq", debounce = false) {
            called = true
            result = it
        }
        advanceUntilIdle()

        assertTrue(called)
        assertNull(result)
    }
}

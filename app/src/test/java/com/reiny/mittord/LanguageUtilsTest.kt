package com.reiny.mittord

import com.reiny.mittord.domain.model.LANGUAGES
import com.reiny.mittord.domain.util.BCP47_TO_LANG_NAME
import com.reiny.mittord.domain.util.LANG_NAME_TO_BCP47
import com.reiny.mittord.domain.util.flagForCode
import com.reiny.mittord.domain.util.langNameForCode
import com.reiny.mittord.domain.util.normalizeCode
import org.junit.Assert.*
import org.junit.Test

class LanguageUtilsTest {

    // ---- normalizeCode ----

    @Test fun normalizeCode_nb_returns_no() = assertEquals("no", normalizeCode("nb"))
    @Test fun normalizeCode_nn_returns_no() = assertEquals("no", normalizeCode("nn"))
    @Test fun normalizeCode_zhHant_returns_zh() = assertEquals("zh", normalizeCode("zh-Hant"))
    @Test fun normalizeCode_zhHans_returns_zh() = assertEquals("zh", normalizeCode("zh-Hans"))
    @Test fun normalizeCode_ptBR_returns_pt() = assertEquals("pt", normalizeCode("pt-BR"))
    @Test fun normalizeCode_en_unchanged() = assertEquals("en", normalizeCode("en"))
    @Test fun normalizeCode_ru_unchanged() = assertEquals("ru", normalizeCode("ru"))
    @Test fun normalizeCode_da_unchanged() = assertEquals("da", normalizeCode("da"))

    // ---- flagForCode ----

    @Test fun flagForCode_no_returns_flag() = assertEquals("🇳🇴", flagForCode("no"))
    @Test fun flagForCode_en_returns_flag() = assertEquals("🇺🇸", flagForCode("en"))
    @Test fun flagForCode_ru_returns_flag() = assertEquals("🇷🇺", flagForCode("ru"))
    @Test fun flagForCode_da_returns_flag() = assertEquals("🇩🇰", flagForCode("da"))
    @Test fun flagForCode_sv_returns_flag() = assertEquals("🇸🇪", flagForCode("sv"))
    @Test fun flagForCode_null_returns_null() = assertNull(flagForCode(null))
    @Test fun flagForCode_unknown_returns_null() = assertNull(flagForCode("xx"))

    // ML Kit returns "nb" → normalize → "no" → flag works
    @Test fun flagForCode_nb_after_normalize() =
        assertEquals("🇳🇴", flagForCode(normalizeCode("nb")))

    @Test fun flagForCode_nn_after_normalize() =
        assertEquals("🇳🇴", flagForCode(normalizeCode("nn")))

    @Test fun flagForCode_zhHant_after_normalize() =
        assertEquals("🇨🇳", flagForCode(normalizeCode("zh-Hant")))

    // ---- langNameForCode ----

    @Test fun langNameForCode_no() = assertEquals("Norwegian", langNameForCode("no"))
    @Test fun langNameForCode_ru() = assertEquals("Russian", langNameForCode("ru"))
    @Test fun langNameForCode_null() = assertNull(langNameForCode(null))
    @Test fun langNameForCode_unknown() = assertNull(langNameForCode("xx"))

    // ---- LANG_NAME_TO_BCP47 round-trips ----

    @Test fun roundTrip_Norwegian() = assertEquals("no", LANG_NAME_TO_BCP47["Norwegian"])
    @Test fun roundTrip_Russian() = assertEquals("ru", LANG_NAME_TO_BCP47["Russian"])
    @Test fun roundTrip_English() = assertEquals("en", LANG_NAME_TO_BCP47["English"])
    @Test fun roundTrip_Danish() = assertEquals("da", LANG_NAME_TO_BCP47["Danish"])
    @Test fun roundTrip_Swedish() = assertEquals("sv", LANG_NAME_TO_BCP47["Swedish"])

    // ---- Every code in the map must have a flag ----

    @Test
    fun allBcp47CodesHaveMatchingFlag() {
        val missing = BCP47_TO_LANG_NAME.keys.filter { flagForCode(it) == null }
        assertTrue("Codes without a flag: $missing", missing.isEmpty())
    }

    // ---- Every Language in LANGUAGES must have a reverse BCP47 mapping ----

    @Test
    fun allLanguagesHaveBcp47Code() {
        val missing = LANGUAGES.filter { LANG_NAME_TO_BCP47[it.name] == null }
        assertTrue("Languages without BCP47: ${missing.map { it.name }}", missing.isEmpty())
    }
}

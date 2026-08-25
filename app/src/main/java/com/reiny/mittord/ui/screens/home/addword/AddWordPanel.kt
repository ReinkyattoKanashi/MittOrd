package com.reiny.mittord.ui.screens.home.addword

import androidx.annotation.StringRes
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.reiny.mittord.R
import com.reiny.mittord.domain.model.Language
import com.reiny.mittord.domain.util.bcp47
import com.reiny.mittord.domain.util.langNameForCode
import com.reiny.mittord.ui.screens.home.components.AddWordState
import com.reiny.mittord.ui.screens.home.components.LanguageFlagButton
import com.reiny.mittord.ui.screens.home.components.RoundedPrimaryButton
import com.reiny.mittord.ui.screens.home.components.TranslateButton
import com.reiny.mittord.ui.screens.home.components.WordInputField
import com.reiny.mittord.ui.screens.settings.LanguagePickerSheet
import com.reiny.mittord.ui.theme.Theme
import com.reiny.mittord.ui.theme.typography
import kotlinx.coroutines.delay

/** Which language picker the panel is currently showing, if any. */
private sealed interface PickerRequest {
    @get:StringRes
    val titleRes: Int

    data object WordLanguage : PickerRequest {
        override val titleRes = R.string.picker_word_language
    }

    data object TranslationLanguage : PickerRequest {
        override val titleRes = R.string.picker_translation_language
    }

    /** Not a language choice but a target: picking one translates the word into it. */
    data object TranslateInto : PickerRequest {
        override val titleRes = R.string.picker_translate_into
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun AddWordPanel(
    isExpanded: Boolean,
    addWord: AddWordState,
    orderedLanguages: List<Language>,
    addRecentLanguage: (String) -> Unit
) {
    val input = addWord.state()
    val wordFocusRequester = remember { FocusRequester() }
    var picker by remember { mutableStateOf<PickerRequest?>(null) }

    LaunchedEffect(isExpanded) {
        if (isExpanded) {
            delay(AddWordCascade.FOCUS_DELAY.toLong())
            // The panel can be collapsed again before the delay is over.
            runCatching { wordFocusRequester.requestFocus() }
        }
    }

    Column(
        modifier = Modifier.fillMaxHeight().imePadding(),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        AnimatedVisibility(
            visible = isExpanded,
            enter = fadeIn(tween(AddWordCascade.FADE_IN, AddWordCascade.TITLE_DELAY)) +
                slideInVertically(tween(AddWordCascade.FADE_IN, AddWordCascade.TITLE_DELAY)) {
                    it / AddWordCascade.TITLE_SLIDE_DIVISOR
                },
            exit = fadeOut(tween(AddWordCascade.FADE_OUT))
        ) {
            Text(
                stringResource(R.string.nav_add_word_title),
                modifier = Modifier.padding(16.dp),
                color = MaterialTheme.colorScheme.onSurface,
                style = Theme.typography.h1
            )
        }

        // Word field: [FlagButton] [InputField]
        AnimatedVisibility(
            visible = isExpanded,
            enter = fadeIn(tween(AddWordCascade.FADE_IN, AddWordCascade.WORD_FIELD_DELAY)) +
                slideInVertically(tween(AddWordCascade.FADE_IN, AddWordCascade.WORD_FIELD_DELAY)) {
                    it / AddWordCascade.FIELD_SLIDE_DIVISOR
                },
            exit = fadeOut(tween(AddWordCascade.FADE_OUT))
        ) {
            Row(
                modifier = Modifier.padding(start = 24.dp, end = 24.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                LanguageFlagButton(
                    languageCode = input.wordLanguageCode,
                    isAuto = input.wordLanguageIsAuto,
                    onClick = { picker = PickerRequest.WordLanguage }
                )
                Spacer(Modifier.width(8.dp))
                WordInputField(
                    modifier = Modifier
                        .weight(1f)
                        .focusRequester(wordFocusRequester),
                    value = input.word,
                    onValueChange = addWord.onWordChange,
                    placeholder = stringResource(R.string.placeholder_word)
                )
            }
        }

        // Translation field: [FlagButton] [InputField] [TranslateButton]
        AnimatedVisibility(
            visible = isExpanded,
            enter = fadeIn(tween(AddWordCascade.FADE_IN, AddWordCascade.TRANSLATION_FIELD_DELAY)) +
                slideInVertically(
                    tween(AddWordCascade.FADE_IN, AddWordCascade.TRANSLATION_FIELD_DELAY)
                ) { it / AddWordCascade.FIELD_SLIDE_DIVISOR },
            exit = fadeOut(tween(AddWordCascade.FADE_OUT))
        ) {
            Row(
                modifier = Modifier.padding(start = 24.dp, end = 24.dp, top = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                LanguageFlagButton(
                    languageCode = input.translationLanguageCode,
                    isAuto = input.translationLanguageIsAuto,
                    onClick = { picker = PickerRequest.TranslationLanguage }
                )
                Spacer(Modifier.width(8.dp))
                WordInputField(
                    modifier = Modifier.weight(1f),
                    value = input.translation,
                    onValueChange = addWord.onTranslationChange,
                    placeholder = stringResource(R.string.placeholder_translation)
                )
                TranslateButton(
                    onClick = { picker = PickerRequest.TranslateInto },
                    isLoading = input.isTranslating
                )
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        AnimatedVisibility(
            visible = isExpanded,
            enter = fadeIn(tween(AddWordCascade.SUBMIT_FADE_IN, AddWordCascade.SUBMIT_DELAY)),
            exit = fadeOut(tween(AddWordCascade.FADE_OUT))
        ) {
            RoundedPrimaryButton(
                modifier = Modifier.padding(bottom = 2.dp),
                text = stringResource(R.string.btn_add),
                onClick = addWord.onAddWord,
                enabled = input.word.isNotBlank()
            )
        }
    }

    picker?.let { request ->
        val shownCode = if (request is PickerRequest.WordLanguage) {
            input.wordLanguageCode
        } else {
            input.translationLanguageCode
        }
        LanguagePickerSheet(
            title = stringResource(request.titleRes),
            selected = langNameForCode(shownCode).orEmpty(),
            isAutoSelected = when (request) {
                PickerRequest.WordLanguage -> input.wordLanguageIsAuto
                PickerRequest.TranslationLanguage -> input.translationLanguageIsAuto
                PickerRequest.TranslateInto -> false
            },
            showAutoOption = request != PickerRequest.TranslateInto,
            orderedLanguages = orderedLanguages,
            onSelect = { language ->
                language?.let { addRecentLanguage(it.name) }
                when (request) {
                    // null means "auto detect" here
                    PickerRequest.WordLanguage ->
                        addWord.onWordLanguageSelected(language?.bcp47())

                    PickerRequest.TranslationLanguage ->
                        addWord.onTranslationLanguageSelected(language?.bcp47())

                    // the API needs some target, so fall back to the plain name
                    PickerRequest.TranslateInto ->
                        language?.let { addWord.onTranslateTranslation(it.bcp47() ?: it.name) }
                }
                picker = null
            },
            onDismiss = { picker = null }
        )
    }
}

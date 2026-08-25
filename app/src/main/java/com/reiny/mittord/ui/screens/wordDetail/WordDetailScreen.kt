package com.reiny.mittord.ui.screens.wordDetail

import androidx.activity.compose.BackHandler
import androidx.annotation.StringRes
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.draw.clip
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import coil.compose.AsyncImage
import com.reiny.mittord.R
import com.reiny.mittord.ui.screens.home.components.LanguageFlagButton
import com.reiny.mittord.ui.screens.home.components.RoundedPrimaryButton
import com.reiny.mittord.ui.screens.home.components.TranslateButton
import com.reiny.mittord.ui.screens.home.components.WordInputField
import com.reiny.mittord.domain.util.bcp47
import com.reiny.mittord.domain.util.langNameForCode
import com.reiny.mittord.ui.screens.settings.LanguagePickerSheet
import com.reiny.mittord.ui.theme.Theme
import com.reiny.mittord.ui.theme.typography
import com.reiny.mittord.util.AppConstants
import kotlinx.coroutines.delay
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WordDetailScreen(
    onBack: () -> Unit,
    viewModel: WordDetailViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    val orderedLanguages by viewModel.orderedLanguages.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    var picker by remember { mutableStateOf<PickerRequest?>(null) }
    var showDiscardDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }

    val handleBack = {
        if (viewModel.hasUnsavedChanges) showDiscardDialog = true else onBack()
    }

    BackHandler(onBack = handleBack)

    // Deliberately a stable map: the focus effect below is keyed on Unit, so it has to
    // reach requesters created long after it started.
    val translationFocusRequesters = remember { mutableMapOf<Int, FocusRequester>() }

    LaunchedEffect(Unit) {
        viewModel.focusTranslation.collect { index ->
            delay(AppConstants.FOCUS_REQUEST_DELAY_MS)
            // The row can be gone again before the delay is over.
            runCatching { translationFocusRequesters[index]?.requestFocus() }
        }
    }

    val errorTranslationFailed = stringResource(R.string.error_translation_failed)
    val errorImageSaveFailed = stringResource(R.string.error_image_save_failed)
    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                WordDetailEvent.Saved, WordDetailEvent.Deleted -> onBack()
                WordDetailEvent.TranslationFailed ->
                    snackbarHostState.showSnackbar(errorTranslationFailed)

                WordDetailEvent.ImageSaveFailed ->
                    snackbarHostState.showSnackbar(errorImageSaveFailed)
            }
        }
    }

    val imageLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let { viewModel.saveImage(it) }
    }

    val cdBack = stringResource(R.string.cd_back)
    val cdDelete = stringResource(R.string.cd_delete)
    val screenTitle = stringResource(R.string.screen_edit_word)
    val placeholderWord = stringResource(R.string.placeholder_word)
    val placeholderTranslation = stringResource(R.string.placeholder_translation)
    val cdRemoveTranslation = stringResource(R.string.cd_remove_translation)
    val btnAddTranslation = stringResource(R.string.btn_add_translation)
    val placeholderComment = stringResource(R.string.placeholder_comment)
    val btnAddPhoto = stringResource(R.string.btn_add_photo)
    val cdWordPhoto = stringResource(R.string.cd_word_photo)
    val cdRemovePhoto = stringResource(R.string.cd_remove_photo)
    val btnSave = stringResource(R.string.btn_save)
    val dialogTitle = stringResource(R.string.dialog_unsaved_title)
    val dialogMessage = stringResource(R.string.dialog_unsaved_message)
    val dialogSave = stringResource(R.string.btn_save)
    val dialogDiscard = stringResource(R.string.dialog_discard)
    val deleteDialogTitle = stringResource(R.string.dialog_delete_title)
    val deleteDialogMessage = stringResource(R.string.dialog_delete_message)
    val btnCancel = stringResource(R.string.btn_cancel)

    Scaffold(
        topBar = {
            TopAppBar(
                title = {},
                navigationIcon = {
                    IconButton(onClick = handleBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = cdBack)
                    }
                },
                actions = {
                    IconButton(onClick = { showDeleteDialog = true }) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = cdDelete,
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = MaterialTheme.colorScheme.background,
        contentWindowInsets = WindowInsets(0)
    ) { innerPadding ->
        if (state.isLoading) return@Scaffold

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Spacer(Modifier.height(16.dp))

            Text(
                text = screenTitle,
                style = Theme.typography.h1,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(Modifier.height(16.dp))

            // Word field: [FlagButton] [InputField]
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                LanguageFlagButton(
                    languageCode = state.wordLanguageCode,
                    isAuto = state.wordLanguageIsAuto,
                    onClick = { picker = PickerRequest.WordLanguage }
                )
                Spacer(Modifier.width(8.dp))
                WordInputField(
                    modifier = Modifier.weight(1f),
                    value = state.word,
                    onValueChange = viewModel::onWordChange,
                    placeholder = placeholderWord
                )
            }

            Spacer(Modifier.height(16.dp))

            // Translation fields: [FlagButton] [InputField] [TranslateButton] [DeleteButton?]
            state.translations.forEachIndexed { i, entry ->
                if (i > 0) Spacer(Modifier.height(10.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    LanguageFlagButton(
                        languageCode = entry.languageCode,
                        isAuto = entry.isAuto,
                        onClick = { picker = PickerRequest.TranslationLanguage(i) }
                    )
                    Spacer(Modifier.width(8.dp))
                    WordInputField(
                        modifier = Modifier
                            .weight(1f)
                            .focusRequester(
                                translationFocusRequesters.getOrPut(i) { FocusRequester() }
                            ),
                        value = entry.text,
                        onValueChange = { viewModel.onTranslationChange(i, it) },
                        placeholder = placeholderTranslation
                    )
                    TranslateButton(
                        onClick = { picker = PickerRequest.TranslateInto(i) },
                        isLoading = entry.isTranslating
                    )
                    if (state.translations.size > 1) {
                        IconButton(onClick = { viewModel.removeTranslation(i) }) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = cdRemoveTranslation,
                                modifier = Modifier.size(18.dp),
                                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(8.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    onClick = { viewModel.addTranslation() },
                    color = MaterialTheme.colorScheme.background,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(vertical = 6.dp, horizontal = 2.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(Modifier.size(6.dp))
                        Text(
                            text = btnAddTranslation,
                            style = Theme.typography.caption,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            Spacer(Modifier.height(12.dp))

            WordInputField(
                modifier = Modifier.fillMaxWidth(),
                value = state.comment,
                onValueChange = viewModel::onCommentChange,
                placeholder = placeholderComment,
                icon = Icons.Default.Edit,
                singleLine = false
            )

            Spacer(Modifier.height(20.dp))

            if (state.imagePath == null) {
                Surface(
                    onClick = { imageLauncher.launch(AppConstants.MIME_IMAGE_ALL) },
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = btnAddPhoto,
                        style = Theme.typography.body,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp)
                    )
                }
            } else {
                val imagePath = state.imagePath
                Box(modifier = Modifier.fillMaxWidth()) {
                    AsyncImage(
                        model = File(imagePath),
                        contentDescription = cdWordPhoto,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                            .clip(RoundedCornerShape(16.dp)),
                        contentScale = ContentScale.Crop
                    )
                    Surface(
                        onClick = { viewModel.removeImage() },
                        shape = RoundedCornerShape(50),
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(8.dp)
                            .size(28.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = cdRemovePhoto,
                                modifier = Modifier.size(16.dp),
                                tint = MaterialTheme.colorScheme.onError
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(20.dp))

            RoundedPrimaryButton(
                text = btnSave,
                onClick = { viewModel.save() },
                enabled = state.word.isNotBlank()
            )
        }
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = {
                Text(
                    text = deleteDialogTitle,
                    style = Theme.typography.h2,
                    color = MaterialTheme.colorScheme.onSurface
                )
            },
            text = {
                Text(
                    text = deleteDialogMessage,
                    style = Theme.typography.body,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                )
            },
            confirmButton = {
                TextButton(onClick = { viewModel.delete() }) {
                    Text(cdDelete, color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text(btnCancel, color = MaterialTheme.colorScheme.onSurface)
                }
            },
            containerColor = MaterialTheme.colorScheme.surface
        )
    }

    if (showDiscardDialog) {
        AlertDialog(
            onDismissRequest = { showDiscardDialog = false },
            title = {
                Text(
                    text = dialogTitle,
                    style = Theme.typography.h2,
                    color = MaterialTheme.colorScheme.onSurface
                )
            },
            text = {
                Text(
                    text = dialogMessage,
                    style = Theme.typography.body,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                )
            },
            confirmButton = {
                TextButton(onClick = { viewModel.save() }) {
                    Text(dialogSave, color = MaterialTheme.colorScheme.primary)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDiscardDialog = false; onBack() }) {
                    Text(dialogDiscard, color = MaterialTheme.colorScheme.error)
                }
            },
            containerColor = MaterialTheme.colorScheme.surface
        )
    }

    picker?.let { request ->
        val index = when (request) {
            PickerRequest.WordLanguage -> null
            is PickerRequest.TranslationLanguage -> request.index
            is PickerRequest.TranslateInto -> request.index
        }
        val entry = index?.let { state.translations.getOrNull(it) }
        if (index != null && entry == null) {
            picker = null
            return@let
        }
        LanguagePickerSheet(
            title = stringResource(request.titleRes),
            selected = langNameForCode(
                if (request is PickerRequest.WordLanguage) state.wordLanguageCode
                else entry?.languageCode
            ).orEmpty(),
            isAutoSelected = when (request) {
                PickerRequest.WordLanguage -> state.wordLanguageIsAuto
                is PickerRequest.TranslationLanguage -> entry?.isAuto == true
                is PickerRequest.TranslateInto -> false
            },
            showAutoOption = request !is PickerRequest.TranslateInto,
            orderedLanguages = orderedLanguages,
            onSelect = { language ->
                language?.let { viewModel.addRecentLanguage(it.name) }
                when (request) {
                    // null means "auto detect" here
                    PickerRequest.WordLanguage ->
                        viewModel.onWordLanguageSelected(language?.bcp47())

                    is PickerRequest.TranslationLanguage ->
                        viewModel.onTranslationLanguageSelected(request.index, language?.bcp47())

                    // the API needs some target, so fall back to the plain name
                    is PickerRequest.TranslateInto ->
                        language?.let {
                            viewModel.translateTranslation(request.index, it.bcp47() ?: it.name)
                        }
                }
                picker = null
            },
            onDismiss = { picker = null }
        )
    }
}

/** Which language picker the editor is showing, if any. */
private sealed interface PickerRequest {
    @get:StringRes
    val titleRes: Int

    data object WordLanguage : PickerRequest {
        override val titleRes = R.string.picker_word_language
    }

    data class TranslationLanguage(val index: Int) : PickerRequest {
        override val titleRes = R.string.picker_translation_language
    }

    /** Not a language choice but a target: picking one translates the word into it. */
    data class TranslateInto(val index: Int) : PickerRequest {
        override val titleRes = R.string.picker_translate_into
    }
}

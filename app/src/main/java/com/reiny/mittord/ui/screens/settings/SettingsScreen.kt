package com.reiny.mittord.ui.screens.settings

import android.content.Intent
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import coil.compose.AsyncImage
import coil.request.CachePolicy
import coil.request.ImageRequest
import com.reiny.mittord.BuildConfig
import com.reiny.mittord.R
import com.reiny.mittord.ui.screens.settings.components.AppInfoBlock
import com.reiny.mittord.ui.screens.settings.components.ClearWordsDialog
import com.reiny.mittord.ui.screens.settings.components.ProfileCard
import com.reiny.mittord.ui.theme.Theme
import com.reiny.mittord.ui.theme.typography
import com.reiny.mittord.util.AppConstants
import java.io.File

@Composable
fun SettingsScreen(
    onBackClick: () -> Unit,
    isDarkTheme: Boolean,
    onDarkThemeChange: (Boolean) -> Unit,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    val imageToCrop by viewModel.imageToCrop.collectAsState()

    var picker by remember { mutableStateOf<LanguageTarget?>(null) }
    var showClearWordsDialog by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val mockDataAdded = stringResource(R.string.mock_data_added)
    val wordsCleared = stringResource(R.string.words_cleared)
    val githubUrl = stringResource(R.string.settings_github_url)
    val noBrowser = stringResource(R.string.error_no_browser)
    val avatarFailed = stringResource(R.string.error_avatar_failed)

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            val message = when (event) {
                SettingsEvent.MockDataAdded -> mockDataAdded
                SettingsEvent.WordsCleared -> wordsCleared
                SettingsEvent.AvatarFailed -> avatarFailed
            }
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
        }
    }

    val imagePicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let(viewModel::onAvatarPicked)
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        contentWindowInsets = WindowInsets(0)
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.background)
                    .windowInsetsPadding(WindowInsets.statusBars)
                    .padding(start = 4.dp, top = 4.dp, end = 16.dp, bottom = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBackClick) {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = stringResource(R.string.cd_back),
                        tint = MaterialTheme.colorScheme.onBackground
                    )
                }
                Text(
                    text = stringResource(R.string.screen_profile),
                    style = Theme.typography.h2,
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.padding(start = 4.dp)
                )
            }

            Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                Spacer(Modifier.height(8.dp))

                ProfileCard(
                    avatarPath = state.avatarPath,
                    avatarVersion = state.avatarVersion,
                    hasAvatar = state.avatarPath != null,
                    onAvatarClick = { imagePicker.launch(AppConstants.MIME_IMAGE_ALL) },
                    onAvatarDelete = viewModel::deleteAvatar
                )

                Spacer(Modifier.height(20.dp))

                SettingsGroup(stringResource(R.string.settings_appearance)) {
                    SettingsToggleRow(
                        icon = Icons.Default.Settings,
                        label = stringResource(R.string.settings_dark_theme),
                        checked = isDarkTheme,
                        onCheckedChange = onDarkThemeChange
                    )
                }

                Spacer(Modifier.height(20.dp))

                SettingsGroup(stringResource(R.string.settings_languages)) {
                    SettingsNavRow(
                        label = stringResource(R.string.settings_learning_language),
                        value = state.learningLanguage,
                        onClick = { picker = LanguageTarget.Learning }
                    )
                    HorizontalDivider(
                        modifier = Modifier.padding(horizontal = 16.dp),
                        color = MaterialTheme.colorScheme.outline
                    )
                    SettingsNavRow(
                        label = stringResource(R.string.settings_native_language),
                        value = state.nativeLanguage,
                        onClick = { picker = LanguageTarget.Native }
                    )
                }

                Spacer(Modifier.height(20.dp))

                SettingsGroup(stringResource(R.string.settings_general)) {
                    SettingsToggleRow(
                        icon = Icons.Default.Notifications,
                        label = stringResource(R.string.settings_notifications),
                        checked = false,
                        onCheckedChange = {}
                    )
                }

                Spacer(Modifier.height(20.dp))

                // Debug-only: BuildConfig.DEBUG is a compile-time constant, so R8 drops
                // this whole block from release builds.
                if (BuildConfig.DEBUG) {
                    SettingsGroup(stringResource(R.string.settings_developer)) {
                        SettingsNavRow(
                            label = stringResource(R.string.settings_add_mock_data),
                            value = "",
                            onClick = viewModel::seedMockData
                        )
                        HorizontalDivider(
                            modifier = Modifier.padding(horizontal = 16.dp),
                            color = MaterialTheme.colorScheme.outline
                        )
                        SettingsNavRow(
                            label = stringResource(R.string.settings_clear_words),
                            value = "",
                            onClick = { showClearWordsDialog = true }
                        )
                    }

                    Spacer(Modifier.height(20.dp))
                }

                AppInfoBlock(
                    onAuthorClick = {
                        val intent = Intent(Intent.ACTION_VIEW, githubUrl.toUri())
                        runCatching { context.startActivity(intent) }.onFailure {
                            Toast.makeText(context, noBrowser, Toast.LENGTH_SHORT).show()
                        }
                    }
                )

                Spacer(Modifier.height(32.dp))
            }
        }
    }

    imageToCrop?.let { source ->
        AvatarCropDialog(
            sourceBitmap = source.asImageBitmap(),
            onConfirm = { cropped -> viewModel.saveAvatarBitmap(cropped.asAndroidBitmap()) },
            onCancel = viewModel::onCropCancelled
        )
    }

    if (showClearWordsDialog) {
        ClearWordsDialog(
            onConfirm = {
                viewModel.clearAllWords()
                showClearWordsDialog = false
            },
            onDismiss = { showClearWordsDialog = false }
        )
    }

    picker?.let { target ->
        LanguagePickerSheet(
            title = stringResource(
                when (target) {
                    LanguageTarget.Learning -> R.string.settings_learning_language
                    LanguageTarget.Native -> R.string.settings_native_language
                }
            ),
            selected = when (target) {
                LanguageTarget.Learning -> state.learningLanguage
                LanguageTarget.Native -> state.nativeLanguage
            },
            // the counterpart is filtered out, so the two can never collide
            orderedLanguages = viewModel.languagesFor(target),
            onSelect = { language ->
                language?.let { viewModel.setLanguage(target, it.name) }
                picker = null
            },
            onDismiss = { picker = null }
        )
    }
}

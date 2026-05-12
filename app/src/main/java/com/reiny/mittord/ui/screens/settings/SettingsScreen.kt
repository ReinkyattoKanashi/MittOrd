package com.reiny.mittord.ui.screens.settings

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import android.widget.Toast
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Settings
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import coil.request.CachePolicy
import coil.request.ImageRequest
import com.reiny.mittord.BuildConfig
import com.reiny.mittord.R
import com.reiny.mittord.ui.theme.Theme
import com.reiny.mittord.ui.theme.typography
import com.reiny.mittord.util.AppConstants
import java.io.File
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun SettingsScreen(
    onBackClick: () -> Unit,
    isDarkTheme: Boolean,
    onDarkThemeChange: (Boolean) -> Unit,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val avatarPath by viewModel.avatarPath.collectAsState()
    val avatarVersion by viewModel.avatarVersion.collectAsState()
    val learningLanguage by viewModel.learningLanguage.collectAsState()
    val nativeLanguage by viewModel.nativeLanguage.collectAsState()

    var showLearningPicker by remember { mutableStateOf(false) }
    var showNativePicker by remember { mutableStateOf(false) }
    var imageToCrop by remember { mutableStateOf<ImageBitmap?>(null) }

    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        viewModel.seedDoneEvent.collect {
            Toast.makeText(context, context.getString(R.string.mock_data_added), Toast.LENGTH_SHORT).show()
        }
    }

    val imagePicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let { selectedUri ->
            val mimeType = context.contentResolver.getType(selectedUri)
            if (mimeType == AppConstants.MIME_IMAGE_GIF) {
                viewModel.saveAvatarGif(selectedUri)
            } else {
                coroutineScope.launch {
                    val loaded = withContext(Dispatchers.IO) {
                        try {
                            context.contentResolver.openInputStream(selectedUri)?.use { stream ->
                                android.graphics.BitmapFactory.decodeStream(stream)?.asImageBitmap()
                            }
                        } catch (_: Exception) { null }
                    }
                    loaded?.let { imageToCrop = it }
                }
            }
        }
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
                        Icons.Default.ArrowBack,
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
                    avatarPath = avatarPath,
                    avatarVersion = avatarVersion,
                    hasAvatar = avatarPath != null,
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
                        value = learningLanguage,
                        onClick = { showLearningPicker = true }
                    )
                    HorizontalDivider(
                        modifier = Modifier.padding(horizontal = 16.dp),
                        color = MaterialTheme.colorScheme.outline
                    )
                    SettingsNavRow(
                        label = stringResource(R.string.settings_native_language),
                        value = nativeLanguage,
                        onClick = { showNativePicker = true }
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

                SettingsGroup(stringResource(R.string.settings_developer)) {
                    SettingsNavRow(
                        label = stringResource(R.string.settings_add_mock_data),
                        value = "",
                        onClick = viewModel::seedMockData
                    )
                }

                Spacer(Modifier.height(20.dp))

                SettingsGroup(stringResource(R.string.settings_about)) {
                    SettingsInfoRow(
                        icon = Icons.Default.Info,
                        label = stringResource(R.string.settings_version),
                        value = BuildConfig.VERSION_NAME
                    )
                    HorizontalDivider(
                        modifier = Modifier.padding(horizontal = 16.dp),
                        color = MaterialTheme.colorScheme.outline
                    )
                    SettingsInfoRow(
                        icon = Icons.Default.Info,
                        label = stringResource(R.string.settings_app_label),
                        value = stringResource(R.string.settings_app_name)
                    )
                }

                Spacer(Modifier.height(32.dp))
            }
        }
    }

    imageToCrop?.let { src ->
        AvatarCropDialog(
            sourceBitmap = src,
            onConfirm = { croppedBitmap ->
                viewModel.saveAvatarBitmap(croppedBitmap.asAndroidBitmap())
                imageToCrop = null
            },
            onCancel = { imageToCrop = null }
        )
    }

    val learningPickerTitle = stringResource(R.string.settings_learning_language)
    val nativePickerTitle = stringResource(R.string.settings_native_language)

    if (showLearningPicker) {
        LanguagePickerSheet(
            title = learningPickerTitle,
            selected = learningLanguage,
            orderedLanguages = viewModel.orderedLanguages(),
            onSelect = { it?.let { lang -> viewModel.setLearningLanguage(lang.name) }; showLearningPicker = false },
            onDismiss = { showLearningPicker = false }
        )
    }

    if (showNativePicker) {
        LanguagePickerSheet(
            title = nativePickerTitle,
            selected = nativeLanguage,
            orderedLanguages = viewModel.orderedLanguages(),
            onSelect = { it?.let { lang -> viewModel.setNativeLanguage(lang.name) }; showNativePicker = false },
            onDismiss = { showNativePicker = false }
        )
    }
}

@Composable
private fun ProfileCard(
    avatarPath: String?,
    avatarVersion: Int,
    hasAvatar: Boolean,
    onAvatarClick: () -> Unit,
    onAvatarDelete: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 2.dp,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(modifier = Modifier.size(88.dp)) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary)
                        .clickable(onClick = onAvatarClick),
                    contentAlignment = Alignment.Center
                ) {
                    if (avatarPath != null) {
                        AsyncImage(
                            model = ImageRequest.Builder(LocalContext.current)
                                .data(File(avatarPath))
                                .memoryCacheKey("avatar-$avatarVersion")
                                .diskCachePolicy(CachePolicy.DISABLED)
                                .build(),
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Text(
                            text = "L",
                            style = Theme.typography.h1,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .align(Alignment.BottomEnd)
                        .border(1.5.dp, MaterialTheme.colorScheme.outlineVariant, CircleShape)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surface)
                        .clickable(onClick = onAvatarClick),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp),
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }

                if (hasAvatar) {
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .align(Alignment.TopEnd)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.error)
                            .clickable(onClick = onAvatarDelete),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = stringResource(R.string.cd_delete_avatar),
                            modifier = Modifier.size(12.dp),
                            tint = MaterialTheme.colorScheme.onError
                        )
                    }
                }
            }

            Spacer(Modifier.height(16.dp))
            Text(
                text = stringResource(R.string.profile_name),
                style = Theme.typography.h2,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = stringResource(R.string.profile_subtitle),
                style = Theme.typography.caption,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f)
            )
        }
    }
}

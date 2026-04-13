package me.pecos.memozy.presentation.screen.settings

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import androidx.compose.foundation.background
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalRippleConfiguration
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.RippleConfiguration
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.res.painterResource
import kotlinx.coroutines.launch
import me.pecos.memozy.data.datasource.remote.auth.AuthState
import me.pecos.memozy.feature.core.resource.R
import me.pecos.memozy.feature.home.impl.BuildConfig
import me.pecos.memozy.presentation.components.AppPopup
import me.pecos.memozy.presentation.components.PopupActionArea
import me.pecos.memozy.presentation.components.PopupNavigation
import me.pecos.memozy.presentation.components.PopupSize
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.offset
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import kotlin.math.roundToInt
import me.pecos.memozy.presentation.theme.AppFontFamily
import me.pecos.memozy.presentation.theme.FontSizeLevel
import me.pecos.memozy.presentation.theme.LocalActivity
import me.pecos.memozy.presentation.theme.LocalAppColors
import me.pecos.memozy.presentation.theme.LocalFontSettings

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onBack: () -> Unit = {},
    onDonation: () -> Unit = {},
    onTrash: () -> Unit = {},
    settingsViewModel: SettingsViewModel = viewModel()
) {
    var showClearDialog by remember { mutableStateOf(false) }
    var showLicenseDialog by remember { mutableStateOf(false) }
    var showLanguageDialog by remember { mutableStateOf(false) }
    var showThemeDialog by remember { mutableStateOf(false) }
    var showFontDialog by remember { mutableStateOf(false) }
    var showRestoreDialog by remember { mutableStateOf(false) }
    var showSignOutDialog by remember { mutableStateOf(false) }
    var showCloudRestoreConfirm by remember { mutableStateOf(false) }

    val selectedLanguage by settingsViewModel.selectedLanguage.collectAsState()
    val selectedTheme by settingsViewModel.selectedTheme.collectAsState()
    val selectedFontFamily by settingsViewModel.selectedFontFamily.collectAsState()
    val selectedFontSize by settingsViewModel.selectedFontSize.collectAsState()
    val backupResult by settingsViewModel.backupResult.collectAsState()
    val isDonationEnabled by settingsViewModel.isDonationEnabled.collectAsState()
    val authState by settingsViewModel.authState.collectAsState()
    val cloudBackupState by settingsViewModel.cloudBackupState.collectAsState()
    val lastBackupTime by settingsViewModel.lastBackupTime.collectAsState()
    val colors = LocalAppColors.current
    val fontSettings = LocalFontSettings.current
    val context = LocalContext.current
    val activity = LocalActivity.current
    val scope = rememberCoroutineScope()

    // 로그인 상태 변경 시 마지막 백업 시간 로드
    LaunchedEffect(authState) {
        if (authState is AuthState.Authenticated) {
            settingsViewModel.loadLastBackupTime()
        }
    }

    // SAF launchers
    val exportLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("application/json")
    ) { uri -> if (uri != null) settingsViewModel.exportBackup(uri) }

    val importLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri -> if (uri != null) settingsViewModel.importBackup(uri) }

    // 백업 결과 토스트
    LaunchedEffect(backupResult) {
        when (val result = backupResult) {
            is BackupResult.Success -> {
                Toast.makeText(
                    context,
                    context.getString(R.string.backup_success, result.message.toIntOrNull() ?: 0),
                    Toast.LENGTH_SHORT
                ).show()
                settingsViewModel.clearBackupResult()
            }

            is BackupResult.Error -> {
                Toast.makeText(
                    context,
                    context.getString(R.string.backup_error),
                    Toast.LENGTH_SHORT
                ).show()
                settingsViewModel.clearBackupResult()
            }

            else -> {}
        }
    }

    // 클라우드 백업 결과 토스트
    LaunchedEffect(cloudBackupState) {
        when (val state = cloudBackupState) {
            is CloudBackupState.UploadSuccess -> {
                Toast.makeText(
                    context,
                    context.getString(R.string.cloud_backup_success, state.memoCount),
                    Toast.LENGTH_SHORT
                ).show()
                settingsViewModel.clearCloudBackupState()
            }
            is CloudBackupState.RestoreSuccess -> {
                Toast.makeText(
                    context,
                    context.getString(R.string.cloud_backup_restore_success, state.memoCount),
                    Toast.LENGTH_SHORT
                ).show()
                settingsViewModel.clearCloudBackupState()
            }
            is CloudBackupState.Error -> {
                Toast.makeText(context, state.message, Toast.LENGTH_SHORT).show()
                settingsViewModel.clearCloudBackupState()
            }
            else -> {}
        }
    }

    val versionName = remember {
        context.packageManager.getPackageInfo(context.packageName, 0).versionName
    }

    if (showThemeDialog) {
        val themeOptions = listOf(
            ThemeMode.LIGHT to stringResource(R.string.theme_light),
            ThemeMode.DARK to stringResource(R.string.theme_dark),
            ThemeMode.SYSTEM to stringResource(R.string.theme_system),
        )
        AppPopup(
            onDismissRequest = { showThemeDialog = false },
            title = stringResource(R.string.theme_settings),
            navigation = PopupNavigation.EMPHASIZED,
            size = PopupSize.MEDIUM,
            actionArea = PopupActionArea.NONE
        ) {
            Column {
                themeOptions.forEach { (mode, label) ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                settingsViewModel.selectTheme(mode)
                                showThemeDialog = false
                            }
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = selectedTheme == mode,
                            onClick = {
                                settingsViewModel.selectTheme(mode)
                                showThemeDialog = false
                            }
                        )
                        Text(
                            label,
                            color = colors.textBody,
                            fontSize = fontSettings.scaled(14),
                            modifier = Modifier.padding(start = 8.dp)
                        )
                    }
                }
            }
        }
    }

    if (showFontDialog) {
        AppPopup(
            onDismissRequest = { showFontDialog = false },
            title = stringResource(R.string.font_settings),
            navigation = PopupNavigation.EMPHASIZED,
            size = PopupSize.XLARGE,
            actionArea = PopupActionArea.NONE
        ) {
            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                AppFontFamily.entries.forEach { family ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { settingsViewModel.selectFontFamily(family) }
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = selectedFontFamily == family,
                            onClick = { settingsViewModel.selectFontFamily(family) }
                        )
                        Text(
                            text = if (family == AppFontFamily.SYSTEM) stringResource(R.string.font_system) else family.displayName,
                            color = colors.textBody,
                            fontFamily = family.fontFamily,
                            modifier = Modifier.padding(start = 8.dp)
                        )
                    }
                }

                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 12.dp),
                    color = colors.cardBorder
                )

                Text(
                    text = stringResource(R.string.font_size),
                    fontWeight = FontWeight.SemiBold,
                    color = colors.textTitle,
                    fontSize = fontSettings.scaled(14)
                )
                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(stringResource(R.string.font_size_small), fontSize = fontSettings.scaled(12), color = colors.textSecondary)
                    Text(stringResource(R.string.font_size_normal), fontSize = fontSettings.scaled(12), color = colors.textSecondary)
                    Text(stringResource(R.string.font_size_large), fontSize = fontSettings.scaled(12), color = colors.textSecondary)
                }
                // 커스텀 슬라이더 (원티드 디자인 스타일)
                val trackHeight = 6.dp
                val thumbSize = 14.dp
                val stepCount = FontSizeLevel.entries.size
                val currentStep = selectedFontSize.ordinal
                val density = LocalDensity.current
                val thumbSizePx = with(density) { thumbSize.toPx() }

                BoxWithConstraints(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(thumbSize)
                        .pointerInput(stepCount) {
                            detectTapGestures { offset ->
                                val stepWidth = size.width.toFloat() / stepCount
                                val tappedStep = (offset.x / stepWidth).toInt().coerceIn(0, stepCount - 1)
                                settingsViewModel.selectFontSize(FontSizeLevel.entries[tappedStep])
                            }
                        }
                        .pointerInput(stepCount) {
                            detectHorizontalDragGestures { change, _ ->
                                change.consume()
                                val stepWidth = size.width.toFloat() / stepCount
                                val draggedStep = (change.position.x / stepWidth).toInt().coerceIn(0, stepCount - 1)
                                settingsViewModel.selectFontSize(FontSizeLevel.entries[draggedStep])
                            }
                        },
                    contentAlignment = Alignment.CenterStart
                ) {
                    val totalWidthPx = constraints.maxWidth.toFloat()
                    val thumbCenterPx = when (currentStep) {
                        0 -> thumbSizePx / 2f
                        stepCount - 1 -> totalWidthPx - thumbSizePx / 2f
                        else -> totalWidthPx / 2f
                    }
                    val thumbLeftPx = thumbCenterPx - thumbSizePx / 2f
                    val thumbLeftDp = with(density) { thumbLeftPx.toDp() }
                    val activeFraction = (thumbCenterPx / totalWidthPx).coerceIn(0f, 1f)

                    // 트랙 배경 (전체)
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(trackHeight)
                            .clip(RoundedCornerShape(trackHeight / 2))
                            .background(colors.chipText.copy(alpha = 0.15f))
                    )
                    // 트랙 활성 영역
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(activeFraction)
                            .height(trackHeight)
                            .clip(RoundedCornerShape(trackHeight / 2))
                            .background(colors.chipText)
                    )
                    // 구슬 (thumb)
                    Box(
                        modifier = Modifier
                            .offset(x = thumbLeftDp)
                            .size(thumbSize)
                            .clip(CircleShape)
                            .background(colors.chipText)
                    )
                }

                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 12.dp),
                    color = colors.cardBorder
                )

                Text(
                    text = stringResource(R.string.font_preview_title),
                    fontFamily = selectedFontFamily.fontFamily,
                    fontSize = selectedFontSize.titleSp.sp,
                    fontWeight = FontWeight.Bold,
                    color = colors.textTitle
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = stringResource(R.string.font_preview_body),
                    fontFamily = selectedFontFamily.fontFamily,
                    fontSize = selectedFontSize.bodySp.sp,
                    color = colors.textBody
                )
            }
        }
    }

    if (showLanguageDialog) {
        AppPopup(
            onDismissRequest = { showLanguageDialog = false },
            title = stringResource(R.string.language_settings),
            navigation = PopupNavigation.EMPHASIZED,
            size = PopupSize.MEDIUM,
            actionArea = PopupActionArea.NONE
        ) {
            Column {
                LANGUAGES.forEach { language ->
                    val onSelectLanguage = {
                        settingsViewModel.selectLanguage(language)
                        showLanguageDialog = false
                        android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
                            @Suppress("DEPRECATION")
                            activity?.overridePendingTransition(0, 0)
                            activity?.recreate()
                        }, 200)
                    }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSelectLanguage() }
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = language.code == selectedLanguage.code,
                            onClick = { onSelectLanguage() }
                        )
                        Text(
                            language.name,
                            color = colors.textBody,
                            modifier = Modifier.padding(start = 8.dp)
                        )
                    }
                }
            }
        }
    }

    if (showRestoreDialog) {
        AppPopup(
            onDismissRequest = { showRestoreDialog = false },
            title = stringResource(R.string.backup_restore),
            navigation = PopupNavigation.EMPHASIZED,
            size = PopupSize.MEDIUM,
            actionArea = PopupActionArea.NEUTRAL,
            primaryButtonText = stringResource(R.string.backup_restore_action),
            isPrimaryDestructive = true,
            onPrimaryClick = {
                showRestoreDialog = false
                importLauncher.launch(arrayOf("application/json"))
            },
            secondaryButtonText = stringResource(R.string.cancel),
            onSecondaryClick = { showRestoreDialog = false }
        ) {
            Text(stringResource(R.string.backup_restore_confirm), color = colors.textBody)
        }
    }

    // 클라우드 복원 확인
    if (showCloudRestoreConfirm) {
        AppPopup(
            onDismissRequest = { showCloudRestoreConfirm = false },
            title = stringResource(R.string.cloud_backup_restore),
            navigation = PopupNavigation.EMPHASIZED,
            size = PopupSize.MEDIUM,
            actionArea = PopupActionArea.NEUTRAL,
            primaryButtonText = stringResource(R.string.backup_restore_action),
            isPrimaryDestructive = true,
            onPrimaryClick = {
                showCloudRestoreConfirm = false
                settingsViewModel.restoreFromCloud()
            },
            secondaryButtonText = stringResource(R.string.cancel),
            onSecondaryClick = { showCloudRestoreConfirm = false }
        ) {
            Text(stringResource(R.string.cloud_backup_restore_confirm), color = colors.textBody)
        }
    }

    if (showSignOutDialog) {
        AppPopup(
            onDismissRequest = { showSignOutDialog = false },
            title = stringResource(R.string.sign_out),
            navigation = PopupNavigation.EMPHASIZED,
            size = PopupSize.MEDIUM,
            actionArea = PopupActionArea.NEUTRAL,
            primaryButtonText = stringResource(R.string.sign_out),
            isPrimaryDestructive = true,
            onPrimaryClick = {
                showSignOutDialog = false
                settingsViewModel.signOut()
            },
            secondaryButtonText = stringResource(R.string.cancel),
            onSecondaryClick = { showSignOutDialog = false }
        ) {
            Text(stringResource(R.string.sign_out_confirm), color = colors.textBody)
        }
    }

    if (showClearDialog) {
        AppPopup(
            onDismissRequest = { showClearDialog = false },
            title = stringResource(R.string.reset_memos),
            navigation = PopupNavigation.EMPHASIZED,
            size = PopupSize.MEDIUM,
            actionArea = PopupActionArea.NEUTRAL,
            primaryButtonText = stringResource(R.string.reset),
            isPrimaryDestructive = true,
            onPrimaryClick = {
                settingsViewModel.clearAllMemos()
                showClearDialog = false
            },
            secondaryButtonText = stringResource(R.string.cancel),
            onSecondaryClick = { showClearDialog = false }
        ) {
            Text(stringResource(R.string.reset_confirm), color = colors.textBody)
        }
    }

    if (showLicenseDialog) {
        val apacheLicense =
            "Licensed under the Apache License, Version 2.0 (the \"License\"); you may not use this file except in compliance with the License. You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0"

        @Composable
        fun LicenseItem(text: String) {
            Text(text = text, color = colors.textBody, fontSize = fontSettings.scaled(11), lineHeight = 17.sp)
        }

        AppPopup(
            onDismissRequest = { showLicenseDialog = false },
            title = stringResource(R.string.open_source_license),
            navigation = PopupNavigation.EMPHASIZED,
            size = PopupSize.LARGE,
            actionArea = PopupActionArea.NONE
        ) {
            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                LicenseItem("[1] Wanted Design System (Montage)\n출처: https://montage.wanted.co.kr\n라이선스: MIT License\nCopyright (c) Wanted Lab Corp.\n\nPermission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the \"Software\"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions: The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.")

                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 12.dp),
                    color = colors.cardBorder
                )

                LicenseItem("[2] Jetpack Compose & AndroidX\n출처: https://developer.android.com/jetpack\n라이선스: Apache License 2.0\nCopyright (c) The Android Open Source Project\n\n$apacheLicense")

                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 12.dp),
                    color = colors.cardBorder
                )

                LicenseItem("[3] Kotlin & kotlinx-coroutines\n출처: https://kotlinlang.org\n라이선스: Apache License 2.0\nCopyright (c) JetBrains s.r.o.\n\n$apacheLicense")

                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 12.dp),
                    color = colors.cardBorder
                )

                LicenseItem("[4] Haze (dev.chrisbanes.haze) 1.7.2\n출처: https://github.com/chrisbanes/haze\n라이선스: Apache License 2.0\nCopyright (c) Chris Banes\n\n$apacheLicense")

                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 12.dp),
                    color = colors.cardBorder
                )

                LicenseItem("[5] Room (androidx.room)\n출처: https://developer.android.com/jetpack/androidx/releases/room\n라이선스: Apache License 2.0\nCopyright (c) The Android Open Source Project\n\n$apacheLicense")

                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 12.dp),
                    color = colors.cardBorder
                )

                LicenseItem("[6] Firebase (Crashlytics & Analytics)\n출처: https://firebase.google.com\n라이선스: Apache License 2.0\nCopyright (c) Google LLC\n\n$apacheLicense")

                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 12.dp),
                    color = colors.cardBorder
                )

                LicenseItem("[7] Shadcn Compose\n출처: https://shadcn-compose.site\n라이선스: MIT License\nCopyright (c) Shadcn Compose Contributors\n\nPermission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the \"Software\"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions: The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.")
            }
        }
    }

    // containerColor 명시 → MaterialTheme.colorScheme.surface 무시
    Scaffold(
        containerColor = colors.screenBackground
    ) { innerPadding ->
        CompositionLocalProvider(
            LocalRippleConfiguration provides RippleConfiguration(color = colors.textTitle)
        ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 24.dp)
            ) {
                Text(
                    text = stringResource(R.string.settings),
                    fontSize = fontSettings.scaled(22),
                    fontWeight = FontWeight.Bold,
                    color = colors.topbarTitle,
                    modifier = Modifier.padding(start = 16.dp, bottom = 12.dp)
                )

                HorizontalDivider(
                    thickness = 0.3.dp ,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )

                Text(
                    text = stringResource(R.string.section_account),
                    fontSize = fontSettings.scaled(12),
                    fontWeight = FontWeight.Bold,
                    color = colors.topbarTitle,
                    modifier = Modifier.padding(start = 16.dp, bottom = 6.dp, top = 10.dp)
                )

                when (val state = authState) {
                    is AuthState.Loading -> {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                strokeWidth = 2.dp,
                                color = colors.textSecondary
                            )
                            Text(
                                text = stringResource(R.string.sign_in_loading),
                                color = colors.textSecondary,
                                fontSize = fontSettings.scaled(13),
                                modifier = Modifier.padding(start = 8.dp)
                            )
                        }
                    }
                    is AuthState.Unauthenticated -> {
                        OutlinedButton(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp),
                            border = BorderStroke(1.dp, colors.cardBorder),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = colors.textTitle),
                            onClick = {
                                scope.launch {
                                    try {
                                        val credentialManager = CredentialManager.create(context)
                                        val googleIdOption = GetGoogleIdOption.Builder()
                                            .setServerClientId(BuildConfig.GOOGLE_WEB_CLIENT_ID)
                                            .setFilterByAuthorizedAccounts(false)
                                            .build()
                                        val request = GetCredentialRequest.Builder()
                                            .addCredentialOption(googleIdOption)
                                            .build()
                                        val result = credentialManager.getCredential(
                                            context = activity ?: context,
                                            request = request,
                                        )
                                        val googleIdToken = GoogleIdTokenCredential
                                            .createFrom(result.credential.data)
                                            .idToken
                                        settingsViewModel.signInWithGoogle(googleIdToken)
                                    } catch (e: androidx.credentials.exceptions.GetCredentialCancellationException) {
                                        // 사용자가 취소 — 무시
                                    } catch (e: Exception) {
                                        android.util.Log.e("SettingsAuth", "Sign-in failed", e)
                                        Toast.makeText(
                                            context,
                                            context.getString(R.string.sign_in_error),
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                }
                            }
                        ) {
                            Icon(painter = painterResource(R.drawable.ic_google), contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(stringResource(R.string.sign_in_google), fontSize = fontSettings.scaled(14))
                        }
                    }
                    is AuthState.Authenticated -> {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = state.user.email ?: "",
                                fontSize = fontSettings.scaled(13),
                                color = colors.textBody,
                            )
                            Text(
                                text = stringResource(R.string.sign_out),
                                fontSize = fontSettings.scaled(12),
                                color = colors.textSecondary,
                                modifier = Modifier
                                    .clickable { showSignOutDialog = true }
                                    .padding(8.dp)
                            )
                        }
                        // 마지막 백업 시간 표시 (이메일 아래)
                        lastBackupTime?.let { time ->
                            Text(
                                text = stringResource(R.string.cloud_backup_last_time, time.take(16).replace("T", " ")),
                                fontSize = fontSettings.scaled(11),
                                color = colors.textSecondary,
                                modifier = Modifier.padding(start = 20.dp, bottom = 4.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                HorizontalDivider(
                    thickness = 0.3.dp,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )

                Text(
                    text = stringResource(R.string.section_theme),
                    fontSize = fontSettings.scaled(12),
                    fontWeight = FontWeight.Bold,
                    color = colors.topbarTitle,
                    modifier = Modifier.padding(start = 16.dp, bottom = 6.dp, top = 10.dp)
                )

                OutlinedButton(
                    onClick = { showLanguageDialog = true },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    border = BorderStroke(1.dp, colors.cardBorder),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = colors.textTitle),
                ) {
                    Text(stringResource(R.string.language_settings), fontSize = fontSettings.scaled(14))
                }

                Spacer(modifier = Modifier.height(6.dp))

                OutlinedButton(
                    onClick = { showThemeDialog = true },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    border = BorderStroke(1.dp, colors.cardBorder),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = colors.textTitle),
                ) {
                    Text(stringResource(R.string.theme_settings), fontSize = fontSettings.scaled(14))
                }

                Spacer(modifier = Modifier.height(6.dp))

                OutlinedButton(
                    onClick = { showFontDialog = true },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    border = BorderStroke(1.dp, colors.cardBorder),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = colors.textTitle),
                ) {
                    Text(stringResource(R.string.font_settings), fontSize = fontSettings.scaled(14))
                }

                Spacer(modifier = Modifier.height(6.dp))

                HorizontalDivider(
                    thickness = 0.3.dp ,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )

                Text(
                    text = stringResource(R.string.section_data),
                    fontSize = fontSettings.scaled(12),
                    fontWeight = FontWeight.Bold,
                    color = colors.topbarTitle,
                    modifier = Modifier.padding(start = 16.dp, bottom = 6.dp, top = 10.dp)
                )

                if (authState is AuthState.Authenticated) {
                    // 로그인: 클라우드 백업
                    OutlinedButton(
                        onClick = { settingsViewModel.uploadCloudBackup() },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        border = BorderStroke(1.dp, colors.cardBorder),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = colors.textTitle),
                    ) {
                        Text(if (cloudBackupState is CloudBackupState.Uploading)
                            stringResource(R.string.cloud_backup_uploading)
                        else stringResource(R.string.cloud_backup_now), fontSize = fontSettings.scaled(14))
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    OutlinedButton(
                        onClick = { showCloudRestoreConfirm = true },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        border = BorderStroke(1.dp, colors.cardBorder),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = colors.textTitle),
                    ) {
                        Text(if (cloudBackupState is CloudBackupState.Restoring)
                            stringResource(R.string.cloud_backup_restoring)
                        else stringResource(R.string.cloud_backup_restore), fontSize = fontSettings.scaled(14))
                    }
                } else {
                    // 비로그인: 로컬 백업
                    OutlinedButton(
                        onClick = {
                            val fileName = "memozy_backup_${
                                java.text.SimpleDateFormat(
                                    "yyyyMMdd_HHmm",
                                    java.util.Locale.getDefault()
                                ).format(java.util.Date())
                            }.json"
                            exportLauncher.launch(fileName)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        border = BorderStroke(1.dp, colors.cardBorder),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = colors.textTitle),
                    ) {
                        Text(stringResource(R.string.backup_export), fontSize = fontSettings.scaled(14))
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    OutlinedButton(
                        onClick = { showRestoreDialog = true },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        border = BorderStroke(1.dp, colors.cardBorder),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = colors.textTitle),
                    ) {
                        Text(stringResource(R.string.backup_restore), fontSize = fontSettings.scaled(14))
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                OutlinedButton(
                    onClick = { onTrash() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    border = BorderStroke(1.dp, colors.cardBorder),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = colors.textTitle),
                ) {
                    Text(stringResource(R.string.trash_title), fontSize = fontSettings.scaled(14))
                }

                if (isDonationEnabled) {
                    Spacer(modifier = Modifier.height(6.dp))

                    OutlinedButton(
                        onClick = { onDonation() },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        border = BorderStroke(1.dp, colors.cardBorder),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = colors.textTitle),
                    ) {
                        Text(stringResource(R.string.donation_button), fontSize = fontSettings.scaled(14))
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                OutlinedButton(
                    onClick = { showClearDialog = true },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    border = BorderStroke(1.dp, colors.cardBorder),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFE24B4A)),
                ) {
                    Text(stringResource(R.string.reset_memos), fontSize = fontSettings.scaled(14))
                }

                HorizontalDivider(
                    thickness = 0.3.dp ,
                    modifier = Modifier.padding(start = 16.dp , top = 12.dp)
                )

                Text(
                    text = stringResource(R.string.section_other),
                    fontSize = fontSettings.scaled(12),
                    fontWeight = FontWeight.Bold,
                    color = colors.topbarTitle,
                    modifier = Modifier.padding(start = 16.dp, bottom = 6.dp, top = 10.dp)
                )

                OutlinedButton(
                    onClick = { showLicenseDialog = true },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    border = BorderStroke(1.dp, colors.cardBorder),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = colors.textTitle),
                ) {
                    Text(stringResource(R.string.open_source_license), fontSize = fontSettings.scaled(14))
                }

                Text(
                    text = "v$versionName",
                    fontSize = fontSettings.scaled(12),
                    color = colors.textSecondary,
                    modifier = Modifier
                        .padding(top = 12.dp)
                        .align(alignment = Alignment.CenterHorizontally)
                )

                // 하단 여유 공간 (네비게이션 바에 가리지 않도록)
                Spacer(modifier = Modifier.height(80.dp))
            }
        }
        }
    }
}

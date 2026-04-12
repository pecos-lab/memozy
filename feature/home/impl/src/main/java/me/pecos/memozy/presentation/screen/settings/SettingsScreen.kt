package me.pecos.memozy.presentation.screen.settings

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.graphics.Color
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
import com.wanted.android.wanted.design.actions.button.WantedButton
import com.wanted.android.wanted.design.actions.button.config.WantedButtonDefaults
import com.wanted.android.wanted.design.util.ButtonType
import com.wanted.android.wanted.design.util.ButtonVariant
import kotlinx.coroutines.launch
import me.pecos.memozy.data.datasource.remote.auth.AuthState
import me.pecos.memozy.feature.core.resource.R
import me.pecos.memozy.feature.home.impl.BuildConfig
import me.pecos.memozy.presentation.components.AppPopup
import me.pecos.memozy.presentation.components.PopupActionArea
import me.pecos.memozy.presentation.components.PopupNavigation
import me.pecos.memozy.presentation.components.PopupSize
import me.pecos.memozy.presentation.theme.LocalActivity
import me.pecos.memozy.presentation.theme.LocalAppColors

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
    var showRestoreDialog by remember { mutableStateOf(false) }
    var showSignOutDialog by remember { mutableStateOf(false) }

    val selectedLanguage by settingsViewModel.selectedLanguage.collectAsState()
    val selectedTheme by settingsViewModel.selectedTheme.collectAsState()
    val backupResult by settingsViewModel.backupResult.collectAsState()
    val isDonationEnabled by settingsViewModel.isDonationEnabled.collectAsState()
    val authState by settingsViewModel.authState.collectAsState()
    val colors = LocalAppColors.current
    val context = LocalContext.current
    val activity = LocalActivity.current
    val scope = rememberCoroutineScope()

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
                            modifier = Modifier.padding(start = 8.dp)
                        )
                    }
                }
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
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                settingsViewModel.selectLanguage(language)
                                showLanguageDialog = false
                                @Suppress("DEPRECATION")
                                activity?.overridePendingTransition(0, 0)
                                activity?.recreate()
                            }
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = language.code == selectedLanguage.code,
                            onClick = {
                                settingsViewModel.selectLanguage(language)
                                showLanguageDialog = false
                                @Suppress("DEPRECATION")
                                activity?.overridePendingTransition(0, 0)
                                activity?.recreate()
                            }
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
            Text(text = text, color = colors.textBody, fontSize = 11.sp, lineHeight = 17.sp)
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
            }
        }
    }

    // containerColor 명시 → MaterialTheme.colorScheme.surface 무시
    Scaffold(
        containerColor = colors.screenBackground
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 24.dp)
            ) {
                Text(
                    text = stringResource(R.string.settings),
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = colors.topbarTitle,
                    modifier = Modifier.padding(start = 16.dp, bottom = 12.dp)
                )

                HorizontalDivider(
                    thickness = 0.3.dp ,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )

                Text(
                    text = stringResource(R.string.section_theme),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = colors.topbarTitle,
                    modifier = Modifier.padding(start = 16.dp, bottom = 8.dp, top = 12.dp)
                )

                WantedButton(
                    text = stringResource(R.string.language_settings),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    buttonDefault = WantedButtonDefaults.getDefault(
                        type = ButtonType.ASSISTIVE,
                        variant = ButtonVariant.OUTLINED
                    ).copy(contentColor = colors.textTitle),
                    onClick = { showLanguageDialog = true }
                )

                Spacer(modifier = Modifier.height(12.dp))

                WantedButton(
                    text = stringResource(R.string.theme_settings),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    buttonDefault = WantedButtonDefaults.getDefault(
                        type = ButtonType.ASSISTIVE,
                        variant = ButtonVariant.OUTLINED
                    ).copy(contentColor = colors.textTitle),
                    onClick = { showThemeDialog = true }
                )

                Spacer(modifier = Modifier.height(12.dp))

                HorizontalDivider(
                    thickness = 0.3.dp,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )

                Text(
                    text = stringResource(R.string.section_account),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = colors.topbarTitle,
                    modifier = Modifier.padding(start = 16.dp, bottom = 8.dp, top = 12.dp)
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
                                fontSize = 13.sp,
                                modifier = Modifier.padding(start = 8.dp)
                            )
                        }
                    }
                    is AuthState.Unauthenticated -> {
                        WantedButton(
                            text = stringResource(R.string.sign_in_google),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp),
                            buttonDefault = WantedButtonDefaults.getDefault(
                                type = ButtonType.ASSISTIVE,
                                variant = ButtonVariant.OUTLINED
                            ).copy(contentColor = colors.textTitle),
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
                                    } catch (e: Exception) {
                                        Toast.makeText(
                                            context,
                                            context.getString(R.string.sign_in_error),
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                }
                            }
                        )
                    }
                    is AuthState.Authenticated -> {
                        Text(
                            text = state.user.email ?: "",
                            fontSize = 13.sp,
                            color = colors.textBody,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        WantedButton(
                            text = stringResource(R.string.sign_out),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp),
                            buttonDefault = WantedButtonDefaults.getDefault(
                                type = ButtonType.ASSISTIVE,
                                variant = ButtonVariant.OUTLINED
                            ).copy(contentColor = colors.textTitle),
                            onClick = { showSignOutDialog = true }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                HorizontalDivider(
                    thickness = 0.3.dp ,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )

                Text(
                    text = stringResource(R.string.section_data),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = colors.topbarTitle,
                    modifier = Modifier.padding(start = 16.dp, bottom = 8.dp, top = 12.dp)
                )

                Spacer(modifier = Modifier.height(12.dp))

                WantedButton(
                    text = stringResource(R.string.trash_title),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    buttonDefault = WantedButtonDefaults.getDefault(
                        type = ButtonType.ASSISTIVE,
                        variant = ButtonVariant.OUTLINED
                    ).copy(contentColor = colors.textTitle),
                    onClick = { onTrash() }
                )

                Spacer(modifier = Modifier.height(12.dp))

                WantedButton(
                    text = stringResource(R.string.backup_export),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    buttonDefault = WantedButtonDefaults.getDefault(
                        type = ButtonType.ASSISTIVE,
                        variant = ButtonVariant.OUTLINED
                    ).copy(contentColor = colors.textTitle),
                    onClick = {
                        val fileName = "memozy_backup_${
                            java.text.SimpleDateFormat(
                                "yyyyMMdd_HHmm",
                                java.util.Locale.getDefault()
                            ).format(java.util.Date())
                        }.json"
                        exportLauncher.launch(fileName)
                    }
                )

                Spacer(modifier = Modifier.height(12.dp))

                WantedButton(
                    text = stringResource(R.string.backup_restore),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    buttonDefault = WantedButtonDefaults.getDefault(
                        type = ButtonType.ASSISTIVE,
                        variant = ButtonVariant.OUTLINED
                    ).copy(contentColor = colors.textTitle),
                    onClick = { showRestoreDialog = true }
                )

                if (isDonationEnabled) {
                    Spacer(modifier = Modifier.height(12.dp))

                    WantedButton(
                        text = stringResource(R.string.donation_button),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        buttonDefault = WantedButtonDefaults.getDefault(
                            type = ButtonType.ASSISTIVE,
                            variant = ButtonVariant.OUTLINED
                        ).copy(contentColor = colors.textTitle),
                        onClick = { onDonation() }
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                WantedButton(
                    text = stringResource(R.string.reset_memos),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    buttonDefault = WantedButtonDefaults.getDefault(
                        type = ButtonType.ASSISTIVE,
                        variant = ButtonVariant.OUTLINED
                    ).copy(contentColor = Color(0xFFE24B4A)),
                    onClick = { showClearDialog = true }
                )

                HorizontalDivider(
                    thickness = 0.3.dp ,
                    modifier = Modifier.padding(start = 16.dp , top = 12.dp)
                )

                Text(
                    text = stringResource(R.string.section_other),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = colors.topbarTitle,
                    modifier = Modifier.padding(start = 16.dp, bottom = 8.dp, top = 12.dp)
                )

                WantedButton(
                    text = stringResource(R.string.open_source_license),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    buttonDefault = WantedButtonDefaults.getDefault(
                        type = ButtonType.ASSISTIVE,
                        variant = ButtonVariant.OUTLINED
                    ).copy(contentColor = colors.textTitle),
                    onClick = { showLicenseDialog = true }
                )

                Text(
                    text = "v$versionName",
                    fontSize = 12.sp,
                    color = colors.textSecondary,
                    modifier = Modifier
                        .padding(top = 12.dp)
                        .align(alignment = Alignment.CenterHorizontally)
                )
            }
        }
    }
}

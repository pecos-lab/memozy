package me.pecos.nota.presentation.screen.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.wanted.android.wanted.design.actions.button.WantedButton
import com.wanted.android.wanted.design.actions.button.config.WantedButtonDefaults
import com.wanted.android.wanted.design.util.ButtonType
import com.wanted.android.wanted.design.util.ButtonVariant
import me.pecos.nota.R
import me.pecos.nota.presentation.components.AppPopup
import me.pecos.nota.presentation.components.PopupActionArea
import me.pecos.nota.presentation.components.PopupNavigation
import me.pecos.nota.presentation.components.PopupSize
import me.pecos.nota.presentation.theme.LocalActivity
import me.pecos.nota.presentation.theme.LocalAppColors

@Composable
fun SettingsScreen(
    onBack: () -> Unit = {},
    onDonation: () -> Unit = {},
    settingsViewModel: SettingsViewModel = viewModel()
) {
    var showClearDialog by remember { mutableStateOf(false) }
    var showLicenseDialog by remember { mutableStateOf(false) }
    var showLanguageDialog by remember { mutableStateOf(false) }
    var showThemeDialog by remember { mutableStateOf(false) }

    val selectedLanguage by settingsViewModel.selectedLanguage.collectAsState()
    val selectedTheme by settingsViewModel.selectedTheme.collectAsState()
    val colors = LocalAppColors.current
    val context = LocalContext.current
    val activity = LocalActivity.current

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
        val apacheLicense = "Licensed under the Apache License, Version 2.0 (the \"License\"); you may not use this file except in compliance with the License. You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0"

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
                WantedButton(
                    text = stringResource(R.string.language_settings),
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                    buttonDefault = WantedButtonDefaults.getDefault(
                        type = ButtonType.ASSISTIVE,
                        variant = ButtonVariant.OUTLINED
                    ).copy(contentColor = colors.textTitle),
                    onClick = { showLanguageDialog = true }
                )

                Spacer(modifier = Modifier.height(12.dp))

                WantedButton(
                    text = stringResource(R.string.theme_settings),
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                    buttonDefault = WantedButtonDefaults.getDefault(
                        type = ButtonType.ASSISTIVE,
                        variant = ButtonVariant.OUTLINED
                    ).copy(contentColor = colors.textTitle),
                    onClick = { showThemeDialog = true }
                )

                Spacer(modifier = Modifier.height(12.dp))

                WantedButton(
                    text = stringResource(R.string.open_source_license),
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                    buttonDefault = WantedButtonDefaults.getDefault(
                        type = ButtonType.ASSISTIVE,
                        variant = ButtonVariant.OUTLINED
                    ).copy(contentColor = colors.textTitle),
                    onClick = { showLicenseDialog = true }
                )

                Spacer(modifier = Modifier.height(12.dp))

                WantedButton(
                    text = stringResource(R.string.donation_button),
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                    buttonDefault = WantedButtonDefaults.getDefault(
                        type = ButtonType.ASSISTIVE,
                        variant = ButtonVariant.OUTLINED
                    ).copy(contentColor = colors.textTitle),
                    onClick = { onDonation() }
                )

                Spacer(modifier = Modifier.height(12.dp))

                WantedButton(
                    text = stringResource(R.string.reset_memos),
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                    buttonDefault = WantedButtonDefaults.getDefault(
                        type = ButtonType.ASSISTIVE,
                        variant = ButtonVariant.OUTLINED
                    ).copy(contentColor = Color(0xFFE24B4A)),
                    onClick = { showClearDialog = true }
                )
            }

            Text(
                text = "v$versionName",
                fontSize = 12.sp,
                color = colors.textSecondary,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 12.dp)
            )
        }
    }
}

package me.pecos.nota

import android.os.Build
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.wanted.android.wanted.design.actions.button.WantedButton
import com.wanted.android.wanted.design.actions.button.config.WantedButtonDefaults
import com.wanted.android.wanted.design.util.ButtonType
import com.wanted.android.wanted.design.util.ButtonVariant

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onBack: () -> Unit = {},
    settingsViewModel: SettingsViewModel = viewModel()
) {
    var showLanguageDialog by remember { mutableStateOf(false) }
    var showThemeDialog by remember { mutableStateOf(false) }
    var showLicenseDialog by remember { mutableStateOf(false) }
    var showClearDialog by remember { mutableStateOf(false) }

    val selectedLanguage by settingsViewModel.selectedLanguage.collectAsState()
    val selectedTheme by settingsViewModel.selectedTheme.collectAsState()
    val shouldRecreate by settingsViewModel.shouldRecreate.collectAsState()
    val colors = LocalAppColors.current
    val activity = LocalActivity.current
    val context = LocalContext.current

    LaunchedEffect(shouldRecreate) {
        if (shouldRecreate) {
            settingsViewModel.onRecreated()
            activity?.recreate()
        }
    }

    val versionName = remember {
        context.packageManager.getPackageInfo(context.packageName, 0).versionName
    }

    // ── 언어설정 Dialog ────────────────────────────────────────────────────────
    if (showLanguageDialog) {
        AlertDialog(
            onDismissRequest = { showLanguageDialog = false },
            containerColor = colors.cardBackground,
            title = { Text(stringResource(R.string.language_settings), color = colors.textTitle) },
            text = {
                Column {
                    LANGUAGES.forEach { language ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { settingsViewModel.selectLanguage(language) }
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = language.code == selectedLanguage.code,
                                onClick = { settingsViewModel.selectLanguage(language) }
                            )
                            Text(
                                text = language.name,
                                color = colors.textBody,
                                modifier = Modifier.padding(start = 8.dp)
                            )
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showLanguageDialog = false }) {
                    Text(stringResource(R.string.close), color = colors.chipText)
                }
            }
        )
    }

    // ── 테마 설정 Dialog ───────────────────────────────────────────────────────
    if (showThemeDialog) {
        val isSystemEnabled = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
        val themeOptions = listOf(
            Triple(ThemeMode.LIGHT, stringResource(R.string.theme_light), true),
            Triple(ThemeMode.DARK, stringResource(R.string.theme_dark), true),
            Triple(ThemeMode.SYSTEM, stringResource(R.string.theme_system), isSystemEnabled),
        )
        AlertDialog(
            onDismissRequest = { showThemeDialog = false },
            containerColor = colors.cardBackground,
            title = { Text(stringResource(R.string.theme_settings), color = colors.textTitle) },
            text = {
                Column {
                    themeOptions.forEach { (mode, label, enabled) ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .then(
                                    if (enabled) Modifier.clickable {
                                        settingsViewModel.selectTheme(mode)
                                    } else Modifier
                                )
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = selectedTheme == mode,
                                onClick = if (enabled) ({ settingsViewModel.selectTheme(mode) }) else null,
                                enabled = enabled
                            )
                            Text(
                                text = label,
                                color = if (enabled) colors.textBody else colors.textSecondary,
                                modifier = Modifier.padding(start = 8.dp)
                            )
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showThemeDialog = false }) {
                    Text(stringResource(R.string.close), color = colors.chipText)
                }
            }
        )
    }

    // ── 오픈소스 라이센스 Dialog ───────────────────────────────────────────────
    if (showLicenseDialog) {
        AlertDialog(
            onDismissRequest = { showLicenseDialog = false },
            containerColor = colors.cardBackground,
            title = { Text(stringResource(R.string.open_source_license), color = colors.textTitle) },
            text = {
                Text(
                    text = """
[1] Wanted Design System (Montage)
출처: https://montage.wanted.co.kr
라이선스: MIT License
Copyright (c) Wanted Lab Corp.

Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
                    """.trimIndent(),
                    color = colors.textBody,
                    fontSize = 12.sp,
                    fontFamily = FontFamily.Monospace,
                    lineHeight = 18.sp,
                    modifier = Modifier.verticalScroll(rememberScrollState())
                )
            },
            confirmButton = {
                TextButton(onClick = { showLicenseDialog = false }) {
                    Text(stringResource(R.string.close), color = colors.chipText)
                }
            }
        )
    }

    // ── 메모 초기화 Dialog ─────────────────────────────────────────────────────
    if (showClearDialog) {
        AlertDialog(
            onDismissRequest = { showClearDialog = false },
            containerColor = colors.cardBackground,
            title = { Text(stringResource(R.string.reset_memos), color = colors.textTitle) },
            text = { Text(stringResource(R.string.reset_confirm), color = colors.textBody) },
            confirmButton = {
                TextButton(onClick = {
                    settingsViewModel.clearAllMemos()
                    showClearDialog = false
                }) {
                    Text(stringResource(R.string.reset), color = Color.Red)
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearDialog = false }) {
                    Text(stringResource(R.string.cancel), color = colors.chipText)
                }
            }
        )
    }

    // containerColor 명시 → MaterialTheme.colorScheme.surface 무시
    Scaffold(
        containerColor = colors.screenBackground,
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.settings), color = colors.topbarTitle) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = null,
                            tint = colors.topbarTitle
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = colors.screenBackground)
            )
        }
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
                WantedButton(
                    text = stringResource(R.string.language_settings),
                    modifier = Modifier.fillMaxWidth(),
                    buttonDefault = WantedButtonDefaults.getDefault(
                        type = ButtonType.ASSISTIVE,
                        variant = ButtonVariant.OUTLINED
                    ).copy(contentColor = colors.textTitle),
                    onClick = { showLanguageDialog = true }
                )

                Spacer(modifier = Modifier.height(12.dp))

                WantedButton(
                    text = stringResource(R.string.theme_settings),
                    modifier = Modifier.fillMaxWidth(),
                    buttonDefault = WantedButtonDefaults.getDefault(
                        type = ButtonType.ASSISTIVE,
                        variant = ButtonVariant.OUTLINED
                    ).copy(contentColor = colors.textTitle),
                    onClick = { showThemeDialog = true }
                )

                Spacer(modifier = Modifier.height(12.dp))

                WantedButton(
                    text = stringResource(R.string.open_source_license),
                    modifier = Modifier.fillMaxWidth(),
                    buttonDefault = WantedButtonDefaults.getDefault(
                        type = ButtonType.ASSISTIVE,
                        variant = ButtonVariant.OUTLINED
                    ).copy(contentColor = colors.textTitle),
                    onClick = { showLicenseDialog = true }
                )

                Spacer(modifier = Modifier.height(12.dp))

                WantedButton(
                    text = stringResource(R.string.reset_memos),
                    modifier = Modifier.fillMaxWidth(),
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

package me.pecos.nota

import android.app.Activity
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.wanted.android.wanted.design.actions.button.WantedButton
import com.wanted.android.wanted.design.actions.button.config.WantedButtonDefaults
import com.wanted.android.wanted.design.util.ButtonType
import com.wanted.android.wanted.design.util.ButtonVariant

@Composable
fun SettingsScreen(
    settingsViewModel: SettingsViewModel = viewModel()
) {
    var showClearDialog by remember { mutableStateOf(false) }
    var showLicenseDialog by remember { mutableStateOf(false) }
    var showLanguageDialog by remember { mutableStateOf(false) }
    var showThemeDialog by remember { mutableStateOf(false) }

    val selectedLanguage by settingsViewModel.selectedLanguage.collectAsState()
    val shouldRecreate by settingsViewModel.shouldRecreate.collectAsState()
    val selectedTheme by settingsViewModel.selectedTheme.collectAsState()
    val activity = LocalContext.current as? Activity

    LaunchedEffect(shouldRecreate) {
        if (shouldRecreate) {
            settingsViewModel.onRecreated()
            activity?.recreate()
        }
    }

    val context = LocalContext.current
    val versionName = remember {
        context.packageManager.getPackageInfo(context.packageName, 0).versionName
    }

    if (showThemeDialog) {
        val themeOptions = listOf(
            ThemeMode.LIGHT to stringResource(R.string.theme_light),
            ThemeMode.DARK to stringResource(R.string.theme_dark),
            ThemeMode.SYSTEM to stringResource(R.string.theme_system),
        )
        AlertDialog(
            onDismissRequest = { showThemeDialog = false },
            title = { Text(stringResource(R.string.theme_settings)) },
            text = {
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
                            Text(label, modifier = Modifier.padding(start = 8.dp))
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showThemeDialog = false }) {
                    Text(stringResource(R.string.close))
                }
            }
        )
    }

    if (showLanguageDialog) {
        AlertDialog(
            onDismissRequest = { showLanguageDialog = false },
            title = { Text(stringResource(R.string.language_settings)) },
            text = {
                Column {
                    LANGUAGES.forEach { language ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    settingsViewModel.selectLanguage(language)
                                    showLanguageDialog = false
                                }
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = language.code == selectedLanguage.code,
                                onClick = {
                                    settingsViewModel.selectLanguage(language)
                                    showLanguageDialog = false
                                }
                            )
                            Text(language.name, modifier = Modifier.padding(start = 8.dp))
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showLanguageDialog = false }) {
                    Text(stringResource(R.string.close))
                }
            }
        )
    }

    if (showClearDialog) {
        AlertDialog(
            onDismissRequest = { showClearDialog = false },
            title = { Text(stringResource(R.string.reset_memos)) },
            text = { Text(stringResource(R.string.reset_confirm)) },
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
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }

    if (showLicenseDialog) {
        AlertDialog(
            onDismissRequest = { showLicenseDialog = false },
            title = { Text(stringResource(R.string.open_source_license)) },
            text = { Text(stringResource(R.string.license_content)) },
            confirmButton = {
                TextButton(onClick = { showLicenseDialog = false }) {
                    Text(stringResource(R.string.close))
                }
            }
        )
    }

    Scaffold { innerPadding ->
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
                    modifier = Modifier.padding(bottom = 24.dp)
                )

                WantedButton(
                    text = stringResource(R.string.language_settings),
                    modifier = Modifier.fillMaxWidth(),
                    type = ButtonType.ASSISTIVE,
                    variant = ButtonVariant.OUTLINED,
                    onClick = { showLanguageDialog = true }
                )

                Spacer(modifier = Modifier.height(12.dp))

                WantedButton(
                    text = stringResource(R.string.theme_settings),
                    modifier = Modifier.fillMaxWidth(),
                    type = ButtonType.ASSISTIVE,
                    variant = ButtonVariant.OUTLINED,
                    onClick = { showThemeDialog = true }
                )

                Spacer(modifier = Modifier.height(12.dp))

                WantedButton(
                    text = stringResource(R.string.open_source_license),
                    modifier = Modifier.fillMaxWidth(),
                    type = ButtonType.ASSISTIVE,
                    variant = ButtonVariant.OUTLINED,
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
                color = Color.Gray,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 12.dp)
            )
        }
    }
}

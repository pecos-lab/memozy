package me.pecos.memozy.presentation.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider

@Composable
actual fun AppThemeShell(isDarkTheme: Boolean, content: @Composable () -> Unit) {
    val appColors = if (isDarkTheme) darkAppColors else lightAppColors
    MaterialTheme(
        colorScheme = if (isDarkTheme) darkColorScheme() else lightColorScheme(),
    ) {
        CompositionLocalProvider(LocalAppColors provides appColors) {
            content()
        }
    }
}

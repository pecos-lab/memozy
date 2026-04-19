package me.pecos.memozy.presentation.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import com.wanted.android.wanted.design.theme.DesignSystemTheme

@Composable
actual fun AppThemeShell(isDarkTheme: Boolean, content: @Composable () -> Unit) {
    val appColors = if (isDarkTheme) darkAppColors else lightAppColors
    DesignSystemTheme(isDarkTheme = isDarkTheme) {
        CompositionLocalProvider(LocalAppColors provides appColors) {
            content()
        }
    }
}

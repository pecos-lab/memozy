package me.pecos.memozy.presentation.theme

import androidx.compose.runtime.Composable
import com.wanted.android.wanted.design.theme.DesignSystemTheme

@Composable
actual fun AppThemeShell(isDarkTheme: Boolean, content: @Composable () -> Unit) {
    DesignSystemTheme(isDarkTheme = isDarkTheme) {
        content()
    }
}

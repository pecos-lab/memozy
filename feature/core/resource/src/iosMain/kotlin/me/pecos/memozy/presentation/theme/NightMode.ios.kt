package me.pecos.memozy.presentation.theme

import androidx.compose.runtime.Composable

@Composable
actual fun OverrideNightMode(isDarkTheme: Boolean, content: @Composable () -> Unit) {
    content()
}

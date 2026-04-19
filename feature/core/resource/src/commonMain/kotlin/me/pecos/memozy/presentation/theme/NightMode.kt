package me.pecos.memozy.presentation.theme

import androidx.compose.runtime.Composable

@Composable
expect fun OverrideNightMode(isDarkTheme: Boolean, content: @Composable () -> Unit)

package me.pecos.memozy.presentation.theme

import androidx.compose.runtime.Composable

/**
 * 플랫폼 별 최상위 테마 래퍼. Android 는 montage-android 의 DesignSystemTheme
 * 을 사용하고, iOS 는 MaterialTheme 만 적용한다.
 */
@Composable
expect fun AppThemeShell(isDarkTheme: Boolean, content: @Composable () -> Unit)

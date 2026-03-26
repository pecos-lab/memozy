package me.pecos.nota

import android.app.Activity
import android.content.Context
import android.content.res.Configuration
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext

// ── 색상 데이터 클래스 ──────────────────────────────────────────────────────────

data class AppColors(
    val screenBackground: Color,
    val topbarTitle: Color,
    val navBackground: Color,
    val navBorder: Color,
    val navIconSelected: Color,
    val navIconUnselected: Color,
    val cardBackground: Color,
    val cardBorder: Color,
    val textTitle: Color,
    val textBody: Color,
    val textSecondary: Color,
    val chipBackground: Color,
    val chipText: Color,
)


val lightAppColors = AppColors(
    screenBackground = Color(0xFFFFFFFF),
    topbarTitle      = Color(0xFF1C1C1E),
    navBackground    = Color(0xFFFFFFFF),
    navBorder        = Color(0xFFE0E0E0),
    navIconSelected  = Color(0xFF000000),
    navIconUnselected= Color(0x66000000),
    cardBackground   = Color(0xFFFFFFFF),
    cardBorder       = Color(0xFFE0E0E0),
    textTitle        = Color(0xFF000000),
    textBody         = Color(0xFF616161),
    textSecondary    = Color(0xFF9E9E9E),
    chipBackground   = Color(0xFFE8F0FE),
    chipText         = Color(0xFF1D6BF3),
)

val darkAppColors = AppColors(
    screenBackground = Color(0xFF1C1C1E),
    topbarTitle      = Color(0xFFF2F2F7),
    navBackground    = Color(0xFF1C1C1E),
    navBorder        = Color(0xFF3A3A3C),
    navIconSelected  = Color(0xFFF2F2F7),
    navIconUnselected= Color(0xFF8E8E93),
    cardBackground   = Color(0xFF2C2C2E),
    cardBorder       = Color(0xFF3A3A3C),
    textTitle        = Color(0xFFF2F2F7),
    textBody         = Color(0xFFEBEBF5),
    textSecondary    = Color(0xFF8E8E93),
    chipBackground   = Color(0xFF3A3A3C),
    chipText         = Color(0xFF6B9FFF),
)

val LocalAppColors = staticCompositionLocalOf { lightAppColors }
val LocalActivity = staticCompositionLocalOf<Activity?> { null }

/**
 * LocalConfiguration + LocalContext 를 모두 오버라이드.
 * - LocalConfiguration: isSystemInDarkTheme() 이 우리 state 를 반환하게 함
 * - LocalContext: colorResource() 가 라이브러리 내부에서 호출될 때
 *   올바른 values / values-night 를 읽도록 context.resources 를 교체
 * activity?.recreate() 가 필요한 곳은 findActivity() 로 Activity 를 꺼내 사용.
 */
@Composable
fun OverrideNightMode(isDarkTheme: Boolean, content: @Composable () -> Unit) {
    val baseConfig = LocalConfiguration.current
    val baseContext = LocalContext.current

    val nightFlag = if (isDarkTheme) Configuration.UI_MODE_NIGHT_YES else Configuration.UI_MODE_NIGHT_NO

    val overriddenConfig = remember(isDarkTheme, baseConfig) {
        Configuration(baseConfig).apply {
            uiMode = (uiMode and Configuration.UI_MODE_NIGHT_MASK.inv()) or nightFlag
        }
    }
    val overriddenContext = remember(isDarkTheme, baseContext) {
        baseContext.createConfigurationContext(overriddenConfig)
    }

    CompositionLocalProvider(
        LocalConfiguration provides overriddenConfig,
        LocalContext provides overriddenContext,
    ) {
        content()
    }
}

/** Context 체인을 따라 Activity 를 찾음 (LocalContext 가 ContextWrapper 로 감싸진 경우 대비) */
fun Context.findActivity(): Activity? {
    var ctx = this
    while (ctx is android.content.ContextWrapper) {
        if (ctx is Activity) return ctx
        ctx = ctx.baseContext
    }
    return null
}

package me.pecos.memozy.presentation.theme

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.res.Configuration
import android.view.ContextThemeWrapper
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext

val LocalActivity = staticCompositionLocalOf<Activity?> { null }

/**
 * LocalConfiguration + LocalContext 를 모두 오버라이드.
 * - LocalConfiguration: isSystemInDarkTheme() 이 우리 state 를 반환하게 함
 * - LocalContext: colorResource() 가 라이브러리 내부에서 호출될 때
 *   올바른 values / values-night 를 읽도록 context.resources 를 교체
 * activity?.recreate() 가 필요한 곳은 findActivity() 로 Activity 를 찾아 사용.
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
        ContextThemeWrapper(baseContext, baseContext.theme).apply {
            applyOverrideConfiguration(overriddenConfig)
        }
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
    while (ctx is ContextWrapper) {
        if (ctx is Activity) return ctx
        ctx = ctx.baseContext
    }
    return null
}

package me.pecos.memozy.presentation.theme

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.res.Configuration
import android.view.ContextThemeWrapper
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext

@Composable
actual fun OverrideNightMode(isDarkTheme: Boolean, content: @Composable () -> Unit) {
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

fun Context.findActivity(): Activity? {
    var ctx = this
    while (ctx is ContextWrapper) {
        if (ctx is Activity) return ctx
        ctx = ctx.baseContext
    }
    return null
}

package me.pecos.memozy.widget

import androidx.compose.ui.graphics.Color
import androidx.glance.color.ColorProvider

object MemoWidgetColors {
    val background = ColorProvider(
        day = Color(0xFFFFFFFF),
        night = Color(0xFF1C1C1E)
    )
    val cardBackground = ColorProvider(
        day = Color(0xFFF5F5F5),
        night = Color(0xFF2C2C2E)
    )
    val title = ColorProvider(
        day = Color(0xFF000000),
        night = Color(0xFFF2F2F7)
    )
    val body = ColorProvider(
        day = Color(0xFF616161),
        night = Color(0xFFEBEBF5)
    )
    val secondary = ColorProvider(
        day = Color(0xFF9E9E9E),
        night = Color(0xFF8E8E93)
    )
    val accent = ColorProvider(
        day = Color(0xFF1D6BF3),
        night = Color(0xFF6B9FFF)
    )
    val addButtonBackground = ColorProvider(
        day = Color(0xFF1D6BF3),
        night = Color(0xFF6B9FFF)
    )
    val addButtonIcon = ColorProvider(
        day = Color(0xFFFFFFFF),
        night = Color(0xFF1C1C1E)
    )
}

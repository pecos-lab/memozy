package me.pecos.memozy.presentation.theme

import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp
import me.pecos.memozy.feature.core.viewmodel.settings.AppFontFamily
import me.pecos.memozy.feature.core.viewmodel.settings.FontSizeLevel

val AppFontFamily.fontFamily: FontFamily
    get() = FontFamily.Default

data class FontSettings(
    val fontFamily: FontFamily = FontFamily.Default,
    val titleSize: TextUnit = 22.sp,
    val bodySize: TextUnit = 15.sp,
    val fontSizeLevel: FontSizeLevel = FontSizeLevel.NORMAL,
    val appFontFamily: AppFontFamily = AppFontFamily.SYSTEM
) {
    fun scaled(baseSp: Int): TextUnit = maxOf(10, baseSp + fontSizeLevel.sizeOffset).sp
}

val LocalFontSettings = compositionLocalOf { FontSettings() }

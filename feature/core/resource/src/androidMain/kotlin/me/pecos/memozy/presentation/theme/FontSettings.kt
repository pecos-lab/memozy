package me.pecos.memozy.presentation.theme

import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp

enum class FontSizeLevel(val titleSp: Int, val bodySp: Int, val sizeOffset: Int) {
    SMALL(20, 13, -2),
    NORMAL(22, 15, 0),
    LARGE(24, 17, 2)
}

enum class AppFontFamily(val value: String, val displayName: String) {
    SYSTEM("system", "시스템 기본");

    val fontFamily: FontFamily
        get() = FontFamily.Default
}

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

package me.pecos.memozy.presentation.theme

import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.googlefonts.Font
import androidx.compose.ui.text.googlefonts.GoogleFont
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp
import me.pecos.memozy.feature.core.resource.R

private val googleFontProvider = GoogleFont.Provider(
    providerAuthority = "com.google.android.gms.fonts",
    providerPackage = "com.google.android.gms",
    certificates = R.array.com_google_android_gms_fonts_certs
)

enum class FontSizeLevel(val titleSp: Int, val bodySp: Int, val sizeOffset: Int) {
    SMALL(20, 13, -2),
    NORMAL(22, 15, 0),
    LARGE(24, 17, 2)
}

enum class AppFontFamily(val value: String, val displayName: String) {
    SYSTEM("system", "시스템 기본"),
    NOTO_SANS_KR("noto_sans_kr", "Noto Sans KR"),
    NANUM_MYEONGJO("nanum_myeongjo", "Nanum Myeongjo"),
    POOR_STORY("poor_story", "Poor Story");

    val fontFamily: FontFamily by lazy {
        when (this) {
            SYSTEM -> FontFamily.Default
            NOTO_SANS_KR -> FontFamily(
                Font(GoogleFont("Noto Sans KR"), googleFontProvider, weight = FontWeight.Normal),
                Font(GoogleFont("Noto Sans KR"), googleFontProvider, weight = FontWeight.Bold),
            )
            NANUM_MYEONGJO -> FontFamily(
                Font(GoogleFont("Nanum Myeongjo"), googleFontProvider, weight = FontWeight.Normal),
                Font(GoogleFont("Nanum Myeongjo"), googleFontProvider, weight = FontWeight.Bold),
            )
            POOR_STORY -> FontFamily(
                Font(GoogleFont("Poor Story"), googleFontProvider, weight = FontWeight.Normal),
            )
        }
    }
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

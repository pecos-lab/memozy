package me.pecos.memozy.presentation.theme

import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

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
    // Action buttons (copy / share / delete / edit)
    val actionBorderWidth: Dp,
    val actionNeutralBg: Color,
    val actionNeutralBorder: Color,
    val actionNeutralTint: Color,
    val actionDeleteBg: Color,
    val actionDeleteBorder: Color,
    val actionDeleteTint: Color,
    val actionEditBg: Color,
    val actionEditBorder: Color,
    val actionEditTint: Color,
)


val lightAppColors = AppColors(
    screenBackground = Color(0xFFFFFFFF),
    topbarTitle      = Color(0xFF1C1C1E),
    navBackground    = Color(0xFFFFFFFF),
    navBorder        = Color(0xFFE0E0E0),
    navIconSelected  = Color(0xFF000000),
    navIconUnselected= Color(0x66000000),
    cardBackground   = Color(0xFFF5F5F5),
    cardBorder       = Color(0xFFE0E0E0),
    textTitle        = Color(0xFF000000),
    textBody         = Color(0xFF616161),
    textSecondary    = Color(0xFF9E9E9E),
    chipBackground   = Color(0xFFEAECF0),
    chipText         = Color(0xFF1D6BF3),
    actionBorderWidth   = 1.5.dp,
    actionNeutralBg     = Color(0x00000000),
    actionNeutralBorder = Color(0xFFAAAAAA),
    actionNeutralTint   = Color(0xFF888888),
    actionDeleteBg      = Color(0x00000000),
    actionDeleteBorder  = Color(0xFFE5735A),
    actionDeleteTint    = Color(0xFFE5735A),
    actionEditBg        = Color(0x00000000),
    actionEditBorder    = Color(0xFF4A9EE8),
    actionEditTint      = Color(0xFF4A9EE8),
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
    actionBorderWidth   = 0.5.dp,
    actionNeutralBg     = Color(0x26AAAAAA),
    actionNeutralBorder = Color(0x59AAAAAA),
    actionNeutralTint   = Color(0xFFAAAAAA),
    actionDeleteBg      = Color(0x2EFF6B4F),
    actionDeleteBorder  = Color(0x66FF6B4F),
    actionDeleteTint    = Color(0xFFFF6B4F),
    actionEditBg        = Color(0x2664B4FF),
    actionEditBorder    = Color(0x5964B4FF),
    actionEditTint      = Color(0xFF64B4FF),
)

val LocalAppColors = staticCompositionLocalOf { lightAppColors }

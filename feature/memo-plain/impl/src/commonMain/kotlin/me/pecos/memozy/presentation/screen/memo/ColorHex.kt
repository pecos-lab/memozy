package me.pecos.memozy.presentation.screen.memo

import androidx.compose.ui.graphics.Color

// 저장된 span 색상(String hex)·툴바 팔레트 hex 를 Compose Color 로 변환하는 commonMain 유틸.
// #RRGGBB / #AARRGGBB 지원. 파싱 실패 시 Color.Unspecified 반환.
// android.graphics.Color.parseColor 를 대체해 androidMain 의존을 제거한다.
internal fun parseHexColor(hex: String): Color {
    return try {
        val cleaned = hex.removePrefix("#")
        when (cleaned.length) {
            6 -> {
                val r = cleaned.substring(0, 2).toInt(16)
                val g = cleaned.substring(2, 4).toInt(16)
                val b = cleaned.substring(4, 6).toInt(16)
                Color(red = r, green = g, blue = b)
            }
            8 -> {
                val a = cleaned.substring(0, 2).toInt(16)
                val r = cleaned.substring(2, 4).toInt(16)
                val g = cleaned.substring(4, 6).toInt(16)
                val b = cleaned.substring(6, 8).toInt(16)
                Color(red = r, green = g, blue = b, alpha = a)
            }
            else -> Color.Unspecified
        }
    } catch (_: Exception) {
        Color.Unspecified
    }
}

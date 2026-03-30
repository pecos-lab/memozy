package me.pecos.memozy.presentation.screen.home.util

import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

// ── 시간 포맷 ───────────────────────────────────────────────────────────────────

fun formatMemoTime(createdAt: Long, languageCode: String): String {
    if (createdAt == 0L) return ""

    val zone = ZoneId.systemDefault()
    val created = Instant.ofEpochMilli(createdAt).atZone(zone).toLocalDateTime()
    val today = LocalDate.now(zone)
    val yesterday = today.minusDays(1)
    val createdDate = created.toLocalDate()

    return when {
        createdDate == today -> {
            val hour = created.hour
            val minute = created.minute
            when (languageCode) {
                "en" -> {
                    val ampm = if (hour < 12) "AM" else "PM"
                    val h = if (hour % 12 == 0) 12 else hour % 12
                    "$h:${minute.toString().padStart(2, '0')} $ampm"
                }
                "ja" -> {
                    val ampm = if (hour < 12) "午前" else "午後"
                    val h = if (hour % 12 == 0) 12 else hour % 12
                    "$ampm${h}:${minute.toString().padStart(2, '0')}"
                }
                else -> {
                    val ampm = if (hour < 12) "오전" else "오후"
                    val h = if (hour % 12 == 0) 12 else hour % 12
                    "$ampm ${h}:${minute.toString().padStart(2, '0')}"
                }
            }
        }
        createdDate == yesterday -> when (languageCode) {
            "en" -> "Yesterday"
            "ja" -> "昨日"
            else -> "어제"
        }
        else -> {
            val y = created.year
            val m = created.monthValue.toString().padStart(2, '0')
            val d = created.dayOfMonth.toString().padStart(2, '0')
            "$y.$m.$d"
        }
    }
}

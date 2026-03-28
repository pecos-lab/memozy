package me.pecos.nota.presentation.screen.home.util

import org.joda.time.DateTime
import org.joda.time.LocalDate
import org.joda.time.format.DateTimeFormat

// ── 시간 포맷 ───────────────────────────────────────────────────────────────────

fun formatMemoTime(createdAt: Long, languageCode: String): String {
    if (createdAt == 0L) return ""

    val created = DateTime(createdAt)
    val today = LocalDate.now()
    val yesterday = today.minusDays(1)
    val createdDate = created.toLocalDate()

    return when {
        createdDate.isEqual(today) -> {
            val hour = created.hourOfDay
            val minute = created.minuteOfHour
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
        createdDate.isEqual(yesterday) -> when (languageCode) {
            "en" -> "Yesterday"
            "ja" -> "昨日"
            else -> "어제"
        }
        else -> DateTimeFormat.forPattern("yyyy.MM.dd").print(created)
    }
}

package me.pecos.nota

// ── 시간 포맷 ───────────────────────────────────────────────────────────────────

fun timezoneForLanguage(languageCode: String): java.util.TimeZone = when (languageCode) {
    "ko" -> java.util.TimeZone.getTimeZone("Asia/Seoul")
    "ja" -> java.util.TimeZone.getTimeZone("Asia/Tokyo")
    "en" -> java.util.TimeZone.getTimeZone("America/New_York")
    else -> java.util.TimeZone.getDefault()
}

fun formatMemoTime(createdAt: Long, languageCode: String): String {
    if (createdAt == 0L) return ""
    val tz = timezoneForLanguage(languageCode)
    val now = java.util.Calendar.getInstance(tz)
    val created = java.util.Calendar.getInstance(tz).apply { timeInMillis = createdAt }

    val sameDay = now.get(java.util.Calendar.YEAR) == created.get(java.util.Calendar.YEAR) &&
            now.get(java.util.Calendar.DAY_OF_YEAR) == created.get(java.util.Calendar.DAY_OF_YEAR)
    val yesterday = run {
        val y = java.util.Calendar.getInstance(tz)
            .apply { timeInMillis = createdAt; add(java.util.Calendar.DAY_OF_YEAR, 1) }
        y.get(java.util.Calendar.YEAR) == now.get(java.util.Calendar.YEAR) &&
                y.get(java.util.Calendar.DAY_OF_YEAR) == now.get(java.util.Calendar.DAY_OF_YEAR)
    }

    return when {
        sameDay -> {
            val hour = created.get(java.util.Calendar.HOUR_OF_DAY)
            val minute = created.get(java.util.Calendar.MINUTE)
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
        yesterday -> when (languageCode) {
            "en" -> "Yesterday"
            "ja" -> "昨日"
            else -> "어제"
        }
        else -> {
            val year = created.get(java.util.Calendar.YEAR)
            val month = created.get(java.util.Calendar.MONTH) + 1
            val day = created.get(java.util.Calendar.DAY_OF_MONTH)
            "${year}.${month.toString().padStart(2, '0')}.${day.toString().padStart(2, '0')}"
        }
    }
}

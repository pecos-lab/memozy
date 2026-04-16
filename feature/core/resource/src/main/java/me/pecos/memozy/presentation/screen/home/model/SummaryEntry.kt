package me.pecos.memozy.presentation.screen.home.model

import org.json.JSONArray
import org.json.JSONObject

data class SummaryEntry(
    val content: String,
    val mode: String = "SIMPLE",
    val createdAt: Long = System.currentTimeMillis()
)

fun List<SummaryEntry>.toJson(): String {
    val array = JSONArray()
    for (entry in this) {
        array.put(JSONObject().apply {
            put("content", entry.content)
            put("mode", entry.mode)
            put("createdAt", entry.createdAt)
        })
    }
    return array.toString()
}

fun parseSummaryEntries(json: String?): List<SummaryEntry> {
    if (json.isNullOrBlank()) return emptyList()
    return try {
        if (json.trimStart().startsWith("[")) {
            val array = JSONArray(json)
            (0 until array.length()).map { i ->
                val obj = array.getJSONObject(i)
                SummaryEntry(
                    content = obj.getString("content"),
                    mode = obj.optString("mode", "SIMPLE"),
                    createdAt = obj.optLong("createdAt", 0L)
                )
            }
        } else {
            // Legacy: plain text → single entry
            listOf(SummaryEntry(content = json))
        }
    } catch (_: Exception) {
        if (json.isNotBlank()) listOf(SummaryEntry(content = json)) else emptyList()
    }
}

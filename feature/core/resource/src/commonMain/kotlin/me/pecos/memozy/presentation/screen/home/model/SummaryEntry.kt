package me.pecos.memozy.presentation.screen.home.model

import kotlin.time.Clock
import kotlin.time.ExperimentalTime
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

private val SUMMARY_JSON = Json { ignoreUnknownKeys = true; isLenient = true }

@OptIn(ExperimentalTime::class)
@Serializable
data class SummaryEntry(
    val content: String,
    val mode: String = "SIMPLE",
    @SerialName("createdAt") val createdAt: Long = Clock.System.now().toEpochMilliseconds()
)

fun List<SummaryEntry>.toJson(): String {
    val array = buildJsonArray {
        for (entry in this@toJson) {
            add(
                buildJsonObject {
                    put("content", JsonPrimitive(entry.content))
                    put("mode", JsonPrimitive(entry.mode))
                    put("createdAt", JsonPrimitive(entry.createdAt))
                }
            )
        }
    }
    return array.toString()
}

fun parseSummaryEntries(json: String?): List<SummaryEntry> {
    if (json.isNullOrBlank()) return emptyList()
    return try {
        if (json.trimStart().startsWith("[")) {
            val element: JsonElement = SUMMARY_JSON.parseToJsonElement(json)
            val array: JsonArray = element.jsonArray
            array.map { item ->
                val obj = item.jsonObject
                SummaryEntry(
                    content = obj["content"]?.jsonPrimitive?.content ?: "",
                    mode = obj["mode"]?.jsonPrimitive?.content ?: "SIMPLE",
                    createdAt = obj["createdAt"]?.jsonPrimitive?.content?.toLongOrNull() ?: 0L
                )
            }
        } else {
            listOf(SummaryEntry(content = json))
        }
    } catch (_: Exception) {
        if (!json.isNullOrBlank()) listOf(SummaryEntry(content = json)) else emptyList()
    }
}

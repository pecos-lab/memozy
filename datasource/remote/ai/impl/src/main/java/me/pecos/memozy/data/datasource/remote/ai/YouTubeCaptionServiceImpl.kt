package me.pecos.memozy.data.datasource.remote.ai

import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.statement.bodyAsText
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class YouTubeCaptionServiceImpl @Inject constructor(
    private val httpClient: HttpClient,
    private val json: Json
) : YouTubeCaptionService {

    override suspend fun extractCaptions(videoId: String): String? {
        return extractVideoInfo(videoId)?.captions
    }

    override suspend fun extractVideoInfo(videoId: String): YouTubeVideoInfo? {
        return try {
            // 1. YouTube 페이지에서 제목 추출
            val title = fetchTitle(videoId)

            // 2. Supadata API로 자막 추출 (ko → en fallback)
            val captions = fetchCaptionsFromSupadata(videoId, "ko")
                ?: fetchCaptionsFromSupadata(videoId, "en")

            YouTubeVideoInfo(
                title = title ?: "YouTube 영상",
                captions = captions
            )
        } catch (e: Exception) {
            null
        }
    }

    override suspend fun fetchTitle(videoId: String): String? {
        return try {
            val responseText = httpClient.get("youtube-title") {
                parameter("videoId", videoId)
            }.bodyAsText()

            val root = json.parseToJsonElement(responseText).jsonObject
            val title = root["title"]?.jsonPrimitive?.content
            title?.takeIf { it != "null" }
        } catch (e: Exception) {
            null
        }
    }

    private suspend fun fetchCaptionsFromSupadata(videoId: String, lang: String = "ko"): String? {
        return try {
            val responseText = httpClient.get("youtube-captions") {
                parameter("url", "https://www.youtube.com/watch?v=$videoId")
                parameter("lang", lang)
            }.bodyAsText()

            val root = json.parseToJsonElement(responseText).jsonObject
            val content = root["content"]?.jsonArray ?: return null

            val sb = StringBuilder()
            for (item in content) {
                val obj = item.jsonObject
                val text = obj["text"]?.jsonPrimitive?.content?.trim() ?: continue
                val offsetMs = obj["offset"]?.jsonPrimitive?.content?.toLongOrNull() ?: 0L
                val sec = offsetMs / 1000
                val minutes = (sec / 60).toInt()
                val seconds = (sec % 60).toInt()
                sb.appendLine("[${String.format("%02d:%02d", minutes, seconds)}] $text")
            }
            sb.toString().takeIf { it.isNotBlank() }
        } catch (e: Exception) {
            null
        }
    }
}

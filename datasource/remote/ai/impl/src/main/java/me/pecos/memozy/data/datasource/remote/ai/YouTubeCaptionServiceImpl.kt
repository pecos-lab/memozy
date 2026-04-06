package me.pecos.memozy.data.datasource.remote.ai

import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.statement.bodyAsText
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import me.pecos.memozy.data.datasource.remote.ai.di.YouTubeHttpClient
import me.pecos.memozy.datasource.remote.ai.impl.BuildConfig
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class YouTubeCaptionServiceImpl @Inject constructor(
    @YouTubeHttpClient private val httpClient: HttpClient,
    private val json: Json
) : YouTubeCaptionService {

    override suspend fun extractCaptions(videoId: String): String? {
        return extractVideoInfo(videoId)?.captions
    }

    override suspend fun extractVideoInfo(videoId: String): YouTubeVideoInfo? {
        return try {
            // 1. YouTube 페이지에서 제목 추출
            val title = fetchTitle(videoId)

            // 2. Supadata API로 자막 추출
            val captions = fetchCaptionsFromSupadata(videoId)

            YouTubeVideoInfo(
                title = title ?: "YouTube 영상",
                captions = captions
            )
        } catch (e: Exception) {
            null
        }
    }

    private suspend fun fetchTitle(videoId: String): String? {
        return try {
            val pageHtml = httpClient.get("https://www.youtube.com/watch?v=$videoId") {
                header("Accept-Language", "ko-KR,ko;q=0.9,en;q=0.8")
                header("User-Agent", "Mozilla/5.0 (Linux; Android 14) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Mobile Safari/537.36")
            }.bodyAsText()

            val playerResponse = parsePlayerResponse(pageHtml) ?: return null
            playerResponse["videoDetails"]
                ?.jsonObject?.get("title")
                ?.jsonPrimitive?.content
        } catch (e: Exception) {
            null
        }
    }

    private suspend fun fetchCaptionsFromSupadata(videoId: String): String? {
        val apiKey = BuildConfig.SUPADATA_API_KEY
        if (apiKey.isBlank()) return null

        return try {
            val responseText = httpClient.get(
                "https://api.supadata.ai/v1/youtube/transcript?url=https://www.youtube.com/watch?v=$videoId&lang=ko"
            ) {
                header("x-api-key", apiKey)
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

    private fun parsePlayerResponse(html: String): kotlinx.serialization.json.JsonObject? {
        val marker = "ytInitialPlayerResponse = {"
        var startIdx = html.indexOf(marker)
        while (startIdx != -1) {
            val jsonStart = startIdx + marker.length - 1
            var depth = 0
            var i = jsonStart
            while (i < html.length) {
                when (html[i]) {
                    '{' -> depth++
                    '}' -> {
                        depth--
                        if (depth == 0) break
                    }
                }
                i++
            }
            if (depth == 0 && i < html.length) {
                val jsonStr = html.substring(jsonStart, i + 1)
                try {
                    return json.parseToJsonElement(jsonStr).jsonObject
                } catch (_: Exception) { }
            }
            startIdx = html.indexOf(marker, startIdx + 1)
        }
        return null
    }
}

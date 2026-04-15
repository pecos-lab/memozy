package me.pecos.memozy.data.datasource.remote.ai

import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.statement.bodyAsText
import kotlinx.serialization.json.Json
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
            // 1번의 API 호출로 자막 + 제목 동시 획득
            // Worker가 내부에서 ko→en 폴백 처리 + 응답에 title 포함
            val responseText = httpClient.get("youtube-captions") {
                parameter("url", "https://www.youtube.com/watch?v=$videoId")
                parameter("lang", "ko")
            }.bodyAsText()

            val root = json.parseToJsonElement(responseText).jsonObject
            if (root.containsKey("error")) return null

            val captions = root["content"]?.jsonPrimitive?.content?.takeIf { it.isNotBlank() }
            val title = root["title"]?.jsonPrimitive?.content?.takeIf { it != "null" && it.isNotBlank() }
                ?: "YouTube 영상"

            YouTubeVideoInfo(
                title = title,
                captions = captions
            )
        } catch (e: Exception) {
            null
        }
    }

    override suspend fun fetchTitle(videoId: String): String? {
        // oembed API로 제목만 빠르게 가져오기 (자막 추출 없이)
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
}

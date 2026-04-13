package me.pecos.memozy.data.datasource.remote.ai

import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.statement.bodyAsText
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
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
            coroutineScope {
                // 제목과 자막을 병렬로 가져오기
                val titleDeferred = async { fetchTitle(videoId) }
                val captionsDeferred = async {
                    fetchCaptionsFromWorker(videoId, "ko")
                        ?: fetchCaptionsFromWorker(videoId, "en")
                }

                YouTubeVideoInfo(
                    title = titleDeferred.await() ?: "YouTube 영상",
                    captions = captionsDeferred.await()
                )
            }
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

    private suspend fun fetchCaptionsFromWorker(videoId: String, lang: String = "ko"): String? {
        return try {
            val responseText = httpClient.get("youtube-captions") {
                parameter("url", "https://www.youtube.com/watch?v=$videoId")
                parameter("lang", lang)
            }.bodyAsText()

            val root = json.parseToJsonElement(responseText).jsonObject
            if (root.containsKey("error")) return null
            root["content"]?.jsonPrimitive?.content?.takeIf { it.isNotBlank() }
        } catch (e: Exception) {
            null
        }
    }
}

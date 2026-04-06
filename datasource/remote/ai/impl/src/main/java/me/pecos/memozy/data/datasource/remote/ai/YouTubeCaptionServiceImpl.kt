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
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class YouTubeCaptionServiceImpl @Inject constructor(
    @YouTubeHttpClient private val httpClient: HttpClient,
    private val json: Json
) : YouTubeCaptionService {

    override suspend fun extractCaptions(videoId: String): String? {
        return try {
            val pageHtml = httpClient.get("https://www.youtube.com/watch?v=$videoId") {
                header("Accept-Language", "ko-KR,ko;q=0.9,en;q=0.8")
                header("User-Agent", "Mozilla/5.0 (Linux; Android 14) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Mobile Safari/537.36")
            }.bodyAsText()

            val captionUrl = extractCaptionUrl(pageHtml) ?: return null
            val captionXml = httpClient.get(captionUrl).bodyAsText()
            parseCaptionXml(captionXml)
        } catch (e: Exception) {
            null
        }
    }

    private fun extractCaptionUrl(html: String): String? {
        val playerResponsePattern = Regex("""ytInitialPlayerResponse\s*=\s*(\{.+?\})\s*;""")
        val match = playerResponsePattern.find(html) ?: return null
        val jsonStr = match.groupValues[1]

        return try {
            val root = json.parseToJsonElement(jsonStr).jsonObject
            val captionTracks = root["captions"]
                ?.jsonObject?.get("playerCaptionsTracklistRenderer")
                ?.jsonObject?.get("captionTracks")
                ?.jsonArray ?: return null

            // 한국어 우선, 없으면 영어, 없으면 첫 번째 트랙
            val track = captionTracks.firstOrNull { track ->
                val lang = track.jsonObject["languageCode"]?.jsonPrimitive?.content
                lang == "ko"
            } ?: captionTracks.firstOrNull { track ->
                val lang = track.jsonObject["languageCode"]?.jsonPrimitive?.content
                lang == "en"
            } ?: captionTracks.firstOrNull()

            track?.jsonObject?.get("baseUrl")?.jsonPrimitive?.content
        } catch (e: Exception) {
            null
        }
    }

    private fun parseCaptionXml(xml: String): String? {
        val textPattern = Regex("""<text[^>]*start="([^"]*)"[^>]*>([^<]*)</text>""")
        val matches = textPattern.findAll(xml).toList()
        if (matches.isEmpty()) return null

        val sb = StringBuilder()
        for (match in matches) {
            val startSec = match.groupValues[1].toDoubleOrNull() ?: 0.0
            val text = match.groupValues[2]
                .replace("&amp;", "&")
                .replace("&lt;", "<")
                .replace("&gt;", ">")
                .replace("&quot;", "\"")
                .replace("&#39;", "'")
                .trim()
            if (text.isNotEmpty()) {
                val minutes = (startSec / 60).toInt()
                val seconds = (startSec % 60).toInt()
                sb.appendLine("[${String.format("%02d:%02d", minutes, seconds)}] $text")
            }
        }
        return sb.toString().takeIf { it.isNotBlank() }
    }
}

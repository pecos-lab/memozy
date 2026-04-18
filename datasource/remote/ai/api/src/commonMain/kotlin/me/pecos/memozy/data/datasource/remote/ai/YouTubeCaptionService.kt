package me.pecos.memozy.data.datasource.remote.ai

data class YouTubeVideoInfo(
    val title: String,
    val captions: String?
)

interface YouTubeCaptionService {
    suspend fun extractCaptions(videoId: String): String?
    suspend fun extractVideoInfo(videoId: String): YouTubeVideoInfo?
    suspend fun fetchTitle(videoId: String): String?
}

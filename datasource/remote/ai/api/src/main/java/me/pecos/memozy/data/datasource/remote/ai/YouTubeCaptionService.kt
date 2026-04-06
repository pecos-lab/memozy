package me.pecos.memozy.data.datasource.remote.ai

interface YouTubeCaptionService {
    suspend fun extractCaptions(videoId: String): String?
}

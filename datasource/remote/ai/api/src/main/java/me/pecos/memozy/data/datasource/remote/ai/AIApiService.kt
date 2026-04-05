package me.pecos.memozy.data.datasource.remote.ai

interface AIApiService {
    suspend fun generateContent(prompt: String): String
    suspend fun generateContentWithVideo(prompt: String, videoUrl: String): String
}

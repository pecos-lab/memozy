package me.pecos.memozy.data.datasource.remote.ai

import kotlinx.coroutines.flow.Flow

interface AIApiService {
    suspend fun generateContent(prompt: String): String
    suspend fun generateContentWithVideo(prompt: String, videoUrl: String): String
    fun generateContentStream(prompt: String): Flow<String>
    suspend fun transcribeAudio(audioBase64: String, mimeType: String, durationSeconds: Long = 0): String
    suspend fun describeImage(imageBase64: String, mimeType: String): String
}

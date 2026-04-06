package me.pecos.memozy.data.datasource.remote.ai

import kotlinx.coroutines.flow.Flow

interface AIApiService {
    suspend fun generateContent(prompt: String): String
    suspend fun generateContentWithVideo(prompt: String, videoUrl: String): String
    fun generateContentStream(prompt: String): Flow<String>
}

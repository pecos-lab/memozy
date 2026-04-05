package me.pecos.memozy.data.datasource.remote.ai

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import me.pecos.memozy.data.datasource.remote.ai.model.GeminiContent
import me.pecos.memozy.data.datasource.remote.ai.model.GeminiFileData
import me.pecos.memozy.data.datasource.remote.ai.model.GeminiPart
import me.pecos.memozy.data.datasource.remote.ai.model.GeminiRequest
import me.pecos.memozy.data.datasource.remote.ai.model.GeminiResponse
import me.pecos.memozy.datasource.remote.ai.impl.BuildConfig
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AIApiServiceImpl @Inject constructor(
    private val httpClient: HttpClient,
) : AIApiService {

    override suspend fun generateContent(prompt: String): String {
        val request = GeminiRequest(
            contents = listOf(
                GeminiContent(
                    parts = listOf(GeminiPart(text = prompt))
                )
            )
        )

        return executeRequest(request)
    }

    override suspend fun generateContentWithVideo(prompt: String, videoUrl: String): String {
        val request = GeminiRequest(
            contents = listOf(
                GeminiContent(
                    parts = listOf(
                        GeminiPart(
                            fileData = GeminiFileData(
                                mimeType = "video/*",
                                fileUri = videoUrl,
                            )
                        ),
                        GeminiPart(text = prompt),
                    )
                )
            )
        )

        return executeRequest(request)
    }

    private suspend fun executeRequest(request: GeminiRequest): String {
        val response: GeminiResponse = httpClient.post(
            "models/${BuildConfig.AI_MODEL}:generateContent?key=${BuildConfig.AI_API_KEY}"
        ) {
            contentType(ContentType.Application.Json)
            setBody(request)
        }.body()

        return response.candidates
            ?.firstOrNull()
            ?.content
            ?.parts
            ?.firstOrNull()
            ?.text
            ?: throw AIException.UnknownException("Empty response from Gemini")
    }
}

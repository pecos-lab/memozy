package me.pecos.memozy.data.datasource.remote.ai

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.post
import io.ktor.client.request.preparePost
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsChannel
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.utils.io.readUTF8Line
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.serialization.json.Json
import me.pecos.memozy.data.datasource.remote.ai.model.GeminiContent
import me.pecos.memozy.data.datasource.remote.ai.model.GeminiFileData
import me.pecos.memozy.data.datasource.remote.ai.model.GeminiInlineData
import me.pecos.memozy.data.datasource.remote.ai.model.GeminiPart
import me.pecos.memozy.data.datasource.remote.ai.model.GenerationConfig
import me.pecos.memozy.data.datasource.remote.ai.model.GeminiRequest
import me.pecos.memozy.data.datasource.remote.ai.model.GeminiResponse

import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AIApiServiceImpl @Inject constructor(
    private val httpClient: HttpClient,
    private val json: Json,
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

    override fun generateContentStream(prompt: String, longOutput: Boolean): Flow<String> =
        generateContentStreamInternal(
            prompt,
            if (longOutput) GenerationConfig.THINKING_DISABLED_LONG_OUTPUT
            else GenerationConfig.THINKING_DISABLED
        )

    private fun generateContentStreamInternal(prompt: String, config: GenerationConfig): Flow<String> = flow {
        val request = GeminiRequest(
            contents = listOf(
                GeminiContent(
                    parts = listOf(GeminiPart(text = prompt))
                )
            ),
            generationConfig = config
        )

        var hasContent = false
        httpClient.preparePost("gemini-stream") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }.execute { response ->
            val channel = response.bodyAsChannel()
            while (!channel.isClosedForRead) {
                val line = channel.readUTF8Line() ?: break
                if (line.startsWith("data: ")) {
                    val jsonStr = line.removePrefix("data: ").trim()
                    if (jsonStr.isNotEmpty()) {
                        try {
                            val chunk = json.decodeFromString<GeminiResponse>(jsonStr)
                            val text = chunk.candidates
                                ?.firstOrNull()
                                ?.content
                                ?.parts
                                ?.firstOrNull()
                                ?.text
                            if (text != null) {
                                hasContent = true
                                emit(text) // 델타만 emit (O(n) 최적화)
                            }
                        } catch (_: Exception) { }
                    }
                }
            }
        }

        if (!hasContent) {
            throw AIException.UnknownException("Empty streaming response from Gemini")
        }
    }

    override suspend fun transcribeAudio(audioBase64: String, mimeType: String, durationSeconds: Long): String {
        val prompt = "이 오디오를 한국어로 받아쓰기해줘. 텍스트만 출력하고 다른 설명은 하지 마."

        val request = GeminiRequest(
            contents = listOf(
                GeminiContent(
                    parts = listOf(
                        GeminiPart(
                            inlineData = GeminiInlineData(
                                mimeType = mimeType,
                                data = audioBase64,
                            )
                        ),
                        GeminiPart(text = prompt),
                    )
                )
            )
        )

        return executeRequest(request)
    }

    override suspend fun describeImage(imageBase64: String, mimeType: String): String {
        val prompt = "이 이미지의 텍스트를 모두 추출해줘. 텍스트가 없으면 이미지 내용을 간결하게 설명해줘. 텍스트만 출력하고 다른 설명은 하지 마."

        val request = GeminiRequest(
            contents = listOf(
                GeminiContent(
                    parts = listOf(
                        GeminiPart(
                            inlineData = GeminiInlineData(
                                mimeType = mimeType,
                                data = imageBase64,
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
        val response: GeminiResponse = httpClient.post("gemini-generate") {
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

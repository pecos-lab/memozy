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

class AIApiServiceImpl(
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
        // 짧은/무음 오디오에 LLM 이 한국 콘텐츠 fabricate 하는 패턴(예: "톡쏘는 정치 김혜영입니다",
        // "구독 좋아요 부탁드립니다") 방지 — 엄격한 instruction.
        val prompt = """
            너는 한국어 음성을 받아쓰기하는 도구야. 다음 규칙을 반드시 지켜:

            1. 실제로 들리는 사람의 한국어 발화만 그대로 텍스트로 옮긴다.
            2. 들리는 내용이 없거나, 무음이거나, 잡음만 있거나, 너무 짧아서 단어를 식별할 수 없으면 정확히 빈 문자열("")만 반환한다.
            3. 절대로 추측하거나 보충하거나 창작하지 않는다. 들린 단어가 불확실하면 빈 문자열을 반환한다.
            4. 인사말("안녕하세요"), 방송 클로징("구독 좋아요"), 뉴스/정치 멘트 등은 실제로 명확히 들렸을 때만 출력한다.
            5. 다른 설명·해설·따옴표·접두어 없이 받아쓰기 결과 텍스트만 출력한다.

            오디오 길이: ${durationSeconds}초
        """.trimIndent()

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

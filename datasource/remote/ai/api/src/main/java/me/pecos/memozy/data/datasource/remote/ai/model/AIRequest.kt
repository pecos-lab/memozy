package me.pecos.memozy.data.datasource.remote.ai.model

import kotlinx.serialization.Serializable

@Serializable
data class GeminiRequest(
    val contents: List<GeminiContent>,
    val generationConfig: GenerationConfig = GenerationConfig.THINKING_DISABLED,
)

@Serializable
data class GenerationConfig(
    val thinkingConfig: ThinkingConfig? = null,
    val maxOutputTokens: Int? = null,
) {
    companion object {
        private const val MAX_OUTPUT_TOKENS_LONG = 65536

        val THINKING_DISABLED = GenerationConfig(
            thinkingConfig = ThinkingConfig(thinkingBudget = 0)
        )
        val THINKING_DISABLED_LONG_OUTPUT = GenerationConfig(
            thinkingConfig = ThinkingConfig(thinkingBudget = 0),
            maxOutputTokens = MAX_OUTPUT_TOKENS_LONG
        )
    }
}

@Serializable
data class ThinkingConfig(
    val thinkingBudget: Int,
)

@Serializable
data class GeminiContent(
    val role: String = "user",
    val parts: List<GeminiPart>,
)

@Serializable
data class GeminiPart(
    val text: String? = null,
    val fileData: GeminiFileData? = null,
    val inlineData: GeminiInlineData? = null,
)

@Serializable
data class GeminiFileData(
    val mimeType: String,
    val fileUri: String,
)

@Serializable
data class GeminiInlineData(
    val mimeType: String,
    val data: String, // base64 encoded
)

package me.pecos.memozy.data.datasource.remote.ai.model

import kotlinx.serialization.Serializable

@Serializable
data class GeminiRequest(
    val contents: List<GeminiContent>,
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
)

@Serializable
data class GeminiFileData(
    val mimeType: String,
    val fileUri: String,
)

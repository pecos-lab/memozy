package me.pecos.memozy.presentation.screen.home.model

data class MemoUiState(
    val id: Int,
    val name: String,
    val categoryId: Int,
    val content: String,
    val createdAt: Long = 0L,
    val updatedAt: Long = 0L,
    val format: MemoFormatUi = MemoFormatUi.PLAIN,
    val isPinned: Boolean = false,
    val audioPath: String? = null,
    val styles: String? = null,
    val youtubeUrl: String? = null,
    val deletedAt: Long? = null,
    val tags: List<TagUiState> = emptyList()
)

data class TagUiState(
    val id: Int,
    val name: String,
    val emoji: String = "🏷️"
)

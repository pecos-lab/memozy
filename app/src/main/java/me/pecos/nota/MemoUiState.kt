package me.pecos.nota

data class MemoUiState(
    val id: Int,
    val name: String,
    val categoryId: Int,
    val content: String,
    val createdAt: Long = 0L,
    val format: MemoFormatUi = MemoFormatUi.PLAIN
)

package me.pecos.nota

data class MemoUiState(
    val id: Int,
    val name: String,
    val category: String,
    val content: String,
    val createdAt: Long = 0L,
    val format: String = "plain"
)

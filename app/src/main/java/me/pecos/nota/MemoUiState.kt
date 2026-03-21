package me.pecos.nota

data class MemoUiState(
    val id: Int,
    val name: String,
    val category: MemoCategory,
    val killThePecos: String,
    val createdAt: Long = 0L
)

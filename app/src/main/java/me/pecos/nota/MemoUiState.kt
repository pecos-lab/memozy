package me.pecos.nota

data class MemoUiState(
    val id: Int,
    val name: String,
    val sex: String,
    val killThePecos: String,
    val createdAt: Long = 0L
)

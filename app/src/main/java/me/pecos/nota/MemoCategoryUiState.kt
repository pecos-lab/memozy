package me.pecos.nota

enum class MemoCategoryUiState(val code: Int) {
    GENERAL(0),
    WORK(1),
    IDEA(2),
    TODO(3),
    STUDY(4),
    PERSONAL(5),
    SCHEDULE(6),
    BUDGET(7);

    companion object {
        fun fromCode(code: Int): MemoCategoryUiState = entries.find { it.code == code } ?: GENERAL
    }
}

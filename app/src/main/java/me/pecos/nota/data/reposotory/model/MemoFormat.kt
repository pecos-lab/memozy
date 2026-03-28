package me.pecos.nota.data.reposotory.model

// Data layer enum — Room DB에 저장되는 값 기준
enum class MemoFormat {
    PLAIN,
    MARKDOWN;

    fun toDbValue(): String = name.lowercase()

    companion object {
        fun fromDbValue(value: String): MemoFormat =
            entries.firstOrNull { it.name.equals(value, ignoreCase = true) } ?: PLAIN
    }
}
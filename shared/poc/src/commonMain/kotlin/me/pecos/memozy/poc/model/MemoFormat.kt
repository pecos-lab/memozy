package me.pecos.memozy.poc.model

enum class MemoFormat {
    PLAIN,
    MARKDOWN;

    fun toDbValue(): String = name.lowercase()

    companion object {
        fun fromDbValue(value: String): MemoFormat =
            entries.firstOrNull { it.name.equals(value, ignoreCase = true) } ?: PLAIN
    }
}

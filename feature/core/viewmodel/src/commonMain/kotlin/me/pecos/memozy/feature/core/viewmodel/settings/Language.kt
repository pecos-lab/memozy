package me.pecos.memozy.feature.core.viewmodel.settings

data class Language(val name: String, val code: String)

val LANGUAGES = listOf(
    Language("한국어", "ko"),
    Language("English", "en"),
    Language("日本語", "ja"),
)

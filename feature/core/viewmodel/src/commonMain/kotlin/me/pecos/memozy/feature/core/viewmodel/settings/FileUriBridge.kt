package me.pecos.memozy.feature.core.viewmodel.settings

interface FileUriBridge {
    suspend fun readText(uri: String): String
    suspend fun writeText(uri: String, content: String)
}

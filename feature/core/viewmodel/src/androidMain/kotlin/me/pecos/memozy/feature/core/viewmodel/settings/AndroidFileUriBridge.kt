package me.pecos.memozy.feature.core.viewmodel.settings

import android.content.Context
import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class AndroidFileUriBridge(
    private val context: Context,
) : FileUriBridge {

    override suspend fun readText(uri: String): String = withContext(Dispatchers.IO) {
        val parsed = Uri.parse(uri)
        context.contentResolver.openInputStream(parsed)?.use { input ->
            input.bufferedReader().readText()
        } ?: throw IllegalStateException("Cannot open input stream for $uri")
    }

    override suspend fun writeText(uri: String, content: String) = withContext(Dispatchers.IO) {
        val parsed = Uri.parse(uri)
        context.contentResolver.openOutputStream(parsed, "wt")?.use { output ->
            output.write(content.toByteArray(Charsets.UTF_8))
        } ?: throw IllegalStateException("Cannot open output stream for $uri")
    }
}

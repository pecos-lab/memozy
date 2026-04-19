package me.pecos.memozy.platform.intent

import android.content.Context
import android.net.Uri

class AndroidSharedContentReader(
    private val context: Context,
) : SharedContentReader {
    override fun readBytes(uri: String): ByteArray? = try {
        context.contentResolver.openInputStream(Uri.parse(uri))?.use { it.readBytes() }
    } catch (_: Exception) {
        null
    }

    override fun getMimeType(uri: String): String? = try {
        context.contentResolver.getType(Uri.parse(uri))
    } catch (_: Exception) {
        null
    }
}

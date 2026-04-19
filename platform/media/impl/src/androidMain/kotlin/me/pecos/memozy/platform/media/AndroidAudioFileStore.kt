package me.pecos.memozy.platform.media

import android.content.Context
import android.os.Environment
import java.io.File

class AndroidAudioFileStore(
    private val context: Context,
) : AudioFileStore {
    override fun cachePath(name: String): String =
        File(context.cacheDir, name).absolutePath

    override fun permanentPath(safeName: String): String {
        val audioDir = File(context.filesDir, AUDIO_SUBDIR).apply {
            if (!exists()) mkdirs()
        }
        return File(audioDir, "$safeName.$AUDIO_EXT").absolutePath
    }

    override fun downloadsPath(name: String): String? {
        @Suppress("DEPRECATION")
        val downloads = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
        return File(downloads, name).absolutePath
    }

    override fun exists(path: String): Boolean = File(path).exists()

    override fun copy(srcPath: String, dstPath: String): Boolean {
        val src = File(srcPath)
        if (!src.exists()) return false
        val dst = File(dstPath)
        dst.parentFile?.let { if (!it.exists()) it.mkdirs() }
        src.inputStream().use { input ->
            dst.outputStream().use { output ->
                input.copyTo(output)
            }
        }
        return true
    }

    override fun readBytes(path: String): ByteArray = File(path).readBytes()

    override fun length(path: String): Long = File(path).length()

    override fun delete(path: String): Boolean = File(path).delete()

    private companion object {
        const val AUDIO_SUBDIR = "audio"
        const val AUDIO_EXT = "m4a"
    }
}

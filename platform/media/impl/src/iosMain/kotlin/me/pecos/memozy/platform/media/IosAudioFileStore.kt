package me.pecos.memozy.platform.media

import kotlinx.cinterop.ExperimentalForeignApi
import platform.Foundation.NSCachesDirectory
import platform.Foundation.NSDocumentDirectory
import platform.Foundation.NSFileManager
import platform.Foundation.NSSearchPathForDirectoriesInDomains
import platform.Foundation.NSUserDomainMask

/**
 * iOS 구현. NSFileManager + NSCachesDirectory / NSDocumentDirectory 기반.
 * 공용 Downloads 디렉터리는 iOS 샌드박스에 존재하지 않으므로 downloadsPath 는 null 을 반환한다.
 */
@OptIn(ExperimentalForeignApi::class)
class IosAudioFileStore : AudioFileStore {
    private val fileManager: NSFileManager get() = NSFileManager.defaultManager

    override fun cachePath(name: String): String {
        val dir = firstPath(NSCachesDirectory)
        return "$dir/$name"
    }

    override fun permanentPath(safeName: String): String {
        val dir = firstPath(NSDocumentDirectory)
        val audioDir = "$dir/$AUDIO_SUBDIR"
        fileManager.createDirectoryAtPath(
            audioDir,
            withIntermediateDirectories = true,
            attributes = null,
            error = null,
        )
        return "$audioDir/$safeName.$AUDIO_EXT"
    }

    override fun downloadsPath(name: String): String? = null

    override fun exists(path: String): Boolean = fileManager.fileExistsAtPath(path)

    override fun copy(srcPath: String, dstPath: String): Boolean {
        if (!fileManager.fileExistsAtPath(srcPath)) return false
        // 기존 파일이 있으면 덮어쓰기: 먼저 제거 시도.
        if (fileManager.fileExistsAtPath(dstPath)) {
            fileManager.removeItemAtPath(dstPath, error = null)
        }
        return fileManager.copyItemAtPath(srcPath, toPath = dstPath, error = null)
    }

    private fun firstPath(directory: platform.Foundation.NSSearchPathDirectory): String {
        val paths = NSSearchPathForDirectoriesInDomains(directory, NSUserDomainMask, true)
        return paths.firstOrNull() as? String ?: ""
    }

    private companion object {
        const val AUDIO_SUBDIR = "audio"
        const val AUDIO_EXT = "m4a"
    }
}

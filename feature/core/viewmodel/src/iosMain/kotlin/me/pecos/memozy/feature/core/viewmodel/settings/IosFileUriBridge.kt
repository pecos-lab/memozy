package me.pecos.memozy.feature.core.viewmodel.settings

import kotlinx.cinterop.BetaInteropApi
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import platform.Foundation.NSData
import platform.Foundation.NSString
import platform.Foundation.NSURL
import platform.Foundation.NSUTF8StringEncoding
import platform.Foundation.create
import platform.Foundation.dataUsingEncoding
import platform.Foundation.dataWithContentsOfURL
import platform.Foundation.writeToURL

@OptIn(ExperimentalForeignApi::class, BetaInteropApi::class)
class IosFileUriBridge : FileUriBridge {
    override suspend fun readText(uri: String): String = withContext(Dispatchers.Default) {
        val url = NSURL.URLWithString(uri) ?: error("Invalid URI: $uri")
        val started = url.startAccessingSecurityScopedResource()
        try {
            val data = NSData.dataWithContentsOfURL(url) ?: error("Cannot read $uri")
            NSString.create(data = data, encoding = NSUTF8StringEncoding) ?: ""
        } finally {
            if (started) url.stopAccessingSecurityScopedResource()
        }
    }

    override suspend fun writeText(uri: String, content: String) {
        withContext(Dispatchers.Default) {
            val url = NSURL.URLWithString(uri) ?: error("Invalid URI: $uri")
            val started = url.startAccessingSecurityScopedResource()
            try {
                val nsString = NSString.create(string = content)
                val data = nsString.dataUsingEncoding(NSUTF8StringEncoding)
                    ?: error("UTF-8 encoding failed")
                data.writeToURL(url, atomically = true)
            } finally {
                if (started) url.stopAccessingSecurityScopedResource()
            }
        }
    }
}

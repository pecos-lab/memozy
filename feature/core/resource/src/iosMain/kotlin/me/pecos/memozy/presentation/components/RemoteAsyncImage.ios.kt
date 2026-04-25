package me.pecos.memozy.presentation.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.toComposeImageBitmap
import androidx.compose.ui.layout.ContentScale
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.usePinned
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jetbrains.skia.Image as SkiaImage
import platform.Foundation.NSData
import platform.Foundation.NSURL
import platform.Foundation.dataWithContentsOfURL
import platform.posix.memcpy

/**
 * iOS 구현. NSData.dataWithContentsOfURL (동기) + Skia 디코딩 + Compose ImageBitmap.
 *
 * 동기 fetch 임에도 안전한 이유: 항상 Dispatchers.Default (백그라운드 스레드 풀) 에서만
 * 호출됨. Compose 메인 스레드는 차단되지 않으므로 UI freeze 없음. NSURLSession 의
 * completion-handler 비동기 API 는 K/N Foundation 바인딩에서 시그니처 매칭이 까다로워
 * 일단 동기 + 백그라운드 디스패처 조합 사용. Coil 3 KMP 도입 시 정식 비동기/캐시로 교체.
 *
 * Coil 3 KMP 도입 전 최소 구현 — 캐시/리트라이/플레이스홀더 등 고급 기능 없음.
 * 단순 url → bytes → Skia Image → Compose. 로딩 실패/대기 중 검정 박스.
 */
@Composable
actual fun RemoteAsyncImage(
    url: String,
    contentDescription: String?,
    modifier: Modifier,
    contentScale: ContentScale,
) {
    var bitmap by remember(url) { mutableStateOf<ImageBitmap?>(null) }
    LaunchedEffect(url) {
        bitmap = withContext(Dispatchers.Default) {
            try {
                val nsUrl = NSURL.URLWithString(url) ?: return@withContext null
                val data = NSData.dataWithContentsOfURL(nsUrl) ?: return@withContext null
                val bytes = data.toByteArray()
                // Skia Image 는 native 리소스 → try/finally 로 close 보장.
                // (K/N 의 Image 는 AutoCloseable 미구현이라 use {} 사용 불가)
                val skia = SkiaImage.makeFromEncoded(bytes)
                try {
                    skia.toComposeImageBitmap()
                } finally {
                    skia.close()
                }
            } catch (_: Throwable) {
                null
            }
        }
    }
    val current = bitmap
    if (current != null) {
        Image(
            bitmap = current,
            contentDescription = contentDescription,
            modifier = modifier,
            contentScale = contentScale,
        )
    } else {
        Box(modifier = modifier.background(Color(0xFF222222)))
    }
}

@OptIn(ExperimentalForeignApi::class)
private fun NSData.toByteArray(): ByteArray {
    // NSData.length 는 ULong → Int 캐스팅 시 2GB 초과면 음수. 이미지 맥락에선 거의 없지만 방어.
    val length = this.length.toLong().coerceAtMost(Int.MAX_VALUE.toLong()).toInt()
    val bytes = ByteArray(length)
    if (length > 0) {
        bytes.usePinned { pinned ->
            memcpy(pinned.addressOf(0), this.bytes, length.toULong())
        }
    }
    return bytes
}

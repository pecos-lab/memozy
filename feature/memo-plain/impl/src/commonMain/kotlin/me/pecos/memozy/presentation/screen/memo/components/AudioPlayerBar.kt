package me.pecos.memozy.presentation.screen.memo.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlin.time.Clock
import kotlin.time.ExperimentalTime
import me.pecos.memozy.feature.core.resource.generated.resources.Res
import me.pecos.memozy.feature.core.resource.generated.resources.recording_file
import me.pecos.memozy.feature.core.resource.generated.resources.saved_to_downloads
import me.pecos.memozy.platform.intent.Sharer
import me.pecos.memozy.platform.intent.ToastDuration
import me.pecos.memozy.platform.intent.ToastPresenter
import me.pecos.memozy.platform.media.AudioFileStore
import me.pecos.memozy.platform.media.MediaService
import me.pecos.memozy.presentation.theme.AppColors
import me.pecos.memozy.presentation.theme.LocalFontSettings
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject

@OptIn(ExperimentalTime::class)
@Composable
fun AudioPlayerBar(
    audioPath: String,
    memoTitle: String,
    colors: AppColors,
    onDismiss: () -> Unit
) {
    val fontSettings = LocalFontSettings.current
    val mediaService: MediaService = koinInject()
    val sharer: Sharer = koinInject()
    val toastPresenter: ToastPresenter = koinInject()
    val audioFileStore: AudioFileStore = koinInject()
    val savedToDownloadsText = stringResource(Res.string.saved_to_downloads)
    var isPlaying by remember { mutableStateOf(false) }
    val audioPlayer = remember(audioPath) {
        mediaService.createAudioPlayer(audioPath).apply {
            setOnCompletionListener { isPlaying = false }
        }
    }
    DisposableEffect(audioPlayer) { onDispose { audioPlayer.release() } }

    Spacer(modifier = Modifier.height(12.dp))
    Box {
        Row(
            modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(10.dp))
                .background(colors.chipBackground.copy(alpha = 0.5f))
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier.size(36.dp).clip(CircleShape).background(colors.chipText)
                    .clickable { if (isPlaying) { audioPlayer.pause(); isPlaying = false } else { audioPlayer.start(); isPlaying = true } },
                contentAlignment = Alignment.Center
            ) { Icon(if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp)) }
            Spacer(modifier = Modifier.width(10.dp))
            Text("🎙️ ${memoTitle.ifBlank { stringResource(Res.string.recording_file) }}", fontSize = fontSettings.scaled(14), color = colors.textBody, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f), maxLines = 1)
            val downloadsTarget = audioFileStore.downloadsPath("memozy_${Clock.System.now().toEpochMilliseconds()}.m4a")
            if (downloadsTarget != null) {
                Icon(Icons.Default.Download, contentDescription = null, tint = colors.textSecondary,
                    modifier = Modifier.size(20.dp).clickable {
                        if (audioFileStore.copy(audioPath, downloadsTarget)) {
                            toastPresenter.show("📁 $savedToDownloadsText", ToastDuration.Long)
                        }
                    })
                Spacer(modifier = Modifier.width(12.dp))
            }
            Icon(Icons.Default.Share, contentDescription = null, tint = colors.textSecondary,
                modifier = Modifier.size(20.dp).clickable {
                    sharer.shareFile(audioPath, "audio/mp4")
                })
        }
        Icon(Icons.Default.Close, contentDescription = null, tint = colors.textSecondary,
            modifier = Modifier.size(16.dp).align(Alignment.TopEnd).clickable { onDismiss() })
    }
}

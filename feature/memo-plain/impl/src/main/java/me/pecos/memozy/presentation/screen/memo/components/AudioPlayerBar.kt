package me.pecos.memozy.presentation.screen.memo.components

import android.content.Context
import android.content.Intent
import android.widget.Toast
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import me.pecos.memozy.feature.core.resource.R
import me.pecos.memozy.platform.media.MediaService
import me.pecos.memozy.presentation.theme.AppColors
import me.pecos.memozy.presentation.theme.LocalFontSettings
import org.koin.compose.koinInject

@Composable
fun AudioPlayerBar(
    audioPath: String,
    memoTitle: String,
    colors: AppColors,
    context: Context,
    onDismiss: () -> Unit
) {
    val fontSettings = LocalFontSettings.current
    val mediaService: MediaService = koinInject()
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
            Text("🎙️ ${memoTitle.ifBlank { stringResource(R.string.recording_file) }}", fontSize = fontSettings.scaled(14), color = colors.textBody, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f), maxLines = 1)
            Icon(Icons.Default.Download, contentDescription = null, tint = colors.textSecondary,
                modifier = Modifier.size(20.dp).clickable {
                    val source = java.io.File(audioPath)
                    val dest = android.os.Environment.getExternalStoragePublicDirectory(android.os.Environment.DIRECTORY_DOWNLOADS)
                    val target = java.io.File(dest, "memozy_${System.currentTimeMillis()}.m4a")
                    source.copyTo(target, overwrite = true)
                    Toast.makeText(context, "📁 " + context.getString(R.string.saved_to_downloads), Toast.LENGTH_LONG).show()
                })
            Spacer(modifier = Modifier.width(12.dp))
            Icon(Icons.Default.Share, contentDescription = null, tint = colors.textSecondary,
                modifier = Modifier.size(20.dp).clickable {
                    val uri = androidx.core.content.FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", java.io.File(audioPath))
                    val shareIntent = Intent(Intent.ACTION_SEND).apply { type = "audio/mp4"; putExtra(Intent.EXTRA_STREAM, uri); addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION) }
                    context.startActivity(Intent.createChooser(shareIntent, null))
                })
        }
        Icon(Icons.Default.Close, contentDescription = null, tint = colors.textSecondary,
            modifier = Modifier.size(16.dp).align(Alignment.TopEnd).clickable { onDismiss() })
    }
}

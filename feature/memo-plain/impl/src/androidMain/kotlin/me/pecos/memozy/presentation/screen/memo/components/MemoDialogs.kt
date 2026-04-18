package me.pecos.memozy.presentation.screen.memo.components

import android.content.ClipboardManager as AndroidClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.SmartDisplay
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import me.pecos.memozy.feature.core.resource.R
import me.pecos.memozy.presentation.components.AppPopup
import me.pecos.memozy.presentation.components.PopupActionArea
import me.pecos.memozy.presentation.components.PopupNavigation
import me.pecos.memozy.presentation.components.PopupSize
import me.pecos.memozy.presentation.theme.AppColors
import me.pecos.memozy.presentation.theme.LocalFontSettings

private val YOUTUBE_URL_REGEX = Regex(
    """(?:https?://)?(?:www\.)?(?:youtube\.com/watch\?v=|youtu\.be/|youtube\.com/shorts/)[\w\-]+(?:[&?][\w\-=]*)*"""
)

private val WEB_URL_REGEX = Regex(
    """https?://\S+"""
)

private fun getSystemClipboardText(context: Context): String {
    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as? AndroidClipboardManager
    return clipboard?.primaryClip?.getItemAt(0)?.text?.toString() ?: ""
}

@Composable
fun YouTubeUrlDialog(
    colors: AppColors,
    clipboardManager: ClipboardManager,
    context: Context,
    onDismiss: () -> Unit,
    onUrlAdded: (String) -> Unit
) {
    val fontSettings = LocalFontSettings.current
    var urlInput by remember { mutableStateOf("") }
    // 다이얼로그가 열릴 때마다 최신 클립보드를 읽도록 key 없이 매번 실행
    val clipText = getSystemClipboardText(context)
    val isValid = YOUTUBE_URL_REGEX.containsMatchIn(urlInput)

    AppPopup(
        onDismissRequest = onDismiss,
        title = stringResource(R.string.youtube_summary_title),
        navigation = PopupNavigation.EMPHASIZED,
        size = PopupSize.MEDIUM,
        actionArea = PopupActionArea.NEUTRAL,
        primaryButtonText = stringResource(R.string.confirm),
        onPrimaryClick = if (isValid) {
            { YOUTUBE_URL_REGEX.find(urlInput)?.value?.let { onUrlAdded(it) } }
        } else null,
        secondaryButtonText = stringResource(R.string.cancel),
        onSecondaryClick = onDismiss
    ) {
        Text(stringResource(R.string.youtube_summary_desc), fontSize = fontSettings.scaled(14), color = colors.textSecondary)
        Spacer(modifier = Modifier.height(12.dp))
        OutlinedTextField(
            value = urlInput, onValueChange = { urlInput = it },
            placeholder = { Text("https://youtu.be/...", fontSize = fontSettings.scaled(14)) },
            singleLine = true, modifier = Modifier.fillMaxWidth()
        )
        if (clipText.isNotBlank() && urlInput.isBlank() && YOUTUBE_URL_REGEX.containsMatchIn(clipText)) {
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFF2196F3).copy(alpha = 0.08f))
                    .clickable { urlInput = YOUTUBE_URL_REGEX.find(clipText)?.value ?: clipText }
                    .padding(horizontal = 10.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.ContentPaste, contentDescription = null,
                    tint = Color(0xFF2196F3), modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = YOUTUBE_URL_REGEX.find(clipText)?.value ?: clipText,
                    fontSize = fontSettings.scaled(12),
                    color = Color(0xFF2196F3), fontWeight = FontWeight.Medium,
                    maxLines = 1, overflow = TextOverflow.Ellipsis
                )
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .clickable {
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.youtube.com")).apply {
                        setPackage("com.google.android.youtube")
                    }
                    try { context.startActivity(intent) } catch (_: Exception) {
                        context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://www.youtube.com")))
                    }
                }
                .padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Default.SmartDisplay, contentDescription = null,
                tint = Color(0xFFFF0000), modifier = Modifier.size(14.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = stringResource(R.string.youtube_open_to_copy),
                fontSize = fontSettings.scaled(12),
                color = Color(0xFFFF0000), fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
fun WebUrlDialog(
    clipboardManager: ClipboardManager,
    onDismiss: () -> Unit,
    onUrlConfirmed: (String) -> Unit
) {
    val context = LocalContext.current
    val fontSettings = LocalFontSettings.current
    var webUrlInput by remember { mutableStateOf("") }
    val clipText = getSystemClipboardText(context)
    val colors = me.pecos.memozy.presentation.theme.LocalAppColors.current
    val isValid = webUrlInput.startsWith("http")

    AppPopup(
        onDismissRequest = onDismiss,
        title = stringResource(R.string.web_summary_title),
        navigation = PopupNavigation.EMPHASIZED,
        size = PopupSize.MEDIUM,
        actionArea = PopupActionArea.NEUTRAL,
        primaryButtonText = stringResource(R.string.confirm),
        onPrimaryClick = if (isValid) {
            { onUrlConfirmed(webUrlInput.trim()) }
        } else null,
        secondaryButtonText = stringResource(R.string.cancel),
        onSecondaryClick = onDismiss
    ) {
        Text(stringResource(R.string.web_summary_desc), fontSize = fontSettings.scaled(14), color = colors.textSecondary)
        Spacer(modifier = Modifier.height(12.dp))
        OutlinedTextField(
            value = webUrlInput, onValueChange = { webUrlInput = it },
            placeholder = { Text("https://...", fontSize = fontSettings.scaled(14)) },
            singleLine = true, modifier = Modifier.fillMaxWidth()
        )
        val webClipUrl = remember(clipText) { WEB_URL_REGEX.find(clipText)?.value }
        val isWebUrl = webClipUrl != null && !YOUTUBE_URL_REGEX.containsMatchIn(webClipUrl)
        if (webUrlInput.isBlank() && isWebUrl) {
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFF2196F3).copy(alpha = 0.08f))
                    .clickable { webUrlInput = webClipUrl }
                    .padding(horizontal = 10.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.ContentPaste, contentDescription = null,
                    tint = Color(0xFF2196F3), modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = webClipUrl, fontSize = fontSettings.scaled(12),
                    color = Color(0xFF2196F3), fontWeight = FontWeight.Medium,
                    maxLines = 1, overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WebLinkBottomSheet(
    url: String,
    clipboardManager: ClipboardManager,
    context: Context,
    onDismiss: () -> Unit
) {
    val colors = me.pecos.memozy.presentation.theme.LocalAppColors.current
    val fontSettings = LocalFontSettings.current
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(),
        containerColor = colors.cardBackground
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 8.dp).padding(bottom = 24.dp)
        ) {
            Text(stringResource(R.string.web_link), fontSize = fontSettings.scaled(18), fontWeight = FontWeight.Bold, color = colors.textTitle)
            Spacer(modifier = Modifier.height(4.dp))
            Text(url, fontSize = fontSettings.scaled(13), color = colors.textSecondary, maxLines = 2)
            Spacer(modifier = Modifier.height(20.dp))

            Row(
                modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp))
                    .clickable { clipboardManager.setText(AnnotatedString(url)); onDismiss() }
                    .padding(vertical = 14.dp, horizontal = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("📋", fontSize = fontSettings.scaled(20)); Spacer(modifier = Modifier.width(12.dp))
                Text(stringResource(R.string.copy_link), fontSize = fontSettings.scaled(16), color = colors.textBody)
            }

            Row(
                modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp))
                    .clickable {
                        val fullUrl = if (url.startsWith("http")) url else "https://$url"
                        context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(fullUrl)))
                        onDismiss()
                    }
                    .padding(vertical = 14.dp, horizontal = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("🌐", fontSize = fontSettings.scaled(20)); Spacer(modifier = Modifier.width(12.dp))
                Text(stringResource(R.string.open_in_browser), fontSize = fontSettings.scaled(16), color = colors.textBody)
            }
        }
    }
}

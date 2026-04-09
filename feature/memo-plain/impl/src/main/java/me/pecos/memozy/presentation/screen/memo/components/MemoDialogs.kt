package me.pecos.memozy.presentation.screen.memo.components

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import me.pecos.memozy.feature.core.resource.R
import me.pecos.memozy.presentation.theme.AppColors

private val YOUTUBE_URL_REGEX = Regex(
    """(?:https?://)?(?:www\.)?(?:youtube\.com/watch\?v=|youtu\.be/|youtube\.com/shorts/)[\w\-]+(?:[&?][\w\-=]*)*"""
)

@Composable
fun YouTubeUrlDialog(
    colors: AppColors,
    clipboardManager: ClipboardManager,
    onDismiss: () -> Unit,
    onUrlAdded: (String) -> Unit
) {
    var urlInput by remember { mutableStateOf("") }
    val clipText = clipboardManager.getText()?.text ?: ""

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.youtube_summary_title), fontWeight = FontWeight.Bold) },
        text = {
            Column {
                Text(stringResource(R.string.youtube_summary_desc), fontSize = 14.sp, color = colors.textSecondary)
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = urlInput, onValueChange = { urlInput = it },
                    placeholder = { Text("https://youtu.be/...", fontSize = 14.sp) },
                    singleLine = true, modifier = Modifier.fillMaxWidth()
                )
                if (clipText.isNotBlank() && urlInput.isBlank()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "📋 ${stringResource(R.string.paste_clipboard)}: $clipText",
                        fontSize = 13.sp, color = Color(0xFF2196F3), fontWeight = FontWeight.SemiBold,
                        maxLines = 1, overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(6.dp))
                            .clickable { urlInput = clipText }.padding(vertical = 4.dp)
                    )
                }
            }
        },
        confirmButton = {
            val isValid = YOUTUBE_URL_REGEX.containsMatchIn(urlInput)
            TextButton(
                onClick = {
                    val url = YOUTUBE_URL_REGEX.find(urlInput)?.value ?: return@TextButton
                    onUrlAdded(url)
                },
                enabled = isValid
            ) { Text(stringResource(R.string.confirm), color = if (isValid) Color(0xFF2196F3) else colors.textSecondary) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.cancel), color = colors.textSecondary) }
        }
    )
}

@Composable
fun WebUrlDialog(
    clipboardManager: ClipboardManager,
    onDismiss: () -> Unit,
    onUrlConfirmed: (String) -> Unit
) {
    var webUrlInput by remember { mutableStateOf("") }
    val clipText = clipboardManager.getText()?.text ?: ""
    val colors = me.pecos.memozy.presentation.theme.LocalAppColors.current

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.web_summary_title), fontWeight = FontWeight.Bold) },
        text = {
            Column {
                Text(stringResource(R.string.web_summary_desc), fontSize = 14.sp, color = colors.textSecondary)
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = webUrlInput, onValueChange = { webUrlInput = it },
                    placeholder = { Text("https://...", fontSize = 14.sp) },
                    singleLine = true, modifier = Modifier.fillMaxWidth()
                )
                if (clipText.isNotBlank() && webUrlInput.isBlank()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "📋 ${stringResource(R.string.paste_clipboard)}: $clipText",
                        fontSize = 13.sp, color = Color(0xFF2196F3), fontWeight = FontWeight.SemiBold,
                        maxLines = 1, overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(6.dp))
                            .clickable { webUrlInput = clipText }.padding(vertical = 4.dp)
                    )
                }
            }
        },
        confirmButton = {
            val isValid = webUrlInput.startsWith("http")
            TextButton(
                onClick = { if (isValid) onUrlConfirmed(webUrlInput.trim()) },
                enabled = isValid
            ) { Text(stringResource(R.string.confirm), color = if (isValid) Color(0xFF2196F3) else colors.textSecondary) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.cancel), color = colors.textSecondary) }
        }
    )
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
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(),
        containerColor = colors.cardBackground
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 8.dp).padding(bottom = 24.dp)
        ) {
            Text(stringResource(R.string.web_link), fontSize = 18.sp, fontWeight = FontWeight.Bold, color = colors.textTitle)
            Spacer(modifier = Modifier.height(4.dp))
            Text(url, fontSize = 13.sp, color = colors.textSecondary, maxLines = 2)
            Spacer(modifier = Modifier.height(20.dp))

            Row(
                modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp))
                    .clickable { clipboardManager.setText(AnnotatedString(url)); onDismiss() }
                    .padding(vertical = 14.dp, horizontal = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("📋", fontSize = 20.sp); Spacer(modifier = Modifier.width(12.dp))
                Text(stringResource(R.string.copy_link), fontSize = 16.sp, color = colors.textBody)
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
                Text("🌐", fontSize = 20.sp); Spacer(modifier = Modifier.width(12.dp))
                Text(stringResource(R.string.open_in_browser), fontSize = 16.sp, color = colors.textBody)
            }
        }
    }
}

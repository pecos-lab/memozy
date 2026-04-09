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
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.ClipboardManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import me.pecos.memozy.feature.core.resource.R
import me.pecos.memozy.presentation.screen.memo.SummaryMode
import me.pecos.memozy.presentation.theme.AppColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun YouTubeLinkBottomSheet(
    url: String,
    colors: AppColors,
    context: Context,
    clipboardManager: ClipboardManager,
    isSummarizing: Boolean,
    isWebSummarizing: Boolean,
    summaryResult: String?,
    onSummarize: ((String, SummaryMode) -> Unit)?,
    onDismiss: () -> Unit,
    onSummarizeAndDismiss: (String, SummaryMode) -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(),
        containerColor = colors.cardBackground
    ) {
        Column(
            modifier = Modifier.fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 8.dp)
                .padding(bottom = 24.dp)
        ) {
            Text("YouTube 링크", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = colors.textTitle)
            Spacer(modifier = Modifier.height(4.dp))
            Text(url, fontSize = 13.sp, color = colors.textSecondary, maxLines = 1)
            Spacer(modifier = Modifier.height(20.dp))

            // 📋 링크 복사
            Row(
                modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp))
                    .clickable { clipboardManager.setText(AnnotatedString(url)); onDismiss() }
                    .padding(vertical = 14.dp, horizontal = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("📋", fontSize = 20.sp); Spacer(modifier = Modifier.width(12.dp))
                Text(stringResource(R.string.copy_link), fontSize = 16.sp, color = colors.textBody)
            }

            // 🌐 브라우저에서 열기
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

            // ▶ YouTube에서 열기
            Row(
                modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp))
                    .clickable {
                        val fullUrl = if (url.startsWith("http")) url else "https://$url"
                        val ytIntent = Intent(Intent.ACTION_VIEW, Uri.parse(fullUrl)).apply {
                            setPackage("com.google.android.youtube")
                        }
                        try { context.startActivity(ytIntent) } catch (_: Exception) {
                            context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(fullUrl)))
                        }
                        onDismiss()
                    }
                    .padding(vertical = 14.dp, horizontal = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("▶️", fontSize = 20.sp); Spacer(modifier = Modifier.width(12.dp))
                Text(stringResource(R.string.open_in_youtube), fontSize = 16.sp, color = colors.textBody)
            }

            // 🤖 AI 요약하기
            if (onSummarize != null && !isSummarizing && summaryResult == null) {
                Row(
                    modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp))
                        .clickable { onSummarizeAndDismiss(url, SummaryMode.SIMPLE) }
                        .padding(vertical = 14.dp, horizontal = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("⚡", fontSize = 20.sp); Spacer(modifier = Modifier.width(12.dp))
                    Text(stringResource(R.string.summary_mode_simple), fontSize = 16.sp, color = colors.textBody)
                }
                Row(
                    modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp))
                        .clickable { onSummarizeAndDismiss(url, SummaryMode.DETAILED) }
                        .padding(vertical = 14.dp, horizontal = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("📑", fontSize = 20.sp); Spacer(modifier = Modifier.width(12.dp))
                    Text(stringResource(R.string.summary_mode_detailed), fontSize = 16.sp, color = colors.textBody)
                }
            }
        }
    }
}

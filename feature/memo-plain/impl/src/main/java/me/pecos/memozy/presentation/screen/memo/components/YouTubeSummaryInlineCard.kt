package me.pecos.memozy.presentation.screen.memo.components

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import me.pecos.memozy.presentation.screen.memo.SummaryMode
import me.pecos.memozy.presentation.theme.AppColors

@Composable
fun YouTubeSummaryInlineCard(
    youtubeUrl: String,
    videoId: String?,
    title: String?,
    memoTitle: String,
    isExpanded: Boolean,
    onExpandToggle: (Boolean) -> Unit,
    summaryText: String?,
    isSummarizing: Boolean,
    currentSummaryMode: SummaryMode,
    onSummarize: ((String, SummaryMode) -> Unit)?,
    onCancelSummarize: (() -> Unit)?,
    onResummarize: (SummaryMode) -> Unit,
    colors: AppColors,
    context: Context,
    clipboardManager: ClipboardManager
) {
    // 접힌 상태
    if (!isExpanded) {
        Row(
            modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp))
                .background(colors.cardBackground).clickable { onExpandToggle(true) }.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("▶ ${stringResource(R.string.summary_expand)}", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = colors.chipText)
            Spacer(modifier = Modifier.weight(1f))
            Spacer(modifier = Modifier.width(12.dp))
            Text(title ?: "YouTube", fontSize = 12.sp, color = colors.textSecondary, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
        Spacer(modifier = Modifier.height(12.dp))
    }

    // 펼친 상태
    if (isExpanded) {
        Column(
            modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(colors.cardBackground)
        ) {
            // 썸네일
            if (videoId != null) {
                coil.compose.AsyncImage(
                    model = "https://img.youtube.com/vi/$videoId/hqdefault.jpg",
                    contentDescription = null,
                    modifier = Modifier.fillMaxWidth().aspectRatio(16f / 9f)
                        .clip(RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp)),
                    contentScale = androidx.compose.ui.layout.ContentScale.Crop
                )
            }

            Column(modifier = Modifier.padding(12.dp)) {
                Text("▶ YouTube", fontSize = 11.sp, color = colors.textSecondary, fontWeight = FontWeight.Medium)
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = title ?: memoTitle.takeIf { it.isNotBlank() } ?: youtubeUrl,
                    fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = colors.textTitle,
                    maxLines = 2, overflow = TextOverflow.Ellipsis
                )
                if (youtubeUrl.isNotBlank()) {
                    Text(youtubeUrl, fontSize = 11.sp, color = Color(0xFF2196F3), maxLines = 1, overflow = TextOverflow.Ellipsis)
                }

                // 액션 버튼
                Spacer(modifier = Modifier.height(10.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    // 원본 보기
                    Row(
                        modifier = Modifier.clip(RoundedCornerShape(8.dp)).background(colors.chipBackground)
                            .clickable {
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(youtubeUrl)).apply { setPackage("com.google.android.youtube") }
                                try { context.startActivity(intent) } catch (_: Exception) {
                                    context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(youtubeUrl)))
                                }
                            }
                            .padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("🔗", fontSize = 12.sp); Spacer(modifier = Modifier.width(4.dp))
                        Text(stringResource(R.string.summary_card_open), fontSize = 11.sp, color = colors.chipText, fontWeight = FontWeight.Medium)
                    }
                    // 복사
                    Row(
                        modifier = Modifier.clip(RoundedCornerShape(8.dp)).background(colors.chipBackground)
                            .clickable {
                                clipboardManager.setText(AnnotatedString(youtubeUrl))
                                Toast.makeText(context, context.getString(R.string.youtube_url_copied), Toast.LENGTH_SHORT).show()
                            }
                            .padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.ContentCopy, null, tint = colors.chipText, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(stringResource(R.string.memo_copy), fontSize = 11.sp, color = colors.chipText, fontWeight = FontWeight.Medium)
                    }
                    // 요약 버튼 (아직 요약 안 됨)
                    if (onSummarize != null && summaryText == null) {
                        Row(
                            modifier = Modifier.clip(RoundedCornerShape(8.dp)).background(Color(0xFF2196F3).copy(alpha = 0.1f))
                                .clickable { onSummarize(youtubeUrl, SummaryMode.SIMPLE) }
                                .padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) { Text(stringResource(R.string.summary_mode_simple), fontSize = 11.sp, color = Color(0xFF2196F3), fontWeight = FontWeight.Medium) }
                        Row(
                            modifier = Modifier.clip(RoundedCornerShape(8.dp)).background(Color(0xFFFF9800).copy(alpha = 0.1f))
                                .clickable { onSummarize(youtubeUrl, SummaryMode.DETAILED) }
                                .padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) { Text(stringResource(R.string.summary_mode_detailed), fontSize = 11.sp, color = Color(0xFFFF9800), fontWeight = FontWeight.Medium) }
                    }
                    // 요약 완료 후 반대 모드
                    if (onSummarize != null && summaryText != null) {
                        val altMode = if (currentSummaryMode == SummaryMode.DETAILED) SummaryMode.SIMPLE else SummaryMode.DETAILED
                        val altLabel = if (altMode == SummaryMode.SIMPLE) stringResource(R.string.summary_mode_simple) else stringResource(R.string.summary_mode_detailed)
                        val altColor = if (altMode == SummaryMode.SIMPLE) Color(0xFF2196F3) else Color(0xFFFF9800)
                        Row(
                            modifier = Modifier.clip(RoundedCornerShape(8.dp)).background(altColor.copy(alpha = 0.1f))
                                .clickable { onResummarize(altMode) }
                                .padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) { Text(altLabel, fontSize = 11.sp, color = altColor, fontWeight = FontWeight.Medium) }
                    }
                }

                // 로딩
                if (isSummarizing) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        CircularProgressIndicator(modifier = Modifier.size(14.dp), strokeWidth = 2.dp, color = colors.textSecondary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(stringResource(R.string.youtube_summarizing), fontSize = 13.sp, color = colors.textSecondary, modifier = Modifier.weight(1f))
                        Icon(Icons.Default.Close, contentDescription = null, tint = colors.textSecondary,
                            modifier = Modifier.size(16.dp).clickable { onCancelSummarize?.invoke() })
                    }
                }

                // 요약 텍스트
                if (summaryText != null) {
                    Spacer(modifier = Modifier.height(10.dp))
                    HorizontalDivider(color = colors.cardBorder)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(summaryText, fontSize = 14.sp, lineHeight = 22.sp, color = colors.textBody)
                }

                // 접기 버튼
                if (summaryText != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth().clickable { onExpandToggle(false) },
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text("▼ ${stringResource(R.string.summary_collapse)}", fontSize = 12.sp, color = colors.textSecondary)
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(12.dp))
    }
}

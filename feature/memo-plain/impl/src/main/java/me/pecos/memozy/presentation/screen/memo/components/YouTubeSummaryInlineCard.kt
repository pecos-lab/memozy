package me.pecos.memozy.presentation.screen.memo.components

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.AutoFixHigh
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
import me.pecos.memozy.presentation.theme.LocalFontSettings

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
    currentSummaryMode: SummaryMode?,
    onSummarize: ((String, SummaryMode) -> Unit)?,
    onCancelSummarize: (() -> Unit)?,
    onResummarize: (SummaryMode) -> Unit,
    onDeleteSummary: (() -> Unit)? = null,
    onAskAi: (() -> Unit)? = null,
    colors: AppColors,
    context: Context,
    clipboardManager: ClipboardManager,
    onStyleSelect: (() -> Unit)? = null
) {
    val fontSettings = LocalFontSettings.current

    // 접힌 상태
    if (!isExpanded) {
        Row(
            modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp))
                .background(colors.cardBackground).clickable { onExpandToggle(true) }.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("▶ ${stringResource(R.string.summary_expand)}", fontSize = fontSettings.scaled(13), fontWeight = FontWeight.SemiBold, color = colors.chipText)
            Spacer(modifier = Modifier.weight(1f))
            Spacer(modifier = Modifier.width(12.dp))
            Text(title ?: "YouTube", fontSize = fontSettings.scaled(12), color = colors.textSecondary, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
        Spacer(modifier = Modifier.height(12.dp))
    }

    // 펼친 상태
    if (isExpanded) {
        Box(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(colors.cardBackground)
        ) {
            // 썸네일
            if (videoId != null) {
                coil.compose.AsyncImage(
                    model = "https://img.youtube.com/vi/$videoId/hqdefault.jpg",
                    contentDescription = null,
                    modifier = Modifier.fillMaxWidth().padding(16.dp).aspectRatio(16f / 9f)
                        .clip(RoundedCornerShape(12.dp)),
                    contentScale = androidx.compose.ui.layout.ContentScale.Crop
                )
            }

            Column(modifier = Modifier.padding(16.dp)) {
                Text("▶ YouTube", fontSize = fontSettings.scaled(11), color = colors.textSecondary, fontWeight = FontWeight.Medium)
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = title ?: memoTitle.takeIf { it.isNotBlank() } ?: youtubeUrl,
                    fontSize = fontSettings.scaled(14), fontWeight = FontWeight.SemiBold, color = colors.textTitle,
                    maxLines = 2, overflow = TextOverflow.Ellipsis
                )
                if (youtubeUrl.isNotBlank()) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(6.dp))
                            .background(colors.chipBackground.copy(alpha = 0.5f))
                            .padding(horizontal = 6.dp, vertical = 3.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Link, contentDescription = null,
                            tint = Color(0xFF2196F3),
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(youtubeUrl, fontSize = fontSettings.scaled(10), color = Color(0xFF2196F3), maxLines = 1, overflow = TextOverflow.Ellipsis)
                    }
                }

                // 액션 버튼 — 원본 보기 + 복사 + 요약 양식
                Spacer(modifier = Modifier.height(10.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Row(
                        modifier = Modifier.weight(1f).clip(RoundedCornerShape(6.dp)).background(colors.chipBackground)
                            .clickable {
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(youtubeUrl)).apply { setPackage("com.google.android.youtube") }
                                try { context.startActivity(intent) } catch (_: Exception) {
                                    context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(youtubeUrl)))
                                }
                            }
                            .padding(vertical = 5.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text("🔗", fontSize = fontSettings.scaled(10)); Spacer(modifier = Modifier.width(3.dp))
                        Text(stringResource(R.string.summary_card_open), fontSize = fontSettings.scaled(10), color = colors.chipText, fontWeight = FontWeight.Medium)
                    }
                    Row(
                        modifier = Modifier.weight(1f).clip(RoundedCornerShape(6.dp)).background(colors.chipBackground)
                            .clickable {
                                clipboardManager.setText(AnnotatedString(youtubeUrl))
                                Toast.makeText(context, context.getString(R.string.youtube_url_copied), Toast.LENGTH_SHORT).show()
                            }
                            .padding(vertical = 5.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(Icons.Default.ContentCopy, null, tint = colors.chipText, modifier = Modifier.size(12.dp))
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(stringResource(R.string.memo_copy), fontSize = fontSettings.scaled(10), color = colors.chipText, fontWeight = FontWeight.Medium)
                    }
                    if (onSummarize != null && summaryText == null && onStyleSelect != null) {
                        Row(
                            modifier = Modifier.weight(1f).clip(RoundedCornerShape(6.dp)).background(Color(0xFF2196F3).copy(alpha = 0.1f))
                                .clickable { onStyleSelect() }
                                .padding(vertical = 5.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Text(stringResource(R.string.summary_style_select), fontSize = fontSettings.scaled(10), color = Color(0xFF2196F3), fontWeight = FontWeight.Medium)
                        }
                    }
                }

                // 로딩
                if (isSummarizing) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        CircularProgressIndicator(modifier = Modifier.size(14.dp), strokeWidth = 2.dp, color = colors.textSecondary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(stringResource(R.string.youtube_summarizing), fontSize = fontSettings.scaled(13), color = colors.textSecondary, modifier = Modifier.weight(1f))
                        Icon(Icons.Default.Close, contentDescription = null, tint = colors.textSecondary,
                            modifier = Modifier.size(16.dp).clickable { onCancelSummarize?.invoke() })
                    }
                }

                // 요약 텍스트
                if (summaryText != null) {
                    Spacer(modifier = Modifier.height(10.dp))
                    HorizontalDivider(color = colors.cardBorder)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(summaryText, fontSize = fontSettings.scaled(14), lineHeight = 22.sp, color = colors.textBody)

                    // 요약 내용 복사 + AI 물어보기 버튼
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(6.dp))
                                .background(colors.chipBackground)
                                .clickable {
                                    clipboardManager.setText(AnnotatedString(summaryText))
                                    Toast.makeText(context, context.getString(R.string.memo_copy_done), Toast.LENGTH_SHORT).show()
                                }
                                .padding(vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(Icons.Default.ContentCopy, null, tint = colors.chipText, modifier = Modifier.size(12.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(stringResource(R.string.summary_copy), fontSize = fontSettings.scaled(10), color = colors.chipText, fontWeight = FontWeight.Medium)
                        }
                        if (onAskAi != null) {
                            Row(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(Color(0xFF7C4DFF).copy(alpha = 0.1f))
                                    .clickable { onAskAi() }
                                    .padding(vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Icon(Icons.Default.AutoFixHigh, null, tint = Color(0xFF7C4DFF), modifier = Modifier.size(12.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Memozy AI", fontSize = fontSettings.scaled(10), color = Color(0xFF7C4DFF), fontWeight = FontWeight.Medium)
                            }
                        }
                    }
                }

                // 접기 버튼
                if (summaryText != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth().clickable { onExpandToggle(false) },
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text("▼ ${stringResource(R.string.summary_collapse)}", fontSize = fontSettings.scaled(12), color = colors.textSecondary)
                    }
                }
            }
        }
        // X 삭제 버튼 (요약 있을 때만)
        if (summaryText != null && onDeleteSummary != null) {
            Icon(
                Icons.Default.Close,
                contentDescription = null,
                tint = colors.textSecondary,
                modifier = Modifier
                    .size(28.dp)
                    .align(Alignment.TopEnd)
                    .padding(6.dp)
                    .clickable { onDeleteSummary() }
            )
        }
        }
        Spacer(modifier = Modifier.height(12.dp))
    }
}

package me.pecos.memozy.presentation.screen.memo.components

import android.content.ClipData
import android.content.ClipboardManager
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
import androidx.compose.material.icons.filled.OpenInBrowser
import androidx.compose.material.icons.filled.Summarize
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import me.pecos.memozy.feature.core.resource.R
import me.pecos.memozy.presentation.screen.memo.SummaryMode
import me.pecos.memozy.presentation.theme.LocalAppColors

enum class SummarySourceType { YOUTUBE, WEB, TWITTER }

@Composable
fun SummaryCard(
    sourceType: SummarySourceType,
    url: String,
    title: String?,
    imageUrl: String? = null,
    hasSummary: Boolean = false,
    summaryMode: SummaryMode? = null,
    onViewSummary: (() -> Unit)? = null,
    onSummarize: ((SummaryMode) -> Unit)? = null,
    onDismiss: () -> Unit
) {
    val colors = LocalAppColors.current
    val context = LocalContext.current

    Box {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(colors.cardBackground)
        ) {
            // 프리뷰 이미지
            if (imageUrl != null) {
                AsyncImage(
                    model = imageUrl,
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(16f / 9f)
                        .clip(RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp)),
                    contentScale = ContentScale.Crop
                )
            }

            Column(modifier = Modifier.padding(12.dp)) {
                // 소스 태그
                val sourceEmoji = when (sourceType) {
                    SummarySourceType.YOUTUBE -> "▶"
                    SummarySourceType.WEB -> "🔗"
                    SummarySourceType.TWITTER -> "𝕏"
                }
                val sourceLabel = when (sourceType) {
                    SummarySourceType.YOUTUBE -> "YouTube"
                    SummarySourceType.WEB -> stringResource(R.string.summary_card_web)
                    SummarySourceType.TWITTER -> "X / Twitter"
                }
                Text(
                    text = "$sourceEmoji $sourceLabel",
                    fontSize = 11.sp,
                    color = colors.textSecondary,
                    fontWeight = FontWeight.Medium
                )

                Spacer(modifier = Modifier.height(4.dp))

                // 제목
                Text(
                    text = title ?: url,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = colors.textTitle,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                if (title != null) {
                    Text(
                        text = url,
                        fontSize = 11.sp,
                        color = Color(0xFF2196F3),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // 액션 버튼 row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // 원본 보기
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(colors.chipBackground)
                            .clickable {
                                val intent = when (sourceType) {
                                    SummarySourceType.YOUTUBE -> {
                                        Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
                                            setPackage("com.google.android.youtube")
                                        }
                                    }
                                    else -> Intent(Intent.ACTION_VIEW, Uri.parse(url))
                                }
                                try {
                                    context.startActivity(intent)
                                } catch (_: Exception) {
                                    context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
                                }
                            }
                            .padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.OpenInBrowser, null, tint = colors.chipText, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(stringResource(R.string.summary_card_open), fontSize = 11.sp, color = colors.chipText, fontWeight = FontWeight.Medium)
                    }

                    // URL 복사
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(colors.chipBackground)
                            .clickable {
                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                clipboard.setPrimaryClip(ClipData.newPlainText("url", url))
                                val toastMsg = when (sourceType) {
                                    SummarySourceType.YOUTUBE -> context.getString(R.string.youtube_url_copied)
                                    SummarySourceType.WEB -> context.getString(R.string.web_url_copied)
                                    else -> context.getString(R.string.memo_copied)
                                }
                                Toast.makeText(context, toastMsg, Toast.LENGTH_SHORT).show()
                            }
                            .padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.ContentCopy, null, tint = colors.chipText, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(stringResource(R.string.memo_copy), fontSize = 11.sp, color = colors.chipText, fontWeight = FontWeight.Medium)
                    }

                    if (hasSummary && onSummarize != null) {
                        // 요약 완료 → 반대 모드 버튼
                        val altMode = if (summaryMode == SummaryMode.DETAILED) SummaryMode.SIMPLE else SummaryMode.DETAILED
                        val altLabel = if (altMode == SummaryMode.SIMPLE) stringResource(R.string.summary_mode_simple) else stringResource(R.string.summary_mode_detailed)
                        val altColor = if (altMode == SummaryMode.SIMPLE) Color(0xFF2196F3) else Color(0xFFFF9800)
                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(altColor.copy(alpha = 0.1f))
                                .clickable { onSummarize(altMode) }
                                .padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Summarize, null, tint = altColor, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(altLabel, fontSize = 11.sp, color = altColor, fontWeight = FontWeight.Medium)
                        }
                    } else if (!hasSummary && onSummarize != null) {
                        // 아직 요약 안 됨 → 두 모드 모두 표시
                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFF2196F3).copy(alpha = 0.1f))
                                .clickable { onSummarize(SummaryMode.SIMPLE) }
                                .padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Summarize, null, tint = Color(0xFF2196F3), modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(stringResource(R.string.summary_mode_simple), fontSize = 11.sp, color = Color(0xFF2196F3), fontWeight = FontWeight.Medium)
                        }
                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFFFF9800).copy(alpha = 0.1f))
                                .clickable { onSummarize(SummaryMode.DETAILED) }
                                .padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Summarize, null, tint = Color(0xFFFF9800), modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(stringResource(R.string.summary_mode_detailed), fontSize = 11.sp, color = Color(0xFFFF9800), fontWeight = FontWeight.Medium)
                        }
                    }
                }
            }
        }

        // 닫기 버튼
        Icon(
            Icons.Default.Close, null,
            tint = colors.textSecondary,
            modifier = Modifier
                .size(20.dp)
                .align(Alignment.TopEnd)
                .padding(4.dp)
                .clickable { onDismiss() }
        )
    }
}

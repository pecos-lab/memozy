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
import me.pecos.memozy.presentation.theme.LocalAppColors

enum class SummarySourceType { YOUTUBE, WEB, TWITTER }

@Composable
fun SummaryCard(
    sourceType: SummarySourceType,
    url: String,
    title: String?,
    imageUrl: String? = null,
    hasSummary: Boolean = false,
    onViewSummary: (() -> Unit)? = null,
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

                    // 요약 보기
                    if (onViewSummary != null) {
                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(
                                    if (hasSummary) colors.chipBackground
                                    else Color(0xFF2196F3).copy(alpha = 0.1f)
                                )
                                .clickable { onViewSummary() }
                                .padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Summarize, null,
                                tint = if (hasSummary) colors.chipText else Color(0xFF2196F3),
                                modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (hasSummary) stringResource(R.string.summary_card_view_summary) else stringResource(R.string.summary_card_summarize),
                                fontSize = 11.sp,
                                color = if (hasSummary) colors.chipText else Color(0xFF2196F3),
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }

                    // URL 복사
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(colors.chipBackground)
                            .clickable {
                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                clipboard.setPrimaryClip(ClipData.newPlainText("url", url))
                                Toast.makeText(context, context.getString(R.string.memo_copied), Toast.LENGTH_SHORT).show()
                            }
                            .padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.ContentCopy, null, tint = colors.chipText, modifier = Modifier.size(14.dp))
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

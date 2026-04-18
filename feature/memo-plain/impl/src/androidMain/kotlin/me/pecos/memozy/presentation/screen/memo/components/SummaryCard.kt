package me.pecos.memozy.presentation.screen.memo.components

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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import me.pecos.memozy.feature.core.resource.R
import me.pecos.memozy.platform.intent.ClipboardService
import me.pecos.memozy.platform.intent.ToastPresenter
import me.pecos.memozy.platform.intent.UrlLauncher
import me.pecos.memozy.presentation.screen.memo.SummaryMode
import me.pecos.memozy.presentation.theme.LocalAppColors
import me.pecos.memozy.presentation.theme.LocalFontSettings
import org.koin.compose.koinInject

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
    val fontSettings = LocalFontSettings.current
    val urlLauncher = koinInject<UrlLauncher>()
    val clipboardService = koinInject<ClipboardService>()
    val toastPresenter = koinInject<ToastPresenter>()
    val youtubeUrlCopiedMsg = stringResource(R.string.youtube_url_copied)
    val webUrlCopiedMsg = stringResource(R.string.web_url_copied)
    val memoCopiedMsg = stringResource(R.string.memo_copied)

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
                    fontSize = fontSettings.scaled(11),
                    color = colors.textSecondary,
                    fontWeight = FontWeight.Medium
                )

                Spacer(modifier = Modifier.height(4.dp))

                // 제목
                Text(
                    text = title ?: url,
                    fontSize = fontSettings.scaled(14),
                    fontWeight = FontWeight.SemiBold,
                    color = colors.textTitle,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                if (title != null) {
                    Text(
                        text = url,
                        fontSize = fontSettings.scaled(11),
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
                                when (sourceType) {
                                    SummarySourceType.YOUTUBE ->
                                        urlLauncher.openPreferringPackage(url, "com.google.android.youtube")
                                    else -> urlLauncher.open(url)
                                }
                            }
                            .padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.OpenInBrowser, null, tint = colors.chipText, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(stringResource(R.string.summary_card_open), fontSize = fontSettings.scaled(11), color = colors.chipText, fontWeight = FontWeight.Medium)
                    }

                    // URL 복사
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(colors.chipBackground)
                            .clickable {
                                clipboardService.copyPlainText("url", url)
                                val toastMsg = when (sourceType) {
                                    SummarySourceType.YOUTUBE -> youtubeUrlCopiedMsg
                                    SummarySourceType.WEB -> webUrlCopiedMsg
                                    else -> memoCopiedMsg
                                }
                                toastPresenter.show(toastMsg)
                            }
                            .padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.ContentCopy, null, tint = colors.chipText, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(stringResource(R.string.memo_copy), fontSize = fontSettings.scaled(11), color = colors.chipText, fontWeight = FontWeight.Medium)
                    }

                    if (!hasSummary && onSummarize != null) {
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
                            Text(stringResource(R.string.summary_mode_simple), fontSize = fontSettings.scaled(11), color = Color(0xFF2196F3), fontWeight = FontWeight.Medium)
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
                            Text(stringResource(R.string.summary_mode_detailed), fontSize = fontSettings.scaled(11), color = Color(0xFFFF9800), fontWeight = FontWeight.Medium)
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

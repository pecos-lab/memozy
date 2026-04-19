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
import androidx.compose.material.icons.filled.AutoFixHigh
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.SmartDisplay
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import me.pecos.memozy.feature.core.resource.generated.resources.Res
import me.pecos.memozy.feature.core.resource.generated.resources.memo_copy
import me.pecos.memozy.feature.core.resource.generated.resources.memo_copy_done
import me.pecos.memozy.feature.core.resource.generated.resources.memozy_ai
import me.pecos.memozy.feature.core.resource.generated.resources.summary_card_open
import me.pecos.memozy.feature.core.resource.generated.resources.summary_collapse
import me.pecos.memozy.feature.core.resource.generated.resources.summary_copy
import me.pecos.memozy.feature.core.resource.generated.resources.summary_expand
import me.pecos.memozy.feature.core.resource.generated.resources.summary_style_detailed
import me.pecos.memozy.feature.core.resource.generated.resources.summary_style_select
import me.pecos.memozy.feature.core.resource.generated.resources.summary_style_simple
import me.pecos.memozy.feature.core.resource.generated.resources.youtube_summarizing
import me.pecos.memozy.feature.core.resource.generated.resources.youtube_url_copied
import me.pecos.memozy.platform.intent.ClipboardService
import me.pecos.memozy.platform.intent.ToastPresenter
import me.pecos.memozy.platform.intent.UrlLauncher
import me.pecos.memozy.presentation.components.RemoteAsyncImage
import me.pecos.memozy.presentation.screen.home.model.SummaryEntry
import me.pecos.memozy.presentation.screen.memo.SummaryMode
import me.pecos.memozy.presentation.theme.AppColors
import me.pecos.memozy.presentation.theme.LocalFontSettings
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject

@Composable
fun YouTubeSummaryInlineCard(
    youtubeUrl: String,
    videoId: String?,
    title: String?,
    memoTitle: String,
    isExpanded: Boolean,
    onExpandToggle: (Boolean) -> Unit,
    summaryEntries: List<SummaryEntry>,
    streamingText: String?,
    isSummarizing: Boolean,
    currentSummaryMode: SummaryMode?,
    onSummarize: ((String, SummaryMode) -> Unit)?,
    onCancelSummarize: (() -> Unit)?,
    onResummarize: (SummaryMode) -> Unit,
    onDeleteSummary: ((Int) -> Unit)? = null,
    onDeleteAllSummaries: (() -> Unit)? = null,
    onAskAi: (() -> Unit)? = null,
    colors: AppColors,
    onStyleSelect: (() -> Unit)? = null
) {
    val fontSettings = LocalFontSettings.current
    val urlLauncher = koinInject<UrlLauncher>()
    val toastPresenter = koinInject<ToastPresenter>()
    val clipboardService = koinInject<ClipboardService>()
    val urlCopiedMsg = stringResource(Res.string.youtube_url_copied)
    val copyDoneMsg = stringResource(Res.string.memo_copy_done)
    val hasSummary = summaryEntries.isNotEmpty()

    if (!isExpanded) {
        Row(
            modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp))
                .background(colors.cardBackground).clickable { onExpandToggle(true) }.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("▶ ${stringResource(Res.string.summary_expand)}", fontSize = fontSettings.scaled(13), fontWeight = FontWeight.SemiBold, color = colors.chipText)
            Spacer(modifier = Modifier.weight(1f))
            Spacer(modifier = Modifier.width(12.dp))
            Text(title ?: "YouTube", fontSize = fontSettings.scaled(12), color = colors.textSecondary, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
        Spacer(modifier = Modifier.height(12.dp))
    }

    if (isExpanded) {
        Box(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(colors.cardBackground)
        ) {
            if (videoId != null) {
                RemoteAsyncImage(
                    url = "https://img.youtube.com/vi/$videoId/hqdefault.jpg",
                    contentDescription = null,
                    modifier = Modifier.fillMaxWidth().padding(16.dp).aspectRatio(16f / 9f)
                        .clip(RoundedCornerShape(12.dp)),
                    contentScale = ContentScale.Crop
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

                Spacer(modifier = Modifier.height(10.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Row(
                        modifier = Modifier.weight(1f).clip(RoundedCornerShape(6.dp)).background(colors.chipBackground)
                            .clickable {
                                urlLauncher.openPreferringPackage(youtubeUrl, "com.google.android.youtube")
                            }
                            .padding(vertical = 5.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(Icons.Default.SmartDisplay, null, tint = colors.chipText, modifier = Modifier.size(12.dp)); Spacer(modifier = Modifier.width(3.dp))
                        Text(stringResource(Res.string.summary_card_open), fontSize = fontSettings.scaled(10), color = colors.chipText, fontWeight = FontWeight.Medium)
                    }
                    Row(
                        modifier = Modifier.weight(1f).clip(RoundedCornerShape(6.dp)).background(colors.chipBackground)
                            .clickable {
                                clipboardService.copyPlainText("youtube_url", youtubeUrl)
                                toastPresenter.show(urlCopiedMsg)
                            }
                            .padding(vertical = 5.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(Icons.Default.ContentCopy, null, tint = colors.chipText, modifier = Modifier.size(12.dp))
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(stringResource(Res.string.memo_copy), fontSize = fontSettings.scaled(10), color = colors.chipText, fontWeight = FontWeight.Medium)
                    }
                    if (onSummarize != null && onStyleSelect != null) {
                        Row(
                            modifier = Modifier.weight(1f).clip(RoundedCornerShape(6.dp)).background(Color(0xFF2196F3).copy(alpha = 0.1f))
                                .clickable { onStyleSelect() }
                                .padding(vertical = 5.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Text(stringResource(Res.string.summary_style_select), fontSize = fontSettings.scaled(10), color = Color(0xFF2196F3), fontWeight = FontWeight.Medium)
                        }
                    }
                }

                if (isSummarizing) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        CircularProgressIndicator(modifier = Modifier.size(14.dp), strokeWidth = 2.dp, color = colors.textSecondary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(stringResource(Res.string.youtube_summarizing), fontSize = fontSettings.scaled(13), color = colors.textSecondary, modifier = Modifier.weight(1f))
                        Icon(Icons.Default.Close, contentDescription = null, tint = colors.textSecondary,
                            modifier = Modifier.size(16.dp).clickable { onCancelSummarize?.invoke() })
                    }
                    if (streamingText != null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        HorizontalDivider(color = colors.cardBorder)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(streamingText, fontSize = fontSettings.scaled(14), lineHeight = 22.sp, color = colors.textBody)
                    }
                }

                summaryEntries.forEachIndexed { index, entry ->
                    Spacer(modifier = Modifier.height(10.dp))
                    HorizontalDivider(color = colors.cardBorder)
                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val modeLabel = when (entry.mode) {
                            "DETAILED" -> stringResource(Res.string.summary_style_detailed)
                            else -> stringResource(Res.string.summary_style_simple)
                        }
                        Text(
                            text = modeLabel,
                            fontSize = fontSettings.scaled(11),
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFF2196F3)
                        )
                        if (onDeleteSummary != null) {
                            Icon(
                                Icons.Default.Close,
                                contentDescription = null,
                                tint = colors.textSecondary,
                                modifier = Modifier
                                    .size(16.dp)
                                    .clickable { onDeleteSummary(index) }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))
                    Text(entry.content, fontSize = fontSettings.scaled(14), lineHeight = 22.sp, color = colors.textBody)

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
                                    clipboardService.copyPlainText("summary", entry.content)
                                    toastPresenter.show(copyDoneMsg)
                                }
                                .padding(vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(Icons.Default.ContentCopy, null, tint = colors.chipText, modifier = Modifier.size(12.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(stringResource(Res.string.summary_copy), fontSize = fontSettings.scaled(10), color = colors.chipText, fontWeight = FontWeight.Medium)
                        }
                        if (onAskAi != null) {
                            Row(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(colors.chipBackground)
                                    .clickable { onAskAi() }
                                    .padding(vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Icon(Icons.Default.AutoFixHigh, null, tint = colors.chipText, modifier = Modifier.size(12.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(stringResource(Res.string.memozy_ai), fontSize = fontSettings.scaled(10), color = colors.chipText, fontWeight = FontWeight.Medium)
                            }
                        }
                    }
                }

                if (hasSummary) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth().clickable { onExpandToggle(false) },
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text("▼ ${stringResource(Res.string.summary_collapse)}", fontSize = fontSettings.scaled(12), color = colors.textSecondary)
                    }
                }
            }
        }
        if (hasSummary && onDeleteAllSummaries != null) {
            Icon(
                Icons.Default.Close,
                contentDescription = null,
                tint = colors.textSecondary,
                modifier = Modifier
                    .size(28.dp)
                    .align(Alignment.TopEnd)
                    .padding(6.dp)
                    .clickable { onDeleteAllSummaries() }
            )
        }
        }
        Spacer(modifier = Modifier.height(12.dp))
    }
}

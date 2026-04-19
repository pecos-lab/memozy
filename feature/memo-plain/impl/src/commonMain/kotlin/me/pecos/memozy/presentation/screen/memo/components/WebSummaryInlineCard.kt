package me.pecos.memozy.presentation.screen.memo.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.material.icons.filled.AutoFixHigh
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Link
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import me.pecos.memozy.feature.core.resource.generated.resources.Res
import me.pecos.memozy.feature.core.resource.generated.resources.memo_copy
import me.pecos.memozy.feature.core.resource.generated.resources.memo_copy_done
import me.pecos.memozy.feature.core.resource.generated.resources.memozy_ai
import me.pecos.memozy.feature.core.resource.generated.resources.summary_card_open
import me.pecos.memozy.feature.core.resource.generated.resources.summary_card_web
import me.pecos.memozy.feature.core.resource.generated.resources.summary_collapse
import me.pecos.memozy.feature.core.resource.generated.resources.summary_copy
import me.pecos.memozy.feature.core.resource.generated.resources.summary_expand
import me.pecos.memozy.feature.core.resource.generated.resources.summary_mode_detailed
import me.pecos.memozy.feature.core.resource.generated.resources.summary_mode_simple
import me.pecos.memozy.feature.core.resource.generated.resources.web_summarizing
import me.pecos.memozy.feature.core.resource.generated.resources.web_url_copied
import me.pecos.memozy.platform.intent.ClipboardService
import me.pecos.memozy.platform.intent.ToastPresenter
import me.pecos.memozy.platform.intent.UrlLauncher
import me.pecos.memozy.presentation.screen.memo.SummaryMode
import me.pecos.memozy.presentation.theme.AppColors
import me.pecos.memozy.presentation.theme.LocalFontSettings
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject

@Composable
fun WebSummaryInlineCard(
    webUrl: String,
    pageTitle: String?,
    isExpanded: Boolean,
    onExpandToggle: (Boolean) -> Unit,
    summaryText: String?,
    isWebSummarizing: Boolean,
    currentSummaryMode: SummaryMode?,
    onSummarize: ((String, SummaryMode) -> Unit)?,
    onCancelSummarize: (() -> Unit)?,
    onResummarize: (SummaryMode) -> Unit,
    onDeleteSummary: (() -> Unit)? = null,
    onAskAi: (() -> Unit)? = null,
    colors: AppColors
) {
    val fontSettings = LocalFontSettings.current
    val urlLauncher = koinInject<UrlLauncher>()
    val toastPresenter = koinInject<ToastPresenter>()
    val clipboardService = koinInject<ClipboardService>()
    val webUrlCopiedMsg = stringResource(Res.string.web_url_copied)
    val copyDoneMsg = stringResource(Res.string.memo_copy_done)

    if (!isExpanded) {
        Row(
            modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp))
                .background(colors.cardBackground).clickable { onExpandToggle(true) }.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("▶ ${stringResource(Res.string.summary_expand)}", fontSize = fontSettings.scaled(13), fontWeight = FontWeight.SemiBold, color = colors.chipText)
            Spacer(modifier = Modifier.weight(1f))
            Spacer(modifier = Modifier.width(12.dp))
            Text(pageTitle ?: stringResource(Res.string.summary_card_web), fontSize = fontSettings.scaled(12), color = colors.textSecondary, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
        Spacer(modifier = Modifier.height(12.dp))
    }

    if (isExpanded) {
        Box(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp))
                .background(colors.cardBackground).padding(16.dp)
        ) {
            Text("🔗 ${stringResource(Res.string.summary_card_web)}", fontSize = fontSettings.scaled(11), color = colors.textSecondary, fontWeight = FontWeight.Medium)
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = pageTitle ?: webUrl,
                fontSize = fontSettings.scaled(14), fontWeight = FontWeight.SemiBold, color = colors.textTitle,
                maxLines = 2, overflow = TextOverflow.Ellipsis
            )
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
                Text(webUrl, fontSize = fontSettings.scaled(10), color = Color(0xFF2196F3), maxLines = 1, overflow = TextOverflow.Ellipsis)
            }

            Spacer(modifier = Modifier.height(10.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Row(
                    modifier = Modifier.weight(1f).clip(RoundedCornerShape(6.dp)).background(colors.chipBackground)
                        .clickable { urlLauncher.open(webUrl) }
                        .padding(vertical = 5.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text("🔗", fontSize = fontSettings.scaled(10)); Spacer(modifier = Modifier.width(3.dp))
                    Text(stringResource(Res.string.summary_card_open), fontSize = fontSettings.scaled(10), color = colors.chipText, fontWeight = FontWeight.Medium)
                }
                Row(
                    modifier = Modifier.weight(1f).clip(RoundedCornerShape(6.dp)).background(colors.chipBackground)
                        .clickable {
                            clipboardService.copyPlainText("web_url", webUrl)
                            toastPresenter.show(webUrlCopiedMsg)
                        }
                        .padding(vertical = 5.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(Icons.Default.ContentCopy, null, tint = colors.chipText, modifier = Modifier.size(12.dp))
                    Spacer(modifier = Modifier.width(3.dp))
                    Text(stringResource(Res.string.memo_copy), fontSize = fontSettings.scaled(10), color = colors.chipText, fontWeight = FontWeight.Medium)
                }
                if (onSummarize != null && summaryText == null) {
                    Row(
                        modifier = Modifier.weight(1f).clip(RoundedCornerShape(6.dp)).background(Color(0xFF2196F3).copy(alpha = 0.1f))
                            .clickable { onSummarize(webUrl, SummaryMode.SIMPLE) }
                            .padding(vertical = 5.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) { Text(stringResource(Res.string.summary_mode_simple), fontSize = fontSettings.scaled(10), color = Color(0xFF2196F3), fontWeight = FontWeight.Medium) }
                    Row(
                        modifier = Modifier.weight(1f).clip(RoundedCornerShape(6.dp)).background(Color(0xFFFF9800).copy(alpha = 0.1f))
                            .clickable { onSummarize(webUrl, SummaryMode.DETAILED) }
                            .padding(vertical = 5.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) { Text(stringResource(Res.string.summary_mode_detailed), fontSize = fontSettings.scaled(10), color = Color(0xFFFF9800), fontWeight = FontWeight.Medium) }
                }
            }

            if (isWebSummarizing) {
                Spacer(modifier = Modifier.height(10.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    CircularProgressIndicator(modifier = Modifier.size(14.dp), strokeWidth = 2.dp, color = colors.textSecondary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(stringResource(Res.string.web_summarizing), fontSize = fontSettings.scaled(13), color = colors.textSecondary, modifier = Modifier.weight(1f))
                    Icon(Icons.Default.Close, contentDescription = null, tint = colors.textSecondary,
                        modifier = Modifier.size(16.dp).clickable { onCancelSummarize?.invoke() })
                }
            }

            if (summaryText != null) {
                Spacer(modifier = Modifier.height(10.dp))
                HorizontalDivider(color = colors.cardBorder)
                Spacer(modifier = Modifier.height(8.dp))
                Text(summaryText, fontSize = fontSettings.scaled(14), lineHeight = 22.sp, color = colors.textBody)

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
                                clipboardService.copyPlainText("summary", summaryText)
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

            if (summaryText != null) {
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

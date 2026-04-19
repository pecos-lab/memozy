package me.pecos.memozy.presentation.screen.home.components

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import me.pecos.memozy.feature.core.resource.generated.resources.Res
import me.pecos.memozy.feature.core.resource.generated.resources.memo_updated_at
import me.pecos.memozy.feature.core.viewmodel.model.MemoUiState
import me.pecos.memozy.presentation.screen.home.model.parseSummaryEntries
import me.pecos.memozy.presentation.screen.home.util.formatMemoTime
import me.pecos.memozy.presentation.theme.LocalAppColors
import me.pecos.memozy.presentation.theme.LocalFontSettings
import me.pecos.memozy.presentation.theme.LocalLanguageCode
import me.pecos.memozy.presentation.util.htmlToPlainText
import org.jetbrains.compose.resources.stringResource

@Composable
fun MemoCardItem(
    memo: MemoUiState,
    @Suppress("UNUSED_PARAMETER") isInSelectionMode: Boolean = false
) {
    val colors = LocalAppColors.current
    val fontSettings = LocalFontSettings.current
    val languageCode = LocalLanguageCode.current

    Card(
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .border(1.dp, colors.cardBorder, RoundedCornerShape(12.dp)),
        elevation = CardDefaults.cardElevation(0.dp),
        colors = CardDefaults.cardColors(containerColor = colors.cardBackground)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    if (memo.isPinned) {
                        Icon(
                            Icons.Default.Star,
                            contentDescription = null,
                            tint = Color(0xFFFFA726),
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                    }
                    Text(
                        text = memo.name,
                        fontWeight = FontWeight.Bold,
                        color = colors.textTitle,
                        fontFamily = fontSettings.fontFamily,
                        fontSize = fontSettings.titleSize,
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = formatMemoTime(memo.createdAt, languageCode),
                        color = colors.textSecondary,
                        fontSize = fontSettings.scaled(12)
                    )
                    if (memo.updatedAt > memo.createdAt + 60_000L) {
                        Text(
                            text = "${stringResource(Res.string.memo_updated_at)} ${formatMemoTime(memo.updatedAt, languageCode)}",
                            fontSize = fontSettings.scaled(11),
                            color = colors.textSecondary.copy(alpha = 0.7f)
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = memo.content.htmlToPlainText()
                    .ifBlank { parseSummaryEntries(memo.summaryContent).firstOrNull()?.content?.take(200) ?: "" },
                color = colors.textBody,
                fontFamily = fontSettings.fontFamily,
                fontSize = fontSettings.bodySize,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

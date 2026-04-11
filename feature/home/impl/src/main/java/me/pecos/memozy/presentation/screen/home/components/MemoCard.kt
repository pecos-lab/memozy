package me.pecos.memozy.presentation.screen.home.components

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import me.pecos.memozy.feature.core.resource.CATEGORY_EMOJIS
import me.pecos.memozy.feature.core.resource.CATEGORY_RES_IDS
import me.pecos.memozy.presentation.screen.home.model.MemoUiState
import me.pecos.memozy.presentation.screen.home.model.TagUiState
import me.pecos.memozy.feature.core.resource.R
import me.pecos.memozy.presentation.screen.home.util.formatMemoTime
import me.pecos.memozy.presentation.theme.LocalAppColors

// ── 메모 카드 (프리뷰 전용) ──────────────────────────────────────────────────

@Composable
fun MemoCardItem(
    memo: MemoUiState,
    tags: List<TagUiState> = emptyList(),
    onTagsClick: (() -> Unit)? = null
) {
    val colors = LocalAppColors.current
    val context = LocalContext.current
    val languageCode = remember {
        context.getSharedPreferences("settings", Context.MODE_PRIVATE)
            .getString("language_code", "ko") ?: "ko"
    }

    Card(
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .border(1.dp, colors.cardBorder, RoundedCornerShape(12.dp)),
        elevation = CardDefaults.cardElevation(0.dp),
        colors = CardDefaults.cardColors(containerColor = colors.cardBackground)
    ) {
        Column(modifier = Modifier.padding(16.dp).animateContentSize(tween(200))) {
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
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = formatMemoTime(memo.createdAt, languageCode),
                        color = colors.textSecondary,
                        fontSize = 12.sp
                    )
                    if (memo.updatedAt > memo.createdAt + 60_000L) {
                        Text(
                            text = "${stringResource(R.string.memo_updated_at)} ${formatMemoTime(memo.updatedAt, languageCode)}",
                            fontSize = 11.sp,
                            color = colors.textSecondary.copy(alpha = 0.7f)
                        )
                    }
                    if (tags.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            modifier = if (onTagsClick != null) Modifier.clickable(onClick = onTagsClick) else Modifier
                        ) {
                            tags.take(2).forEach { tag ->
                                Text(
                                    text = tag.name,
                                    fontSize = 10.sp,
                                    color = colors.chipText,
                                    maxLines = 1,
                                    modifier = Modifier
                                        .background(colors.chipBackground, RoundedCornerShape(50))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                            if (tags.size > 2) {
                                Text(
                                    text = "+${tags.size - 2}",
                                    fontSize = 10.sp,
                                    color = colors.textSecondary,
                                    modifier = Modifier.padding(start = 2.dp)
                                )
                            }
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = memo.content
                    .replace(Regex("<br\\s*/?>"), "\n")
                    .replace(Regex("<[^>]+>"), "")
                    .replace("&nbsp;", " ")
                    .replace("&amp;", "&")
                    .trim()
                    .ifBlank { memo.summaryContent?.take(200) ?: "" },
                color = colors.textBody,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

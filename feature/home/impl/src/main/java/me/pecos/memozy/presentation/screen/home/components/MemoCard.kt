package me.pecos.memozy.presentation.screen.home.components

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import me.pecos.memozy.feature.core.resource.R
import me.pecos.memozy.presentation.screen.home.util.formatMemoTime
import me.pecos.memozy.presentation.theme.LocalAppColors

// ── 메모 카드 (프리뷰 전용) ──────────────────────────────────────────────────

@Composable
fun MemoCardItem(
    memo: MemoUiState
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
                    if (memo.categoryId in 1..CATEGORY_RES_IDS.size) {
                        val categoryIndex = memo.categoryId - 1
                        val categoryLabel = "${CATEGORY_EMOJIS[categoryIndex]} ${stringResource(CATEGORY_RES_IDS[categoryIndex])}"
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = categoryLabel,
                            fontSize = 11.sp,
                            color = colors.chipText,
                            modifier = Modifier
                                .background(colors.chipBackground, RoundedCornerShape(50))
                                .padding(horizontal = 8.dp, vertical = 2.dp)
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = memo.content,
                color = colors.textBody,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

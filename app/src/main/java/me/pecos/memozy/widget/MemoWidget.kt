package me.pecos.memozy.widget

import android.content.Context
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.action.actionParametersOf
import androidx.glance.action.actionStartActivity
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.size
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import dagger.hilt.android.EntryPointAccessors
import me.pecos.memozy.MainActivity
import me.pecos.memozy.R
import me.pecos.memozy.data.datasource.local.entity.Memo
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MemoWidget : GlanceAppWidget() {

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val entryPoint = EntryPointAccessors.fromApplication(
            context.applicationContext,
            WidgetEntryPoint::class.java
        )
        val repository = entryPoint.memoRepository()
        val memos = repository.getRecentMemos(5)

        provideContent {
            GlanceTheme {
                Column(
                    modifier = GlanceModifier
                        .fillMaxSize()
                        .background(MemoWidgetColors.background)
                        .cornerRadius(16.dp)
                        .padding(16.dp)
                ) {
                    // Header
                    Row(
                        modifier = GlanceModifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Memozy",
                            style = TextStyle(
                                color = MemoWidgetColors.title,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold
                            ),
                            modifier = GlanceModifier.defaultWeight()
                        )
                        Box(
                            modifier = GlanceModifier
                                .size(36.dp)
                                .cornerRadius(18.dp)
                                .background(MemoWidgetColors.addButtonBackground)
                                .clickable(
                                    actionStartActivity<MainActivity>(
                                        parameters = newMemoParams()
                                    )
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Image(
                                provider = ImageProvider(R.drawable.ic_widget_add),
                                contentDescription = "새 메모",
                                modifier = GlanceModifier.size(20.dp)
                            )
                        }
                    }

                    Spacer(modifier = GlanceModifier.height(12.dp))

                    if (memos.isEmpty()) {
                        Box(
                            modifier = GlanceModifier
                                .fillMaxWidth()
                                .defaultWeight(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = context.getString(R.string.widget_empty),
                                style = TextStyle(
                                    color = MemoWidgetColors.secondary,
                                    fontSize = 14.sp
                                )
                            )
                        }
                    } else {
                        Column(modifier = GlanceModifier.defaultWeight()) {
                            for (memo in memos) {
                                Column(
                                    modifier = GlanceModifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp)
                                        .cornerRadius(12.dp)
                                        .background(MemoWidgetColors.cardBackground)
                                        .padding(12.dp)
                                        .clickable(
                                            actionStartActivity<MainActivity>(
                                                parameters = openMemoParams(memo.id)
                                            )
                                        )
                                ) {
                                    Text(
                                        text = memo.name.ifBlank { context.getString(R.string.widget_untitled) },
                                        style = TextStyle(
                                            color = MemoWidgetColors.title,
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Medium
                                        ),
                                        maxLines = 1
                                    )
                                    val widgetContent = memo.content.ifBlank { memo.summaryContent ?: "" }
                                    if (widgetContent.isNotBlank()) {
                                        Spacer(modifier = GlanceModifier.height(2.dp))
                                        Text(
                                            text = widgetContent.take(80),
                                            style = TextStyle(
                                                color = MemoWidgetColors.body,
                                                fontSize = 12.sp
                                            ),
                                            maxLines = 2
                                        )
                                    }
                                    Spacer(modifier = GlanceModifier.height(4.dp))
                                    Text(
                                        text = formatDate(memo.updatedAt),
                                        style = TextStyle(
                                            color = MemoWidgetColors.secondary,
                                            fontSize = 10.sp
                                        )
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    companion object {
        private fun newMemoParams() = actionParametersOf(
            MemoWidgetKeys.ACTION_KEY to MemoWidgetKeys.ACTION_NEW_MEMO
        )

        private fun openMemoParams(memoId: Int) = actionParametersOf(
            MemoWidgetKeys.ACTION_KEY to MemoWidgetKeys.ACTION_OPEN_MEMO,
            MemoWidgetKeys.MEMO_ID_KEY to memoId
        )

        private fun formatDate(timestamp: Long): String {
            val now = System.currentTimeMillis()
            val diff = now - timestamp
            return when {
                diff < 60_000 -> "방금"
                diff < 3_600_000 -> "${diff / 60_000}분 전"
                diff < 86_400_000 -> "${diff / 3_600_000}시간 전"
                diff < 604_800_000 -> "${diff / 86_400_000}일 전"
                else -> SimpleDateFormat("M/d", Locale.getDefault()).format(Date(timestamp))
            }
        }
    }
}

package me.pecos.memozy.presentation.screen.memo.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Quiz
import androidx.compose.material.icons.filled.SmartDisplay
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import me.pecos.memozy.feature.core.resource.R
import me.pecos.memozy.presentation.screen.home.model.MemoUiState
import me.pecos.memozy.presentation.screen.memo.SummaryMode
import me.pecos.memozy.presentation.theme.AppColors

@Composable
fun MemoActionBar(
    colors: AppColors,
    isNewMemo: Boolean,
    existingMemo: MemoUiState,
    // 녹음
    onStartRecording: (() -> Unit)?,
    onStopRecording: (() -> Unit)?,
    isRecording: Boolean,
    isTranscribing: Boolean,
    // 유튜브
    onYoutubeSummarize: ((String, SummaryMode) -> Unit)?,
    isSummarizing: Boolean,
    isWebSummarizing: Boolean,
    detectedYoutubeUrl: String?,
    onYoutubeChipClick: () -> Unit,
    onYoutubeDialogOpen: () -> Unit,
    // 웹
    onWebSummarize: ((String, SummaryMode) -> Unit)?,
    onWebDialogOpen: () -> Unit,
    // 리마인더
    onSetReminder: ((Int, Long?) -> Unit)?,
    // 퀴즈
    onQuiz: ((Int) -> Unit)?
) {
    HorizontalDivider(color = colors.cardBorder)
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 🎙️ 녹음
        if (onStartRecording != null) {
            Row(
                modifier = Modifier.clip(RoundedCornerShape(20.dp))
                    .background(
                        if (isRecording) Color(0xFFE24B4A)
                        else if (isTranscribing) colors.chipBackground.copy(alpha = 0.3f)
                        else colors.chipBackground
                    )
                    .clickable(enabled = !isTranscribing) {
                        if (isRecording) onStopRecording?.invoke() else onStartRecording()
                    }
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = if (isRecording) Icons.Default.Stop else Icons.Default.Mic,
                    contentDescription = null,
                    tint = if (isRecording) Color.White else colors.chipText,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = if (isRecording) stringResource(R.string.recording_stop) else stringResource(R.string.recording_start),
                    fontSize = 12.sp,
                    color = if (isRecording) Color.White else colors.chipText,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }

        // 📺 유튜브 요약
        if (onYoutubeSummarize != null) {
            val hasYoutubeUrl = detectedYoutubeUrl != null
            Row(
                modifier = Modifier.clip(RoundedCornerShape(20.dp))
                    .background(
                        if (isSummarizing || isWebSummarizing) colors.chipBackground.copy(alpha = 0.3f)
                        else if (hasYoutubeUrl) Color(0xFF2196F3).copy(alpha = 0.1f)
                        else colors.chipBackground
                    )
                    .clickable(enabled = !isSummarizing && !isWebSummarizing) {
                        if (hasYoutubeUrl) onYoutubeChipClick() else onYoutubeDialogOpen()
                    }
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.SmartDisplay, contentDescription = null,
                    tint = if (hasYoutubeUrl) Color(0xFF2196F3) else colors.chipText,
                    modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text(stringResource(R.string.youtube_label), fontSize = 12.sp,
                    color = if (hasYoutubeUrl) Color(0xFF2196F3) else colors.chipText,
                    fontWeight = FontWeight.SemiBold)
            }
        }

        // 🔗 웹 요약
        if (onWebSummarize != null) {
            Row(
                modifier = Modifier.clip(RoundedCornerShape(20.dp))
                    .background(
                        if (isSummarizing || isWebSummarizing) colors.chipBackground.copy(alpha = 0.3f)
                        else colors.chipBackground
                    )
                    .clickable(enabled = !isSummarizing && !isWebSummarizing) { onWebDialogOpen() }
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.Link, contentDescription = null, tint = colors.chipText, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text(stringResource(R.string.link_label), fontSize = 12.sp, color = colors.chipText, fontWeight = FontWeight.SemiBold)
            }
        }

        // ⏰ 리마인더
        if (onSetReminder != null && !isNewMemo) {
            var showReminderPicker by remember { mutableStateOf(false) }
            val hasReminder = existingMemo.reminderAt != null && existingMemo.reminderAt > System.currentTimeMillis()
            Row(
                modifier = Modifier.clip(RoundedCornerShape(20.dp))
                    .background(if (hasReminder) Color(0xFFFFA726).copy(alpha = 0.15f) else colors.chipBackground)
                    .clickable { showReminderPicker = true }
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.Notifications, contentDescription = null,
                    tint = if (hasReminder) Color(0xFFFFA726) else colors.chipText,
                    modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text(stringResource(R.string.notification_label), fontSize = 12.sp,
                    color = if (hasReminder) Color(0xFFFFA726) else colors.chipText,
                    fontWeight = FontWeight.SemiBold)
            }

            if (showReminderPicker) {
                ReminderPickerDialog(
                    currentReminder = existingMemo.reminderAt,
                    onDismiss = { showReminderPicker = false },
                    onConfirm = { reminderAt ->
                        onSetReminder(existingMemo.id, reminderAt)
                        showReminderPicker = false
                    },
                    onCancel = {
                        onSetReminder(existingMemo.id, null)
                        showReminderPicker = false
                    }
                )
            }
        }

        // 🧠 퀴즈
        if (onQuiz != null && !isNewMemo && existingMemo.content.length >= 20) {
            Row(
                modifier = Modifier.clip(RoundedCornerShape(20.dp))
                    .background(colors.chipBackground)
                    .clickable { onQuiz(existingMemo.id) }
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.Quiz, contentDescription = null, tint = colors.chipText, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text(stringResource(R.string.quiz_label), fontSize = 12.sp, color = colors.chipText, fontWeight = FontWeight.SemiBold)
            }
        }

        Spacer(modifier = Modifier.weight(1f))
    }
}

package me.pecos.memozy.presentation.screen.memo.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Quiz
import androidx.compose.material.icons.filled.SmartDisplay
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
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
    Row(
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 🎙️ 녹음
        if (onStartRecording != null) {
            Box(
                modifier = Modifier.size(36.dp).clip(RoundedCornerShape(8.dp))
                    .background(
                        if (isRecording) Color(0xFFE24B4A)
                        else if (isTranscribing) colors.chipBackground.copy(alpha = 0.3f)
                        else colors.chipBackground.copy(alpha = 0.4f)
                    )
                    .clickable(enabled = !isTranscribing) {
                        if (isRecording) onStopRecording?.invoke() else onStartRecording()
                    },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (isRecording) Icons.Default.Stop else Icons.Default.Mic,
                    contentDescription = null,
                    tint = if (isRecording) Color.White else colors.chipText,
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        // 📺 유튜브 요약
        if (onYoutubeSummarize != null) {
            val hasYoutubeUrl = detectedYoutubeUrl != null
            Box(
                modifier = Modifier.size(36.dp).clip(RoundedCornerShape(8.dp))
                    .background(
                        if (isSummarizing || isWebSummarizing) colors.chipBackground.copy(alpha = 0.3f)
                        else if (hasYoutubeUrl) Color(0xFF2196F3).copy(alpha = 0.1f)
                        else colors.chipBackground.copy(alpha = 0.4f)
                    )
                    .clickable(enabled = !isSummarizing && !isWebSummarizing) {
                        if (hasYoutubeUrl) onYoutubeChipClick() else onYoutubeDialogOpen()
                    },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.SmartDisplay, contentDescription = null,
                    tint = if (hasYoutubeUrl) Color(0xFF2196F3) else colors.chipText,
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        // 🔗 웹 요약
        if (onWebSummarize != null) {
            Box(
                modifier = Modifier.size(36.dp).clip(RoundedCornerShape(8.dp))
                    .background(
                        if (isSummarizing || isWebSummarizing) colors.chipBackground.copy(alpha = 0.3f)
                        else colors.chipBackground.copy(alpha = 0.4f)
                    )
                    .clickable(enabled = !isSummarizing && !isWebSummarizing) { onWebDialogOpen() },
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Link, contentDescription = null, tint = colors.chipText, modifier = Modifier.size(20.dp))
            }
        }

        // ⏰ 리마인더
        if (onSetReminder != null && !isNewMemo) {
            var showReminderPicker by remember { mutableStateOf(false) }
            val reminderAt = existingMemo.reminderAt
            val hasReminder = reminderAt != null && reminderAt > System.currentTimeMillis()
            Box(
                modifier = Modifier.size(36.dp).clip(RoundedCornerShape(8.dp))
                    .background(if (hasReminder) Color(0xFFFFA726).copy(alpha = 0.15f) else colors.chipBackground.copy(alpha = 0.4f))
                    .clickable { showReminderPicker = true },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.Notifications, contentDescription = null,
                    tint = if (hasReminder) Color(0xFFFFA726) else colors.chipText,
                    modifier = Modifier.size(20.dp)
                )
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
            Box(
                modifier = Modifier.size(36.dp).clip(RoundedCornerShape(8.dp))
                    .background(colors.chipBackground.copy(alpha = 0.4f))
                    .clickable { onQuiz(existingMemo.id) },
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Quiz, contentDescription = null, tint = colors.chipText, modifier = Modifier.size(20.dp))
            }
        }

    }
}

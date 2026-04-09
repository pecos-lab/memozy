package me.pecos.memozy.presentation.screen.memo.components

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import me.pecos.memozy.feature.core.resource.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReminderPickerDialog(
    currentReminder: Long?,
    onDismiss: () -> Unit,
    onConfirm: (Long) -> Unit,
    onCancel: () -> Unit
) {
    val calendar = java.util.Calendar.getInstance()
    if (currentReminder != null) calendar.timeInMillis = currentReminder

    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = calendar.timeInMillis
    )
    var showTimePicker by remember { mutableStateOf(false) }

    if (!showTimePicker) {
        DatePickerDialog(
            onDismissRequest = onDismiss,
            confirmButton = {
                TextButton(onClick = { showTimePicker = true }) {
                    Text(stringResource(R.string.next_step))
                }
            },
            dismissButton = {
                if (currentReminder != null) {
                    TextButton(onClick = onCancel) {
                        Text(stringResource(R.string.cancel_reminder), color = Color(0xFFE24B4A))
                    }
                }
                TextButton(onClick = onDismiss) {
                    Text(stringResource(R.string.cancel))
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    } else {
        val timePickerState = rememberTimePickerState(
            initialHour = calendar.get(java.util.Calendar.HOUR_OF_DAY),
            initialMinute = calendar.get(java.util.Calendar.MINUTE)
        )
        AlertDialog(
            onDismissRequest = onDismiss,
            title = { Text(stringResource(R.string.time_select)) },
            text = { TimePicker(state = timePickerState) },
            confirmButton = {
                TextButton(onClick = {
                    val selectedDate = datePickerState.selectedDateMillis ?: System.currentTimeMillis()
                    val cal = java.util.Calendar.getInstance().apply {
                        timeInMillis = selectedDate
                        set(java.util.Calendar.HOUR_OF_DAY, timePickerState.hour)
                        set(java.util.Calendar.MINUTE, timePickerState.minute)
                        set(java.util.Calendar.SECOND, 0)
                    }
                    onConfirm(cal.timeInMillis)
                }) {
                    Text(stringResource(R.string.confirm))
                }
            },
            dismissButton = {
                TextButton(onClick = { showTimePicker = false }) {
                    Text(stringResource(R.string.previous))
                }
            }
        )
    }
}

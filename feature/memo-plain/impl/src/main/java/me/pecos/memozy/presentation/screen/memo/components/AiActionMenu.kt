package me.pecos.memozy.presentation.screen.memo.components

import android.view.HapticFeedbackConstants
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import me.pecos.memozy.presentation.theme.LocalAppColors
import me.pecos.memozy.presentation.theme.LocalFontSettings

enum class AiPresetAction(
    val emoji: String,
    val label: String
) {
    EXPLAIN("\uD83E\uDD14", "이해하기 쉽게 설명해줘"),
    ORGANIZE("\uD83D\uDCDD", "깔끔하게 정리해줘"),
    SUMMARIZE("✂\uFE0F", "핵심만 요약해줘"),
    CUSTOM("✏\uFE0F", "직접 입력");
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AiActionMenu(
    onPresetSelected: (AiPresetAction) -> Unit,
    onDismiss: () -> Unit
) {
    val colors = LocalAppColors.current
    val fontSettings = LocalFontSettings.current
    val view = LocalView.current

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(),
        containerColor = colors.cardBackground
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 24.dp)
        ) {
            Text(
                text = "AI 어시스트",
                fontSize = fontSettings.scaled(16),
                fontWeight = FontWeight.Bold,
                color = colors.textTitle,
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp)
            )
            Spacer(modifier = Modifier.padding(top = 8.dp))

            AiPresetAction.entries.forEachIndexed { index, action ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                            onPresetSelected(action)
                        }
                        .padding(horizontal = 20.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = action.emoji,
                        fontSize = fontSettings.scaled(18)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = action.label,
                        fontSize = fontSettings.scaled(15),
                        color = colors.textBody
                    )
                }
                if (index < AiPresetAction.entries.size - 1) {
                    HorizontalDivider(
                        color = colors.chipBackground.copy(alpha = 0.3f),
                        thickness = 0.5.dp,
                        modifier = Modifier.padding(horizontal = 20.dp)
                    )
                }
            }
        }
    }
}

package me.pecos.memozy.presentation.screen.memo.components

import android.view.HapticFeedbackConstants
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import me.pecos.memozy.presentation.theme.LocalAppColors
import me.pecos.memozy.presentation.theme.LocalFontSettings

enum class AiPresetAction(
    val emoji: String,
    val label: String
) {
    EXPLAIN("🤔", "이해하기 쉽게 설명해줘"),
    ORGANIZE("📝", "깔끔하게 정리해줘"),
    SUMMARIZE("✂️", "핵심만 요약해줘"),
    CUSTOM("✏️", "직접 입력");
}

@Composable
fun AiActionMenu(
    onPresetSelected: (AiPresetAction) -> Unit,
    onDismiss: () -> Unit
) {
    val colors = LocalAppColors.current
    val fontSettings = LocalFontSettings.current
    val view = LocalView.current

    Popup(
        alignment = Alignment.BottomStart,
        onDismissRequest = onDismiss,
        properties = PopupProperties(focusable = true)
    ) {
        ElevatedCard(
            shape = RoundedCornerShape(12.dp),
            elevation = CardDefaults.elevatedCardElevation(defaultElevation = 8.dp),
            colors = CardDefaults.elevatedCardColors(containerColor = colors.cardBackground)
        ) {
            Column(modifier = Modifier.padding(vertical = 4.dp)) {
                AiPresetAction.entries.forEachIndexed { index, action ->
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .clickable {
                                view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                                onPresetSelected(action)
                            }
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = action.emoji,
                            fontSize = fontSettings.scaled(15)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = action.label,
                            fontSize = fontSettings.scaled(14),
                            color = colors.textBody
                        )
                    }
                    if (index < AiPresetAction.entries.size - 1) {
                        HorizontalDivider(
                            color = colors.chipBackground.copy(alpha = 0.3f),
                            thickness = 0.5.dp
                        )
                    }
                }
            }
        }
    }
}

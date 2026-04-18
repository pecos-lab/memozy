package me.pecos.memozy.presentation.screen.memo.components

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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import me.pecos.memozy.platform.intent.HapticKind
import me.pecos.memozy.platform.intent.HapticService
import me.pecos.memozy.presentation.theme.LocalAppColors
import me.pecos.memozy.presentation.theme.LocalFontSettings
import org.koin.compose.koinInject

enum class AiPresetAction(
    val emoji: String,
    val labelResId: Int
) {
    EXPLAIN("\uD83E\uDD14", me.pecos.memozy.feature.core.resource.R.string.ai_action_explain),
    ORGANIZE("\uD83D\uDCDD", me.pecos.memozy.feature.core.resource.R.string.ai_action_organize),
    SUMMARIZE("✂\uFE0F", me.pecos.memozy.feature.core.resource.R.string.ai_action_summarize),
    CUSTOM("✏\uFE0F", me.pecos.memozy.feature.core.resource.R.string.ai_action_custom);
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AiActionMenu(
    onPresetSelected: (AiPresetAction) -> Unit,
    onDismiss: () -> Unit
) {
    val colors = LocalAppColors.current
    val fontSettings = LocalFontSettings.current
    val hapticService = koinInject<HapticService>()

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
                text = stringResource(me.pecos.memozy.feature.core.resource.R.string.memozy_ai),
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
                            hapticService.perform(HapticKind.KeyboardTap)
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
                        text = stringResource(action.labelResId),
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

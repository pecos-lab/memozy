package me.pecos.memozy.presentation.screen.memo.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import me.pecos.memozy.presentation.screen.memo.SummaryStyle
import me.pecos.memozy.presentation.theme.LocalAppColors
import me.pecos.memozy.presentation.theme.LocalFontSettings

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SummaryStyleBottomSheet(
    onSelect: (SummaryStyle) -> Unit,
    onDismiss: () -> Unit
) {
    val colors = LocalAppColors.current
    val fontSettings = LocalFontSettings.current

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(),
        containerColor = colors.cardBackground
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp)
        ) {
            Text(
                text = "요약 양식 선택",
                fontSize = fontSettings.scaled(18),
                fontWeight = FontWeight.Bold,
                color = colors.textTitle
            )

            Spacer(modifier = Modifier.height(16.dp))

            SummaryStyle.entries.forEach { style ->
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            onSelect(style)
                            onDismiss()
                        }
                        .padding(vertical = 12.dp)
                ) {
                    Text(
                        text = style.displayName,
                        fontSize = fontSettings.scaled(15),
                        fontWeight = FontWeight.SemiBold,
                        color = colors.textTitle
                    )
                    Text(
                        text = style.description,
                        fontSize = fontSettings.scaled(12),
                        color = colors.textSecondary
                    )
                }
            }
        }
    }
}

package me.pecos.memozy.presentation.screen.memo.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import me.pecos.memozy.feature.core.resource.R
import me.pecos.memozy.presentation.theme.LocalAppColors
import me.pecos.memozy.presentation.theme.LocalFontSettings
import me.pecos.memozy.presentation.theme.SubscriptionTier

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AiLimitBottomSheet(
    subscriptionTier: SubscriptionTier,
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
                .padding(horizontal = 24.dp, vertical = 8.dp)
                .padding(bottom = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = stringResource(R.string.ai_limit_title),
                fontSize = fontSettings.scaled(18),
                fontWeight = FontWeight.Bold,
                color = colors.textTitle,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(12.dp))

            val message = if (subscriptionTier.isPro) {
                stringResource(R.string.ai_limit_pro_message, subscriptionTier.dailyAiLimit)
            } else {
                stringResource(R.string.ai_limit_free_message, subscriptionTier.dailyAiLimit)
            }

            Text(
                text = message,
                fontSize = fontSettings.scaled(14),
                color = colors.textBody,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(20.dp))

            if (!subscriptionTier.isPro) {
                Button(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = colors.chipText
                    )
                ) {
                    Text(
                        text = stringResource(R.string.ai_limit_upgrade),
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = stringResource(R.string.ai_limit_upgrade_desc),
                    fontSize = fontSettings.scaled(12),
                    color = colors.textBody.copy(alpha = 0.6f),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(12.dp))
            }

            OutlinedButton(
                onClick = onDismiss,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = stringResource(R.string.ai_limit_close),
                    color = colors.textBody,
                    modifier = Modifier.padding(vertical = 4.dp)
                )
            }
        }
    }
}

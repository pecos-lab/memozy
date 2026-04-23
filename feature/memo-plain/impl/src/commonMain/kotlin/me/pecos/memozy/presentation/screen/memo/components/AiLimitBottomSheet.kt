package me.pecos.memozy.presentation.screen.memo.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import me.pecos.memozy.feature.core.resource.generated.resources.Res
import me.pecos.memozy.feature.core.resource.generated.resources.ai_limit_ad_exhausted
import me.pecos.memozy.feature.core.resource.generated.resources.ai_limit_ad_remaining
import me.pecos.memozy.feature.core.resource.generated.resources.ai_limit_close
import me.pecos.memozy.feature.core.resource.generated.resources.ai_limit_free_message
import me.pecos.memozy.feature.core.resource.generated.resources.ai_limit_ios_unsupported
import me.pecos.memozy.feature.core.resource.generated.resources.ai_limit_pro_message
import me.pecos.memozy.feature.core.resource.generated.resources.ai_limit_title
import me.pecos.memozy.feature.core.resource.generated.resources.ai_limit_upgrade
import me.pecos.memozy.feature.core.resource.generated.resources.ai_limit_upgrade_desc
import me.pecos.memozy.feature.core.resource.generated.resources.ai_limit_watch_ad
import me.pecos.memozy.presentation.theme.LocalAppColors
import me.pecos.memozy.presentation.theme.LocalFontSettings
import me.pecos.memozy.presentation.theme.SubscriptionTier
import me.pecos.memozy.presentation.util.stringResourceFormatted
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AiLimitBottomSheet(
    subscriptionTier: SubscriptionTier,
    canWatchAd: Boolean,
    remainingAdViews: Int,
    isAdLoading: Boolean,
    onWatchAd: () -> Unit,
    onUpgrade: () -> Unit,
    onDismiss: () -> Unit,
    isPlatformSupported: Boolean = true,
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
                text = stringResource(Res.string.ai_limit_title),
                fontSize = fontSettings.scaled(18),
                fontWeight = FontWeight.Bold,
                color = colors.textTitle,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(12.dp))

            val message = if (subscriptionTier.isPro) {
                stringResourceFormatted(Res.string.ai_limit_pro_message, subscriptionTier.dailyAiLimit)
            } else {
                stringResourceFormatted(Res.string.ai_limit_free_message, subscriptionTier.dailyAiLimit)
            }

            Text(
                text = message,
                fontSize = fontSettings.scaled(14),
                color = colors.textBody,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(20.dp))

            if (!subscriptionTier.isPro) {
                // 광고 시청 버튼 (Free 유저만) — iOS는 현재 리워드 광고 SDK 미연동
                if (!isPlatformSupported) {
                    Text(
                        text = stringResource(Res.string.ai_limit_ios_unsupported),
                        fontSize = fontSettings.scaled(13),
                        color = colors.textSecondary,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                } else if (canWatchAd) {
                    Button(
                        onClick = onWatchAd,
                        enabled = !isAdLoading,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = colors.chipText
                        )
                    ) {
                        if (isAdLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                strokeWidth = 2.dp,
                                color = colors.cardBackground
                            )
                        } else {
                            Text(
                                text = stringResource(Res.string.ai_limit_watch_ad),
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(vertical = 4.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = stringResourceFormatted(Res.string.ai_limit_ad_remaining, remainingAdViews),
                        fontSize = fontSettings.scaled(12),
                        color = colors.textBody.copy(alpha = 0.6f),
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(12.dp))
                } else {
                    Text(
                        text = stringResource(Res.string.ai_limit_ad_exhausted),
                        fontSize = fontSettings.scaled(13),
                        color = colors.textSecondary,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                }

                // Pro 업그레이드 버튼
                OutlinedButton(
                    onClick = onUpgrade,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = stringResource(Res.string.ai_limit_upgrade),
                        color = colors.chipText,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = stringResource(Res.string.ai_limit_upgrade_desc),
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
                    text = stringResource(Res.string.ai_limit_close),
                    color = colors.textBody,
                    modifier = Modifier.padding(vertical = 4.dp)
                )
            }
        }
    }
}

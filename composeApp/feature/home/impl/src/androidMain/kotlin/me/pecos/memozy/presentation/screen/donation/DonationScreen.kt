package me.pecos.memozy.presentation.screen.donation

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import me.pecos.memozy.feature.core.resource.*
import me.pecos.memozy.data.billing.BillingManager
import me.pecos.memozy.data.billing.DonationProduct
import me.pecos.memozy.data.billing.PurchaseState
import me.pecos.memozy.presentation.components.AppPopup
import me.pecos.memozy.presentation.components.PopupActionArea
import me.pecos.memozy.presentation.components.PopupNavigation
import me.pecos.memozy.presentation.components.PopupSize
import me.pecos.memozy.presentation.theme.LocalActivity
import me.pecos.memozy.presentation.theme.LocalAppColors

private data class DonationTier(
    val productId: String,
    val emoji: String,
    val nameResId: StringResource,
    val descResId: StringResource
)

private val DONATION_TIERS = listOf(
    DonationTier("donation_rice_1", "\uD83C\uDF5A", Res.string.donation_tier_1_name, Res.string.donation_tier_1_desc),
    DonationTier("donation_rice_2", "\uD83C\uDF71", Res.string.donation_tier_2_name, Res.string.donation_tier_2_desc),
    DonationTier("donation_rice_3", "\uD83C\uDF5B", Res.string.donation_tier_3_name, Res.string.donation_tier_3_desc),
    DonationTier("donation_rice_4", "\uD83C\uDF3E", Res.string.donation_tier_4_name, Res.string.donation_tier_4_desc),
    DonationTier("donation_rice_5", "\uD83D\uDCB0", Res.string.donation_tier_5_name, Res.string.donation_tier_5_desc),
)

@Composable
fun DonationScreen(
    onBack: () -> Unit = {},
    billingManager: BillingManager
) {
    val products by billingManager.products.collectAsState()
    val purchaseState by billingManager.purchaseState.collectAsState()
    val isConnected by billingManager.isConnected.collectAsState()
    val colors = LocalAppColors.current
    val activity = LocalActivity.current

    LaunchedEffect(Unit) {
        billingManager.connect()
    }

    if (purchaseState is PurchaseState.Success) {
        AppPopup(
            onDismissRequest = { billingManager.resetPurchaseState() },
            title = stringResource(Res.string.donation_thank_title),
            navigation = PopupNavigation.EMPHASIZED,
            size = PopupSize.MEDIUM,
            actionArea = PopupActionArea.NONE
        ) {
            Text(
                text = stringResource(Res.string.donation_thank_message),
                color = colors.textBody
            )
        }
    }

    if (purchaseState is PurchaseState.Error) {
        AppPopup(
            onDismissRequest = { billingManager.resetPurchaseState() },
            title = stringResource(Res.string.donation_error_title),
            navigation = PopupNavigation.EMPHASIZED,
            size = PopupSize.MEDIUM,
            actionArea = PopupActionArea.NONE
        ) {
            Text(
                text = stringResource(Res.string.donation_error_message),
                color = colors.textBody
            )
        }
    }

    Scaffold(
        containerColor = colors.screenBackground
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 24.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 8.dp)
            ) {
                IconButton(onClick = onBack) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = stringResource(Res.string.close),
                        tint = colors.topbarTitle
                    )
                }
                Text(
                    text = stringResource(Res.string.donation_title),
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = colors.topbarTitle
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = stringResource(Res.string.donation_subtitle),
                fontSize = 14.sp,
                color = colors.textSecondary,
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            Spacer(modifier = Modifier.height(20.dp))

            DONATION_TIERS.forEach { tier ->
                val product = products.find { it.productId == tier.productId }
                val available = billingManager.isProductAvailable(tier.productId)
                DonationCard(
                    tier = tier,
                    product = product,
                    isConnected = isConnected && available,
                    onClick = {
                        if (activity != null) {
                            billingManager.launchPurchaseFlow(activity, tier.productId)
                        }
                    }
                )
                Spacer(modifier = Modifier.height(12.dp))
            }
        }
    }
}

@Composable
private fun DonationCard(
    tier: DonationTier,
    product: DonationProduct?,
    isConnected: Boolean,
    onClick: () -> Unit
) {
    val colors = LocalAppColors.current

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(colors.cardBackground)
            .border(1.dp, colors.cardBorder, RoundedCornerShape(16.dp))
            .clickable(enabled = isConnected) { onClick() }
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = tier.emoji,
                    fontSize = 32.sp
                )

                Column(
                    modifier = Modifier.padding(start = 12.dp)
                ) {
                    Text(
                        text = stringResource(tier.nameResId),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = colors.textTitle
                    )
                    Text(
                        text = stringResource(tier.descResId),
                        fontSize = 12.sp,
                        color = colors.textSecondary
                    )
                }
            }

            Text(
                text = product?.formattedPrice
                    ?: if (isConnected) "-" else stringResource(Res.string.donation_loading),
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = colors.textTitle,
                textAlign = TextAlign.End
            )
        }
    }
}

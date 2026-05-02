package me.pecos.memozy.presentation.screen.subscription

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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import me.pecos.memozy.feature.core.resource.generated.resources.Res
import me.pecos.memozy.feature.core.resource.generated.resources.close
import me.pecos.memozy.feature.core.resource.generated.resources.donation_error_message
import me.pecos.memozy.feature.core.resource.generated.resources.donation_error_title
import me.pecos.memozy.feature.core.resource.generated.resources.donation_loading
import me.pecos.memozy.feature.core.resource.generated.resources.subscription_current_plan
import me.pecos.memozy.feature.core.resource.generated.resources.subscription_desc
import me.pecos.memozy.feature.core.resource.generated.resources.subscription_feature_more
import me.pecos.memozy.feature.core.resource.generated.resources.subscription_feature_no_ads
import me.pecos.memozy.feature.core.resource.generated.resources.subscription_feature_ocr
import me.pecos.memozy.feature.core.resource.generated.resources.subscription_feature_web
import me.pecos.memozy.feature.core.resource.generated.resources.subscription_feature_youtube
import me.pecos.memozy.feature.core.resource.generated.resources.subscription_manage
import me.pecos.memozy.feature.core.resource.generated.resources.subscription_monthly
import me.pecos.memozy.feature.core.resource.generated.resources.subscription_restore
import me.pecos.memozy.feature.core.resource.generated.resources.subscription_title
import me.pecos.memozy.feature.core.resource.generated.resources.subscription_yearly
import me.pecos.memozy.feature.core.resource.generated.resources.subscription_yearly_discount
import me.pecos.memozy.platform.ads.AdsService
import me.pecos.memozy.platform.billing.BillingService
import me.pecos.memozy.platform.billing.PurchaseState
import me.pecos.memozy.platform.intent.UrlLauncher
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject
import me.pecos.memozy.presentation.components.AppPopup
import me.pecos.memozy.presentation.components.PopupActionArea
import me.pecos.memozy.presentation.components.PopupNavigation
import me.pecos.memozy.presentation.components.PopupSize
import me.pecos.memozy.presentation.theme.LocalActivity
import me.pecos.memozy.presentation.theme.LocalAppColors
import me.pecos.memozy.presentation.theme.LocalFontSettings
import me.pecos.memozy.presentation.theme.LocalIsLoggedIn
import me.pecos.memozy.presentation.theme.LocalSubscriptionTier

@Composable
fun SubscriptionScreen(
    onBack: () -> Unit = {},
    billingService: BillingService = koinInject(),
    adsService: AdsService = koinInject(),
) {
    val subscriptionProducts by billingService.subscriptionProducts.collectAsState()
    val purchaseState by billingService.purchaseState.collectAsState()
    val currentTier = LocalSubscriptionTier.current
    val isLoggedIn = LocalIsLoggedIn.current
    val colors = LocalAppColors.current
    val fontSettings = LocalFontSettings.current
    val activity = LocalActivity.current
    val urlLauncher: UrlLauncher = koinInject()
    val analyticsService: me.pecos.memozy.platform.analytics.AnalyticsService = koinInject()
    val isSystemDark = colors.screenBackground == Color(0xFF1C1C1E)

    androidx.compose.runtime.LaunchedEffect(Unit) {
        analyticsService.logEvent(
            me.pecos.memozy.platform.analytics.AnalyticsEvents.SUBSCRIPTION_VIEWED,
        )
    }

    androidx.compose.runtime.LaunchedEffect(purchaseState) {
        if (purchaseState is PurchaseState.Success) {
            analyticsService.logEvent(
                me.pecos.memozy.platform.analytics.AnalyticsEvents.SUBSCRIPTION_PURCHASED,
            )
        }
    }

    if (purchaseState is PurchaseState.Success) {
        AppPopup(
            onDismissRequest = { billingService.resetPurchaseState() },
            title = stringResource(Res.string.subscription_title),
            navigation = PopupNavigation.EMPHASIZED,
            size = PopupSize.MEDIUM,
            actionArea = PopupActionArea.NONE
        ) {
            Text(
                text = "Pro \uAD6C\uB3C5\uC774 \uD65C\uC131\uD654\uB418\uC5C8\uC5B4\uC694!",
                color = colors.textBody
            )
        }
    }

    if (purchaseState is PurchaseState.Error) {
        AppPopup(
            onDismissRequest = { billingService.resetPurchaseState() },
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
        ) {
            // ── 히어로 영역 ──
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(colors.screenBackground)
                    .padding(horizontal = 24.dp)
                    .padding(top = 24.dp, bottom = 32.dp)
            ) {
                Column {
                    // 뒤로가기 — IconButton 기본 12dp 내부 패딩 보정해서 M과 좌측 정렬
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier.offset(x = (-12).dp)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(Res.string.close),
                            tint = colors.textTitle
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "Memozy Pro",
                        fontSize = fontSettings.scaled(28),
                        fontWeight = FontWeight.Bold,
                        color = colors.textTitle
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = stringResource(Res.string.subscription_desc),
                        fontSize = fontSettings.scaled(15),
                        color = colors.textSecondary
                    )

                    if (currentTier.isPro) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = stringResource(Res.string.subscription_current_plan),
                            fontSize = fontSettings.scaled(13),
                            fontWeight = FontWeight.Bold,
                            color = colors.chipText,
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(colors.chipText.copy(alpha = 0.1f))
                                .padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }
                }
            }

            // ── Free vs Pro 비교 테이블 ──
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(colors.screenBackground)
                    .border(1.dp, colors.cardBorder, RoundedCornerShape(16.dp))
                    .padding(20.dp)
            ) {
                // 헤더
                Row(modifier = Modifier.fillMaxWidth()) {
                    Spacer(modifier = Modifier.weight(1.4f))
                    Column(
                        modifier = Modifier.weight(1f),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Free",
                            fontSize = fontSettings.scaled(13),
                            fontWeight = FontWeight.Bold,
                            color = colors.textSecondary,
                            textAlign = TextAlign.Center,
                        )
                        Text(
                            text = "광고 시청 후 이용",
                            fontSize = fontSettings.scaled(10),
                            color = colors.textSecondary,
                            textAlign = TextAlign.Center,
                        )
                    }
                    Text(
                        text = "Pro",
                        fontSize = fontSettings.scaled(13),
                        fontWeight = FontWeight.Bold,
                        color = colors.chipText,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))
                HorizontalDivider(color = colors.cardBorder, thickness = 0.5.dp)
                Spacer(modifier = Modifier.height(8.dp))

                // 비교 항목
                val comparisons = listOf(
                    Triple(stringResource(Res.string.subscription_feature_youtube), true, true),
                    Triple(stringResource(Res.string.subscription_feature_web), true, true),
                    Triple(stringResource(Res.string.subscription_feature_no_ads), false, true),
                )

                comparisons.forEach { (label, free, pro) ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = label,
                            fontSize = fontSettings.scaled(13),
                            color = colors.textBody,
                            modifier = Modifier.weight(1.4f)
                        )
                        Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                            if (free) {
                                Icon(Icons.Default.Check, null, tint = colors.textSecondary, modifier = Modifier.size(16.dp))
                            } else {
                                Icon(Icons.Default.Close, null, tint = colors.cardBorder, modifier = Modifier.size(16.dp))
                            }
                        }
                        Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                            if (pro) {
                                Icon(Icons.Default.Check, null, tint = colors.chipText, modifier = Modifier.size(16.dp))
                            } else {
                                Icon(Icons.Default.Close, null, tint = colors.cardBorder, modifier = Modifier.size(16.dp))
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            if (!isLoggedIn) {
                Text(
                    text = "로그인 없이는 구독이 어렵습니다",
                    fontSize = fontSettings.scaled(13),
                    color = colors.textSecondary,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(colors.cardBorder.copy(alpha = 0.15f))
                        .padding(horizontal = 16.dp, vertical = 10.dp)
                )
                Spacer(modifier = Modifier.height(12.dp))
            }

            // ── 구독 상품 카드 ──
            val monthly = subscriptionProducts.find { it.productId == BillingService.SUB_PRO_MONTHLY }
            val yearly = subscriptionProducts.find { it.productId == BillingService.SUB_PRO_YEARLY }

            if (yearly != null) {
                SubscriptionCard(
                    label = stringResource(Res.string.subscription_yearly),
                    price = yearly.formattedPrice,
                    badge = stringResource(Res.string.subscription_yearly_discount),
                    isCurrentPlan = currentTier.isPro,
                    isRecommended = true,
                    onClick = {
                        if (activity != null && !currentTier.isPro) {
                            billingService.launchSubscriptionFlow(activity, BillingService.SUB_PRO_YEARLY)
                        }
                    }
                )
                Spacer(modifier = Modifier.height(12.dp))
            }

            if (monthly != null) {
                SubscriptionCard(
                    label = stringResource(Res.string.subscription_monthly),
                    price = monthly.formattedPrice,
                    isCurrentPlan = currentTier.isPro,
                    onClick = {
                        if (activity != null && !currentTier.isPro) {
                            billingService.launchSubscriptionFlow(activity, BillingService.SUB_PRO_MONTHLY)
                        }
                    }
                )
                Spacer(modifier = Modifier.height(12.dp))
            }

            // 상품이 아직 로드 안 됐을 때
            if (subscriptionProducts.isEmpty()) {
                Text(
                    text = stringResource(Res.string.donation_loading),
                    fontSize = fontSettings.scaled(14),
                    color = colors.textSecondary,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // ── 하단 링크 ──
            if (currentTier.isPro) {
                Text(
                    text = stringResource(Res.string.subscription_manage),
                    fontSize = fontSettings.scaled(14),
                    color = colors.chipText,
                    fontWeight = FontWeight.Medium,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            urlLauncher.open("https://play.google.com/store/account/subscriptions")
                        }
                        .padding(12.dp)
                )
            }

            Text(
                text = stringResource(Res.string.subscription_restore),
                fontSize = fontSettings.scaled(14),
                color = colors.textSecondary,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { billingService.queryExistingSubscriptions() }
                    .padding(12.dp)
            )

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun SubscriptionCard(
    label: String,
    price: String,
    badge: String? = null,
    isCurrentPlan: Boolean,
    isRecommended: Boolean = false,
    onClick: () -> Unit
) {
    val colors = LocalAppColors.current
    val fontSettings = LocalFontSettings.current
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(colors.screenBackground)
            .border(
                width = if (isRecommended && !isCurrentPlan) 2.dp else 1.dp,
                color = if (isRecommended && !isCurrentPlan) colors.chipText else colors.cardBorder,
                shape = RoundedCornerShape(16.dp)
            )
            .clickable(enabled = !isCurrentPlan) { onClick() }
            .padding(20.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = label,
                        fontSize = fontSettings.scaled(16),
                        fontWeight = FontWeight.Bold,
                        color = colors.textTitle
                    )
                    if (badge != null) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = badge,
                            fontSize = fontSettings.scaled(11),
                            fontWeight = FontWeight.Bold,
                            color = colors.chipText,
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(colors.chipText.copy(alpha = 0.1f))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }

            Text(
                text = price,
                fontSize = fontSettings.scaled(18),
                fontWeight = FontWeight.Bold,
                color = if (isCurrentPlan) colors.textSecondary else colors.chipText,
                textAlign = TextAlign.End
            )
        }
    }
}

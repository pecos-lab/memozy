package me.pecos.memozy.platform.billing

import kotlin.time.Instant
import kotlinx.coroutines.flow.StateFlow
import me.pecos.memozy.presentation.theme.SubscriptionTier

data class DonationProduct(
    val productId: String,
    val name: String,
    val description: String,
    val formattedPrice: String,
)

data class SubscriptionProduct(
    val productId: String,
    val name: String,
    val description: String,
    val formattedPrice: String,
    val billingPeriod: String,
)

/**
 * 구독 라이프사이클 상태 — entitlement 활성/만료일/자동갱신/은혜기간 등 UI 노출용 정보.
 *
 * RC `EntitlementInfo` 의 핵심 필드 매핑 (#294 Epic 섹션 5).
 * FREE 사용자는 [Empty] 싱글톤.
 */
data class SubscriptionDetail(
    val tier: SubscriptionTier,
    val productId: String?,
    val expiresAt: Instant?,
    /** RC `willRenew` — 자동 갱신 예정 여부. false 면 만료일에 PRO 종료. */
    val willRenew: Boolean,
    /** RC `billingIssueDetectedAt` 가 셋 + entitlement 활성 → 결제 실패 후 은혜기간 진행 중. */
    val inGracePeriod: Boolean,
) {
    companion object {
        val Empty = SubscriptionDetail(
            tier = SubscriptionTier.FREE,
            productId = null,
            expiresAt = null,
            willRenew = false,
            inGracePeriod = false,
        )
    }
}

sealed class PurchaseState {
    data object Idle : PurchaseState()
    data object Loading : PurchaseState()
    data object Success : PurchaseState()
    data class Error(val message: String) : PurchaseState()
}

interface BillingService {
    val products: StateFlow<List<DonationProduct>>
    val subscriptionProducts: StateFlow<List<SubscriptionProduct>>
    val purchaseState: StateFlow<PurchaseState>
    val isConnected: StateFlow<Boolean>
    val subscriptionTier: StateFlow<SubscriptionTier>
    val subscriptionDetail: StateFlow<SubscriptionDetail>

    fun connect()
    fun disconnect()
    fun launchPurchaseFlow(activity: Any?, productId: String)
    fun launchSubscriptionFlow(activity: Any?, productId: String)
    fun queryExistingSubscriptions()
    /**
     * 다른 기기/계정에서 구매한 구독을 현재 RC appUserId 로 복원 시도.
     * 결과는 [purchaseState] 로 흘러간다 (Loading → Success/Error).
     */
    fun restorePurchases()
    fun isProductAvailable(productId: String): Boolean
    fun resetPurchaseState()

    companion object {
        const val SUB_PRO_MONTHLY = "pro_monthly"
        const val SUB_PRO_YEARLY = "pro_yearly"
    }
}

package me.pecos.memozy.platform.billing

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import me.pecos.memozy.presentation.theme.SubscriptionTier

/**
 * iOS 측 BillingService — KMP Wave 3-I 단계 stub.
 *
 * 이 시점에선 StoreKit 1 (SKProductsRequest / SKPaymentQueue) 의 delegate / observer 패턴을
 * 풀로 K/N 인터롭하는 작업과, App Store Connect 의 IAP 상품 등록 / Sandbox tester / Xcode
 * StoreKit Configuration 파일 등 외부 작업이 동시에 필요. 이 PR 스코프는 BillingService
 * 인터페이스가 iOS 에서도 만족되어 앱이 충돌 없이 실행되는 것까지 — 실 결제는 모두 follow-up.
 *
 * 동작:
 * - subscriptionTier 항상 FREE 반환
 * - 상품 목록은 빈 리스트
 * - launchSubscriptionFlow / launchPurchaseFlow 호출 시 PurchaseState.Error 로 안내
 * - queryExistingSubscriptions 는 no-op
 *
 * 후속:
 * - SKProductsRequest / SKProductsRequestDelegate 로 IAP 상품 메타데이터 로드
 * - SKPaymentQueue 옵저버 등록 후 결제 결과 PurchaseState 매핑
 * - Transaction.updates 모방 (SKPaymentTransactionObserver) → Pro 활성/만료 SubscriptionTier 갱신
 * - Server-side receipt validation (Supabase Edge Function 재사용)
 */
class IosBillingService : BillingService {

    private val _products = MutableStateFlow<List<DonationProduct>>(emptyList())
    override val products: StateFlow<List<DonationProduct>> = _products.asStateFlow()

    private val _subscriptionProducts = MutableStateFlow<List<SubscriptionProduct>>(emptyList())
    override val subscriptionProducts: StateFlow<List<SubscriptionProduct>> = _subscriptionProducts.asStateFlow()

    private val _purchaseState = MutableStateFlow<PurchaseState>(PurchaseState.Idle)
    override val purchaseState: StateFlow<PurchaseState> = _purchaseState.asStateFlow()

    private val _isConnected = MutableStateFlow(false)
    override val isConnected: StateFlow<Boolean> = _isConnected.asStateFlow()

    private val _subscriptionTier = MutableStateFlow(SubscriptionTier.FREE)
    override val subscriptionTier: StateFlow<SubscriptionTier> = _subscriptionTier.asStateFlow()

    override fun connect() {
        // StoreKit 풀 연결은 follow-up. 일단 connected 로만 표시해 SubscriptionScreen UI 가
        // "연결 중" 상태에서 멈추지 않도록.
        _isConnected.value = true
    }

    override fun disconnect() {
        _isConnected.value = false
    }

    override fun launchPurchaseFlow(activity: Any?, productId: String) {
        _purchaseState.value = PurchaseState.Error("iOS 결제는 곧 지원됩니다.")
    }

    override fun launchSubscriptionFlow(activity: Any?, productId: String) {
        _purchaseState.value = PurchaseState.Error("iOS 구독은 곧 지원됩니다.")
    }

    override fun queryExistingSubscriptions() {
        // no-op — 실 구현 시 SKPaymentQueue.default().restoreCompletedTransactions() 또는
        // Transaction.currentEntitlements 를 통해 현재 활성 구독 조회.
    }

    override fun isProductAvailable(productId: String): Boolean = false

    override fun resetPurchaseState() {
        _purchaseState.value = PurchaseState.Idle
    }
}

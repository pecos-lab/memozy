package me.pecos.memozy.platform.billing

import com.revenuecat.purchases.kmp.Purchases
import com.revenuecat.purchases.kmp.models.CustomerInfo
import com.revenuecat.purchases.kmp.models.Package
import com.revenuecat.purchases.kmp.models.Period
import com.revenuecat.purchases.kmp.models.PeriodUnit
import com.revenuecat.purchases.kmp.models.PurchasesErrorCode
import com.revenuecat.purchases.kmp.models.PurchasesException
import com.revenuecat.purchases.kmp.models.StoreProduct
import com.revenuecat.purchases.kmp.result.awaitCustomerInfoResult
import com.revenuecat.purchases.kmp.result.awaitGetProductsResult
import com.revenuecat.purchases.kmp.result.awaitLogInResult
import com.revenuecat.purchases.kmp.result.awaitLogOutResult
import com.revenuecat.purchases.kmp.result.awaitOfferingsResult
import com.revenuecat.purchases.kmp.result.awaitPurchaseResult
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import me.pecos.memozy.data.datasource.remote.auth.AuthState
import me.pecos.memozy.data.repository.user.AuthRepository
import me.pecos.memozy.presentation.theme.SubscriptionTier

/**
 * RevenueCat 기반 BillingService — Android/iOS 단일 구현.
 *
 * 사전 조건: [RevenueCatInitializer.configure] 가 앱 부트스트랩에서 먼저 호출되어야 함.
 *
 * Tier 매핑: entitlement [ENTITLEMENT_PRO] active 여부 → PRO/FREE.
 * 도네이션 5종은 entitlement 없이 일회성 구매로 추적.
 */
class RevenueCatBillingService(
    private val authRepository: AuthRepository,
) : BillingService {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private var authJob: Job? = null

    private val subscriptionPackages = mutableMapOf<String, Package>()
    private val donationProducts = mutableMapOf<String, StoreProduct>()

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
        if (!Purchases.isConfigured) {
            _isConnected.value = false
            return
        }
        _isConnected.value = true
        scope.launch { refreshOfferings() }
        scope.launch { refreshDonationProducts() }
        startAuthSync()
    }

    override fun disconnect() {
        authJob?.cancel()
        authJob = null
        _isConnected.value = false
    }

    override fun launchPurchaseFlow(activity: Any?, productId: String) {
        // RC 는 자체 ActivityLifecycleCallbacks 로 현재 Activity 추적 — activity 인자 무시.
        val product = donationProducts[productId]
        if (product == null) {
            _purchaseState.value = PurchaseState.Error("도네이션 상품 정보를 불러오지 못했습니다.")
            return
        }
        _purchaseState.value = PurchaseState.Loading
        scope.launch {
            Purchases.sharedInstance.awaitPurchaseResult(product)
                .onSuccess {
                    _purchaseState.value = PurchaseState.Success
                    refreshCustomerInfo()
                }
                .onFailure { handlePurchaseFailure(it) }
        }
    }

    override fun launchSubscriptionFlow(activity: Any?, productId: String) {
        val pkg = subscriptionPackages[productId]
        if (pkg == null) {
            _purchaseState.value = PurchaseState.Error("구독 상품 정보를 불러오지 못했습니다.")
            return
        }
        _purchaseState.value = PurchaseState.Loading
        scope.launch {
            Purchases.sharedInstance.awaitPurchaseResult(pkg)
                .onSuccess {
                    _purchaseState.value = PurchaseState.Success
                    refreshCustomerInfo()
                }
                .onFailure { handlePurchaseFailure(it) }
        }
    }

    override fun queryExistingSubscriptions() {
        scope.launch { refreshCustomerInfo() }
    }

    override fun isProductAvailable(productId: String): Boolean =
        donationProducts.containsKey(productId) || subscriptionPackages.containsKey(productId)

    override fun resetPurchaseState() {
        _purchaseState.value = PurchaseState.Idle
    }

    /**
     * Supabase 인증 상태 변화에 RC appUserId 를 자동 정렬.
     * Authenticated → logIn(userId) / Unauthenticated|Loading → logOut
     * (Loading 은 초기 부팅 시 잠깐 발생할 수 있어 logOut 으로 묶어 처리 — 익명 ID 발급)
     */
    private fun startAuthSync() {
        authJob?.cancel()
        authJob = scope.launch {
            authRepository.authState
                .map { state -> (state as? AuthState.Authenticated)?.user?.id }
                .distinctUntilChanged()
                .collect { userId -> syncAppUserId(userId) }
        }
    }

    private suspend fun syncAppUserId(userId: String?) {
        if (!Purchases.isConfigured) return
        if (userId != null) {
            Purchases.sharedInstance.awaitLogInResult(userId)
                .onSuccess { applyCustomerInfo(it.customerInfo) }
        } else {
            Purchases.sharedInstance.awaitLogOutResult()
                .onSuccess { applyCustomerInfo(it) }
        }
    }

    private suspend fun refreshOfferings() {
        Purchases.sharedInstance.awaitOfferingsResult()
            .onSuccess { offerings ->
                val current = offerings.current
                if (current == null) {
                    _subscriptionProducts.value = emptyList()
                    return@onSuccess
                }
                subscriptionPackages.clear()
                current.availablePackages.forEach { pkg ->
                    subscriptionPackages[pkg.storeProduct.id] = pkg
                }
                _subscriptionProducts.value = current.availablePackages.map { pkg ->
                    SubscriptionProduct(
                        productId = pkg.storeProduct.id,
                        name = pkg.storeProduct.title,
                        description = pkg.storeProduct.localizedDescription.orEmpty(),
                        formattedPrice = pkg.storeProduct.price.formatted,
                        billingPeriod = pkg.storeProduct.period?.toIso8601().orEmpty(),
                    )
                }
            }
    }

    private suspend fun refreshDonationProducts() {
        Purchases.sharedInstance.awaitGetProductsResult(DONATION_PRODUCT_IDS)
            .onSuccess { storeProducts ->
                donationProducts.clear()
                storeProducts.forEach { donationProducts[it.id] = it }
                _products.value = DONATION_PRODUCT_IDS.mapNotNull { id ->
                    donationProducts[id]?.let { product ->
                        DonationProduct(
                            productId = product.id,
                            name = product.title,
                            description = product.localizedDescription.orEmpty(),
                            formattedPrice = product.price.formatted,
                        )
                    }
                }
            }
    }

    private suspend fun refreshCustomerInfo() {
        Purchases.sharedInstance.awaitCustomerInfoResult()
            .onSuccess { applyCustomerInfo(it) }
    }

    private fun applyCustomerInfo(info: CustomerInfo) {
        _subscriptionTier.value = if (info.entitlements.active.containsKey(ENTITLEMENT_PRO)) {
            SubscriptionTier.PRO
        } else {
            SubscriptionTier.FREE
        }
    }

    private fun handlePurchaseFailure(error: Throwable) {
        val code = (error as? PurchasesException)?.error?.code
        _purchaseState.value = when (code) {
            PurchasesErrorCode.PurchaseCancelledError -> PurchaseState.Idle
            PurchasesErrorCode.NetworkError,
            PurchasesErrorCode.OfflineConnectionError -> PurchaseState.Error("네트워크 연결을 확인해주세요.")
            PurchasesErrorCode.PaymentPendingError -> PurchaseState.Error("결제가 보류 중입니다. 잠시 후 다시 확인해주세요.")
            PurchasesErrorCode.ProductNotAvailableForPurchaseError -> PurchaseState.Error("현재 구매할 수 없는 상품입니다.")
            PurchasesErrorCode.PurchaseNotAllowedError -> PurchaseState.Error("구매가 허용되지 않은 계정입니다.")
            PurchasesErrorCode.ProductAlreadyPurchasedError -> PurchaseState.Error("이미 구매한 상품입니다.")
            PurchasesErrorCode.StoreProblemError -> PurchaseState.Error("스토어 연결 문제가 발생했습니다.")
            PurchasesErrorCode.ReceiptAlreadyInUseError,
            PurchasesErrorCode.ReceiptInUseByOtherSubscriberError -> PurchaseState.Error("다른 계정에서 사용 중인 구독입니다.")
            else -> PurchaseState.Error(error.message.orEmpty().ifEmpty { "구매 실패" })
        }
    }

    companion object {
        const val ENTITLEMENT_PRO = "pro"

        val DONATION_PRODUCT_IDS = listOf(
            "donation_rice_1",
            "donation_rice_2",
            "donation_rice_3",
            "donation_rice_4",
            "donation_rice_5",
        )
    }
}

private fun Period.toIso8601(): String = when (unit) {
    PeriodUnit.DAY -> "P${value}D"
    PeriodUnit.WEEK -> "P${value}W"
    PeriodUnit.MONTH -> "P${value}M"
    PeriodUnit.YEAR -> "P${value}Y"
    PeriodUnit.UNKNOWN -> ""
}

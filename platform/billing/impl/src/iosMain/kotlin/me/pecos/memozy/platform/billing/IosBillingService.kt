package me.pecos.memozy.platform.billing

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import me.pecos.memozy.presentation.theme.SubscriptionTier
import platform.Foundation.NSError
import platform.Foundation.NSNumberFormatter
import platform.Foundation.NSUserDefaults
import platform.StoreKit.SKPayment
import platform.StoreKit.SKPaymentQueue
import platform.StoreKit.SKPaymentTransaction
import platform.StoreKit.SKPaymentTransactionObserverProtocol
import platform.StoreKit.SKPaymentTransactionState.*
import platform.StoreKit.SKProduct
import platform.StoreKit.SKProductPeriodUnit.*
import platform.StoreKit.SKProductsRequest
import platform.StoreKit.SKProductsRequestDelegateProtocol
import platform.StoreKit.SKProductsResponse
import platform.StoreKit.SKRequest
import platform.darwin.NSObject

private const val KEY_TIER = "billing_tier"

class IosBillingService : BillingService {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private val productMap = mutableMapOf<String, SKProduct>()
    private var productsDelegate: ProductsDelegate? = null
    private val observer = PaymentObserver(
        onPurchased = { productId -> handlePurchased(productId) },
        onRestored = { productId -> handlePurchased(productId) },
        onFailed = { error -> handleFailed(error) },
    )

    private val _products = MutableStateFlow<List<DonationProduct>>(emptyList())
    override val products: StateFlow<List<DonationProduct>> = _products.asStateFlow()

    private val _subscriptionProducts = MutableStateFlow<List<SubscriptionProduct>>(emptyList())
    override val subscriptionProducts: StateFlow<List<SubscriptionProduct>> = _subscriptionProducts.asStateFlow()

    private val _purchaseState = MutableStateFlow<PurchaseState>(PurchaseState.Idle)
    override val purchaseState: StateFlow<PurchaseState> = _purchaseState.asStateFlow()

    private val _isConnected = MutableStateFlow(false)
    override val isConnected: StateFlow<Boolean> = _isConnected.asStateFlow()

    private val _subscriptionTier = MutableStateFlow(loadCachedTier())
    override val subscriptionTier: StateFlow<SubscriptionTier> = _subscriptionTier.asStateFlow()

    override fun connect() {
        SKPaymentQueue.defaultQueue().addTransactionObserver(observer)
        _isConnected.value = true
        loadProducts()
        queryExistingSubscriptions()
    }

    override fun disconnect() {
        SKPaymentQueue.defaultQueue().removeTransactionObserver(observer)
        _isConnected.value = false
    }

    override fun launchSubscriptionFlow(activity: Any?, productId: String) {
        val product = productMap[productId] ?: run {
            _purchaseState.value = PurchaseState.Error("상품 정보를 불러오는 중입니다. 잠시 후 다시 시도해주세요.")
            return
        }
        _purchaseState.value = PurchaseState.Loading
        val payment = SKPayment.paymentWithProduct(product)
        SKPaymentQueue.defaultQueue().addPayment(payment)
    }

    override fun launchPurchaseFlow(activity: Any?, productId: String) {
        launchSubscriptionFlow(activity, productId)
    }

    override fun queryExistingSubscriptions() {
        SKPaymentQueue.defaultQueue().restoreCompletedTransactions()
    }

    override fun isProductAvailable(productId: String): Boolean =
        productMap.containsKey(productId)

    override fun resetPurchaseState() {
        _purchaseState.value = PurchaseState.Idle
    }

    private fun loadProducts() {
        val ids = setOf(BillingService.SUB_PRO_MONTHLY, BillingService.SUB_PRO_YEARLY)
        val delegate = ProductsDelegate(
            onLoaded = { products, _ ->
                scope.launch {
                    products.forEach { productMap[it.productIdentifier] = it }
                    _subscriptionProducts.value = products
                        .sortedBy { it.productIdentifier }
                        .map { it.toSubscriptionProduct() }
                }
                productsDelegate = null
            },
            onError = { _ ->
                productsDelegate = null
            }
        )
        productsDelegate = delegate
        val request = SKProductsRequest(productIdentifiers = ids)
        request.delegate = delegate
        delegate.retainRequest(request)
        request.start()
    }

    private fun handlePurchased(productId: String) {
        val subIds = setOf(BillingService.SUB_PRO_MONTHLY, BillingService.SUB_PRO_YEARLY)
        if (productId in subIds) {
            setTier(SubscriptionTier.PRO)
            _purchaseState.value = PurchaseState.Success
        }
    }

    private fun handleFailed(error: String?) {
        _purchaseState.value = if (error == null) PurchaseState.Idle
        else PurchaseState.Error(error)
    }

    private fun setTier(tier: SubscriptionTier) {
        _subscriptionTier.value = tier
        NSUserDefaults.standardUserDefaults.setObject(tier.name, KEY_TIER)
        NSUserDefaults.standardUserDefaults.synchronize()
    }

    private fun loadCachedTier(): SubscriptionTier {
        val cached = NSUserDefaults.standardUserDefaults.stringForKey(KEY_TIER)
        return if (cached == SubscriptionTier.PRO.name) SubscriptionTier.PRO else SubscriptionTier.FREE
    }
}

private fun SKProduct.toSubscriptionProduct(): SubscriptionProduct {
    val formatter = NSNumberFormatter()
    formatter.numberStyle = 2uL // NSNumberFormatterCurrencyStyle
    formatter.locale = priceLocale
    val formattedPrice = formatter.stringFromNumber(price) ?: price.stringValue
    val period = subscriptionPeriod?.let {
        when (it.unit) {
            SKProductPeriodUnitDay -> "P${it.numberOfUnits}D"
            SKProductPeriodUnitWeek -> "P${it.numberOfUnits}W"
            SKProductPeriodUnitMonth -> "P${it.numberOfUnits}M"
            SKProductPeriodUnitYear -> "P${it.numberOfUnits}Y"
            else -> ""
        }
    } ?: ""
    return SubscriptionProduct(
        productId = productIdentifier,
        name = localizedTitle,
        description = localizedDescription,
        formattedPrice = formattedPrice,
        billingPeriod = period,
    )
}

private class PaymentObserver(
    private val onPurchased: (String) -> Unit,
    private val onRestored: (String) -> Unit,
    private val onFailed: (String?) -> Unit,
) : NSObject(), SKPaymentTransactionObserverProtocol {

    @Suppress("UNCHECKED_CAST")
    override fun paymentQueue(queue: SKPaymentQueue, updatedTransactions: List<*>) {
        (updatedTransactions as List<SKPaymentTransaction>).forEach { tx ->
            when (tx.transactionState) {
                SKPaymentTransactionStatePurchased -> {
                    queue.finishTransaction(tx)
                    onPurchased(tx.payment.productIdentifier)
                }
                SKPaymentTransactionStateRestored -> {
                    queue.finishTransaction(tx)
                    tx.originalTransaction?.payment?.productIdentifier?.let { onRestored(it) }
                }
                SKPaymentTransactionStateFailed -> {
                    queue.finishTransaction(tx)
                    val isCancelled = tx.error?.code?.toInt() == 2
                    onFailed(if (isCancelled) null else tx.error?.localizedDescription)
                }
                SKPaymentTransactionStatePurchasing,
                SKPaymentTransactionStateDeferred -> { /* 진행 중 */ }
            }
        }
    }

    override fun paymentQueueRestoreCompletedTransactionsFinished(queue: SKPaymentQueue) {}

    override fun paymentQueue(
        queue: SKPaymentQueue,
        restoreCompletedTransactionsFailedWithError: NSError,
    ) {}
}

private class ProductsDelegate(
    private val onLoaded: (List<SKProduct>, List<String>) -> Unit,
    private val onError: (String) -> Unit = {},
) : NSObject(), SKProductsRequestDelegateProtocol {

    private var requestRef: SKProductsRequest? = null

    fun retainRequest(request: SKProductsRequest) {
        requestRef = request
    }

    @Suppress("UNCHECKED_CAST")
    override fun productsRequest(request: SKProductsRequest, didReceiveResponse: SKProductsResponse) {
        val products = didReceiveResponse.products as List<SKProduct>
        val invalid = (didReceiveResponse.invalidProductIdentifiers as List<String>)
        onLoaded(products, invalid)
        requestRef = null
    }

    override fun request(request: SKRequest, didFailWithError: NSError) {
        onError("${didFailWithError.localizedDescription} (code=${didFailWithError.code})")
        requestRef = null
    }
}

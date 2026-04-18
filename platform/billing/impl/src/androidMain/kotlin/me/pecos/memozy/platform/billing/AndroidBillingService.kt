package me.pecos.memozy.platform.billing

import android.app.Activity
import android.content.Context
import com.android.billingclient.api.AcknowledgePurchaseParams
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingFlowParams
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.ConsumeParams
import com.android.billingclient.api.PendingPurchasesParams
import com.android.billingclient.api.ProductDetails
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.PurchasesUpdatedListener
import com.android.billingclient.api.QueryProductDetailsParams
import com.android.billingclient.api.QueryPurchasesParams
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import me.pecos.memozy.presentation.theme.SubscriptionTier

fun provideBillingService(context: Context): BillingService = AndroidBillingService(context)

internal class AndroidBillingService(
    context: Context,
) : BillingService, PurchasesUpdatedListener {

    private val appContext = context.applicationContext

    private companion object {
        const val PRODUCT_RICE_1 = "donation_rice_1"
        const val PRODUCT_RICE_2 = "donation_rice_2"
        const val PRODUCT_RICE_3 = "donation_rice_3"
        const val PRODUCT_RICE_4 = "donation_rice_4"
        const val PRODUCT_RICE_5 = "donation_rice_5"

        val PRODUCT_IDS = listOf(
            PRODUCT_RICE_1,
            PRODUCT_RICE_2,
            PRODUCT_RICE_3,
            PRODUCT_RICE_4,
            PRODUCT_RICE_5,
        )

        val SUBSCRIPTION_IDS = listOf(
            BillingService.SUB_PRO_MONTHLY,
            BillingService.SUB_PRO_YEARLY,
        )
    }

    private val billingClient: BillingClient = BillingClient.newBuilder(appContext)
        .setListener(this)
        .enablePendingPurchases(
            PendingPurchasesParams.newBuilder()
                .enableOneTimeProducts()
                .enablePrepaidPlans()
                .build()
        )
        .build()

    private val productDetailsMap = mutableMapOf<String, ProductDetails>()
    private val subscriptionDetailsMap = mutableMapOf<String, ProductDetails>()

    private val _products = MutableStateFlow<List<DonationProduct>>(emptyList())
    override val products: StateFlow<List<DonationProduct>> = _products

    private val _subscriptionProducts = MutableStateFlow<List<SubscriptionProduct>>(emptyList())
    override val subscriptionProducts: StateFlow<List<SubscriptionProduct>> = _subscriptionProducts

    private val _purchaseState = MutableStateFlow<PurchaseState>(PurchaseState.Idle)
    override val purchaseState: StateFlow<PurchaseState> = _purchaseState

    private val _isConnected = MutableStateFlow(false)
    override val isConnected: StateFlow<Boolean> = _isConnected

    private val _subscriptionTier = MutableStateFlow(SubscriptionTier.FREE)
    override val subscriptionTier: StateFlow<SubscriptionTier> = _subscriptionTier

    override fun connect() {
        if (billingClient.isReady) return

        billingClient.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(billingResult: BillingResult) {
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                    _isConnected.value = true
                    queryProducts()
                    querySubscriptions()
                    queryExistingSubscriptions()
                } else {
                    _isConnected.value = false
                }
            }

            override fun onBillingServiceDisconnected() {
                _isConnected.value = false
                connect()
            }
        })
    }

    private fun queryProducts() {
        val productList = PRODUCT_IDS.map { productId ->
            QueryProductDetailsParams.Product.newBuilder()
                .setProductId(productId)
                .setProductType(BillingClient.ProductType.INAPP)
                .build()
        }

        val params = QueryProductDetailsParams.newBuilder()
            .setProductList(productList)
            .build()

        billingClient.queryProductDetailsAsync(params) { billingResult, productDetailsList ->
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                productDetailsMap.clear()
                productDetailsList.forEach { productDetailsMap[it.productId] = it }

                val donationProducts = PRODUCT_IDS.mapNotNull { id ->
                    productDetailsMap[id]?.let { details ->
                        DonationProduct(
                            productId = details.productId,
                            name = details.name,
                            description = details.description,
                            formattedPrice = details.oneTimePurchaseOfferDetails?.formattedPrice ?: "",
                        )
                    }
                }
                _products.value = donationProducts
            }
        }
    }

    override fun launchPurchaseFlow(activity: Any?, productId: String) {
        val androidActivity = activity as? Activity ?: return
        val details = productDetailsMap[productId] ?: return
        _purchaseState.value = PurchaseState.Loading

        val productDetailsParamsList = listOf(
            BillingFlowParams.ProductDetailsParams.newBuilder()
                .setProductDetails(details)
                .build()
        )

        val billingFlowParams = BillingFlowParams.newBuilder()
            .setProductDetailsParamsList(productDetailsParamsList)
            .build()

        billingClient.launchBillingFlow(androidActivity, billingFlowParams)
    }

    override fun isProductAvailable(productId: String): Boolean =
        productDetailsMap.containsKey(productId)

    private fun querySubscriptions() {
        val subList = SUBSCRIPTION_IDS.map { subId ->
            QueryProductDetailsParams.Product.newBuilder()
                .setProductId(subId)
                .setProductType(BillingClient.ProductType.SUBS)
                .build()
        }

        val params = QueryProductDetailsParams.newBuilder()
            .setProductList(subList)
            .build()

        billingClient.queryProductDetailsAsync(params) { billingResult, productDetailsList ->
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                subscriptionDetailsMap.clear()
                productDetailsList.forEach { subscriptionDetailsMap[it.productId] = it }

                val subProducts = SUBSCRIPTION_IDS.mapNotNull { id ->
                    subscriptionDetailsMap[id]?.let { details ->
                        val offerDetails = details.subscriptionOfferDetails?.firstOrNull()
                        val pricingPhase = offerDetails?.pricingPhases?.pricingPhaseList?.firstOrNull()
                        SubscriptionProduct(
                            productId = details.productId,
                            name = details.name,
                            description = details.description,
                            formattedPrice = pricingPhase?.formattedPrice ?: "",
                            billingPeriod = pricingPhase?.billingPeriod ?: "",
                        )
                    }
                }
                _subscriptionProducts.value = subProducts
            }
        }
    }

    override fun launchSubscriptionFlow(activity: Any?, productId: String) {
        val androidActivity = activity as? Activity ?: return
        val details = subscriptionDetailsMap[productId] ?: return
        val offerToken = details.subscriptionOfferDetails?.firstOrNull()?.offerToken ?: return
        _purchaseState.value = PurchaseState.Loading

        val productDetailsParams = BillingFlowParams.ProductDetailsParams.newBuilder()
            .setProductDetails(details)
            .setOfferToken(offerToken)
            .build()

        val billingFlowParams = BillingFlowParams.newBuilder()
            .setProductDetailsParamsList(listOf(productDetailsParams))
            .build()

        billingClient.launchBillingFlow(androidActivity, billingFlowParams)
    }

    override fun queryExistingSubscriptions() {
        billingClient.queryPurchasesAsync(
            QueryPurchasesParams.newBuilder()
                .setProductType(BillingClient.ProductType.SUBS)
                .build()
        ) { billingResult, purchases ->
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                val hasActiveSub = purchases.any { purchase ->
                    purchase.purchaseState == Purchase.PurchaseState.PURCHASED &&
                        purchase.products.any { it in SUBSCRIPTION_IDS }
                }
                _subscriptionTier.value = if (hasActiveSub) SubscriptionTier.PRO else SubscriptionTier.FREE

                purchases.filter {
                    it.purchaseState == Purchase.PurchaseState.PURCHASED && !it.isAcknowledged
                }.forEach { acknowledgePurchase(it) }
            }
        }
    }

    override fun onPurchasesUpdated(
        billingResult: BillingResult,
        purchases: MutableList<Purchase>?,
    ) {
        when (billingResult.responseCode) {
            BillingClient.BillingResponseCode.OK -> {
                purchases?.forEach { purchase ->
                    if (purchase.purchaseState == Purchase.PurchaseState.PURCHASED) {
                        val isSubscription = purchase.products.any { it in SUBSCRIPTION_IDS }
                        if (isSubscription) {
                            acknowledgePurchase(purchase)
                            _subscriptionTier.value = SubscriptionTier.PRO
                            _purchaseState.value = PurchaseState.Success
                        } else {
                            consumePurchase(purchase)
                        }
                    }
                }
            }
            BillingClient.BillingResponseCode.USER_CANCELED -> {
                _purchaseState.value = PurchaseState.Idle
            }
            else -> {
                _purchaseState.value = PurchaseState.Error(billingResult.debugMessage)
            }
        }
    }

    private fun consumePurchase(purchase: Purchase) {
        val consumeParams = ConsumeParams.newBuilder()
            .setPurchaseToken(purchase.purchaseToken)
            .build()

        billingClient.consumeAsync(consumeParams) { billingResult, _ ->
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                _purchaseState.value = PurchaseState.Success
            } else {
                _purchaseState.value = PurchaseState.Error(billingResult.debugMessage)
            }
        }
    }

    private fun acknowledgePurchase(purchase: Purchase) {
        if (purchase.isAcknowledged) return
        val params = AcknowledgePurchaseParams.newBuilder()
            .setPurchaseToken(purchase.purchaseToken)
            .build()
        billingClient.acknowledgePurchase(params) { /* no-op */ }
    }

    override fun resetPurchaseState() {
        _purchaseState.value = PurchaseState.Idle
    }

    override fun disconnect() {
        billingClient.endConnection()
    }
}

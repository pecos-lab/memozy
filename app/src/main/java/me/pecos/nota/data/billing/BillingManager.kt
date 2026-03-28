package me.pecos.nota.data.billing

import android.app.Activity
import android.content.Context
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
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

data class DonationProduct(
    val productId: String,
    val name: String,
    val description: String,
    val formattedPrice: String,
    val productDetails: ProductDetails? = null
)

sealed class PurchaseState {
    data object Idle : PurchaseState()
    data object Loading : PurchaseState()
    data object Success : PurchaseState()
    data class Error(val message: String) : PurchaseState()
}

class BillingManager(
    private val context: Context
) : PurchasesUpdatedListener {

    companion object {
        private const val PRODUCT_RICE_1 = "donation_rice_1"
        private const val PRODUCT_RICE_2 = "donation_rice_2"
        private const val PRODUCT_RICE_3 = "donation_rice_3"
        private const val PRODUCT_RICE_4 = "donation_rice_4"
        private const val PRODUCT_RICE_5 = "donation_rice_5"

        val PRODUCT_IDS = listOf(
            PRODUCT_RICE_1,
            PRODUCT_RICE_2,
            PRODUCT_RICE_3,
            PRODUCT_RICE_4,
            PRODUCT_RICE_5
        )
    }

    private val billingClient: BillingClient = BillingClient.newBuilder(context)
        .setListener(this)
        .enablePendingPurchases(
            PendingPurchasesParams.newBuilder()
                .enableOneTimeProducts()
                .build()
        )
        .build()

    private val _products = MutableStateFlow<List<DonationProduct>>(emptyList())
    val products: StateFlow<List<DonationProduct>> = _products

    private val _purchaseState = MutableStateFlow<PurchaseState>(PurchaseState.Idle)
    val purchaseState: StateFlow<PurchaseState> = _purchaseState

    private val _isConnected = MutableStateFlow(false)
    val isConnected: StateFlow<Boolean> = _isConnected

    fun connect() {
        billingClient.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(billingResult: BillingResult) {
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                    _isConnected.value = true
                    queryProducts()
                } else {
                    _isConnected.value = false
                }
            }

            override fun onBillingServiceDisconnected() {
                _isConnected.value = false
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
                val donationProducts = productDetailsList.map { details ->
                    DonationProduct(
                        productId = details.productId,
                        name = details.name,
                        description = details.description,
                        formattedPrice = details.oneTimePurchaseOfferDetails?.formattedPrice ?: "",
                        productDetails = details
                    )
                }.sortedBy { PRODUCT_IDS.indexOf(it.productId) }
                _products.value = donationProducts
            }
        }
    }

    fun launchPurchaseFlow(activity: Activity, productDetails: ProductDetails) {
        _purchaseState.value = PurchaseState.Loading

        val productDetailsParamsList = listOf(
            BillingFlowParams.ProductDetailsParams.newBuilder()
                .setProductDetails(productDetails)
                .build()
        )

        val billingFlowParams = BillingFlowParams.newBuilder()
            .setProductDetailsParamsList(productDetailsParamsList)
            .build()

        billingClient.launchBillingFlow(activity, billingFlowParams)
    }

    override fun onPurchasesUpdated(
        billingResult: BillingResult,
        purchases: MutableList<Purchase>?
    ) {
        when (billingResult.responseCode) {
            BillingClient.BillingResponseCode.OK -> {
                purchases?.forEach { purchase ->
                    if (purchase.purchaseState == Purchase.PurchaseState.PURCHASED) {
                        consumePurchase(purchase)
                    }
                }
            }
            BillingClient.BillingResponseCode.USER_CANCELED -> {
                _purchaseState.value = PurchaseState.Idle
            }
            else -> {
                _purchaseState.value = PurchaseState.Error(
                    billingResult.debugMessage
                )
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

    fun resetPurchaseState() {
        _purchaseState.value = PurchaseState.Idle
    }

    fun disconnect() {
        billingClient.endConnection()
    }
}

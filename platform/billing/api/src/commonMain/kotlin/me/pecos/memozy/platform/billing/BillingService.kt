package me.pecos.memozy.platform.billing

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

    fun connect()
    fun disconnect()
    fun launchPurchaseFlow(activity: Any?, productId: String)
    fun launchSubscriptionFlow(activity: Any?, productId: String)
    fun queryExistingSubscriptions()
    fun isProductAvailable(productId: String): Boolean
    fun resetPurchaseState()

    companion object {
        const val SUB_PRO_MONTHLY = "pro_monthly"
        const val SUB_PRO_YEARLY = "pro_yearly"
    }
}

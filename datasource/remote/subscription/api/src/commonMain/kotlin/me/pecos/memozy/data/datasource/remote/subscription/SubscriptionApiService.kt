package me.pecos.memozy.data.datasource.remote.subscription

import me.pecos.memozy.presentation.theme.SubscriptionTier

data class ValidatePurchaseRequest(
    val userId: String,
    val productId: String,
    val purchaseToken: String,
    val platform: String, // "android" or "ios"
)

data class ValidatePurchaseResponse(
    val success: Boolean,
    val subscription: SubscriptionData?,
    val error: String? = null,
)

data class SyncSubscriptionRequest(
    val userId: String,
)

data class SubscriptionData(
    val tier: SubscriptionTier,
    val productId: String?,
    val expiresAt: String?,
    val autoRenewing: Boolean,
    val inGracePeriod: Boolean = false,
)

interface SubscriptionApiService {
    /**
     * Validate a purchase with the server
     * This should be called after a successful in-app purchase
     */
    suspend fun validatePurchase(request: ValidatePurchaseRequest): ValidatePurchaseResponse

    /**
     * Sync subscription status from server
     * This should be called on app launch to ensure subscription state is up-to-date
     */
    suspend fun syncSubscription(request: SyncSubscriptionRequest): SubscriptionData
}

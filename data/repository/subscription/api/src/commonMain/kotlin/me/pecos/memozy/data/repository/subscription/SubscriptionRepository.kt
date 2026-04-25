package me.pecos.memozy.data.repository.subscription

import me.pecos.memozy.presentation.theme.SubscriptionTier

interface SubscriptionRepository {
    /**
     * Validate a purchase with the server after successful in-app purchase
     * @param userId User ID from auth
     * @param productId Product ID (e.g., "pro_monthly")
     * @param purchaseToken Purchase token from Google Play or App Store
     * @param platform Platform: "android" or "ios"
     * @return Result with subscription data or error
     */
    suspend fun validatePurchase(
        userId: String,
        productId: String,
        purchaseToken: String,
        platform: String,
    ): Result<SubscriptionTier>

    /**
     * Sync subscription status from server
     * Should be called on app launch to ensure local state matches server
     * @param userId User ID from auth
     * @return Result with current subscription tier
     */
    suspend fun syncSubscriptionStatus(userId: String): Result<SubscriptionTier>
}

package me.pecos.memozy.data.repository.subscription

import me.pecos.memozy.data.datasource.remote.subscription.SubscriptionApiService
import me.pecos.memozy.data.datasource.remote.subscription.SyncSubscriptionRequest
import me.pecos.memozy.data.datasource.remote.subscription.ValidatePurchaseRequest
import me.pecos.memozy.presentation.theme.SubscriptionTier

class SubscriptionRepositoryImpl(
    private val subscriptionApiService: SubscriptionApiService,
) : SubscriptionRepository {

    override suspend fun validatePurchase(
        userId: String,
        productId: String,
        purchaseToken: String,
        platform: String,
    ): Result<SubscriptionTier> = runCatching {
        val request = ValidatePurchaseRequest(
            userId = userId,
            productId = productId,
            purchaseToken = purchaseToken,
            platform = platform,
        )

        val response = subscriptionApiService.validatePurchase(request)

        if (response.success && response.subscription != null) {
            response.subscription.tier
        } else {
            throw Exception(response.error ?: "Purchase validation failed")
        }
    }

    override suspend fun syncSubscriptionStatus(userId: String): Result<SubscriptionTier> = runCatching {
        val request = SyncSubscriptionRequest(userId = userId)
        val subscriptionData = subscriptionApiService.syncSubscription(request)
        subscriptionData.tier
    }
}

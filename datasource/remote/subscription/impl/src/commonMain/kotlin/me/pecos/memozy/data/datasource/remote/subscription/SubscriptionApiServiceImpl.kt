package me.pecos.memozy.data.datasource.remote.subscription

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import me.pecos.memozy.presentation.theme.SubscriptionTier

@Serializable
private data class ValidatePurchaseApiRequest(
    val userId: String,
    val productId: String,
    val purchaseToken: String,
    val platform: String,
)

@Serializable
private data class ValidatePurchaseApiResponse(
    val success: Boolean,
    val subscription: SubscriptionApiData? = null,
    val error: String? = null,
)

@Serializable
private data class SubscriptionApiData(
    val tier: String,
    val expiresAt: String? = null,
    val autoRenewing: Boolean,
)

@Serializable
private data class SyncSubscriptionApiRequest(
    val userId: String,
)

@Serializable
private data class SyncSubscriptionApiResponse(
    val tier: String,
    val productId: String? = null,
    val expiresAt: String? = null,
    val autoRenewing: Boolean,
    val inGracePeriod: Boolean,
)

class SubscriptionApiServiceImpl(
    private val httpClient: HttpClient,
    private val json: Json,
) : SubscriptionApiService {

    override suspend fun validatePurchase(request: ValidatePurchaseRequest): ValidatePurchaseResponse {
        val apiRequest = ValidatePurchaseApiRequest(
            userId = request.userId,
            productId = request.productId,
            purchaseToken = request.purchaseToken,
            platform = request.platform,
        )

        val response: ValidatePurchaseApiResponse = httpClient.post("validate-purchase") {
            contentType(ContentType.Application.Json)
            setBody(apiRequest)
        }.body()

        return ValidatePurchaseResponse(
            success = response.success,
            subscription = response.subscription?.let {
                SubscriptionData(
                    tier = if (it.tier == "PRO") SubscriptionTier.PRO else SubscriptionTier.FREE,
                    productId = request.productId,
                    expiresAt = it.expiresAt,
                    autoRenewing = it.autoRenewing,
                )
            },
            error = response.error,
        )
    }

    override suspend fun syncSubscription(request: SyncSubscriptionRequest): SubscriptionData {
        val apiRequest = SyncSubscriptionApiRequest(
            userId = request.userId,
        )

        val response: SyncSubscriptionApiResponse = httpClient.post("sync-subscription") {
            contentType(ContentType.Application.Json)
            setBody(apiRequest)
        }.body()

        return SubscriptionData(
            tier = if (response.tier == "PRO") SubscriptionTier.PRO else SubscriptionTier.FREE,
            productId = response.productId,
            expiresAt = response.expiresAt,
            autoRenewing = response.autoRenewing,
            inGracePeriod = response.inGracePeriod,
        )
    }
}

package me.pecos.memozy.data.repository.subscription

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import me.pecos.memozy.data.datasource.remote.subscription.SubscriptionApiService
import me.pecos.memozy.data.datasource.remote.subscription.SubscriptionData
import me.pecos.memozy.data.datasource.remote.subscription.SyncSubscriptionRequest
import me.pecos.memozy.data.datasource.remote.subscription.ValidatePurchaseRequest
import me.pecos.memozy.data.datasource.remote.subscription.ValidatePurchaseResponse
import me.pecos.memozy.presentation.theme.SubscriptionTier
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class SubscriptionRepositoryImplTest {

    private lateinit var subscriptionApiService: SubscriptionApiService
    private lateinit var repository: SubscriptionRepositoryImpl

    @Before
    fun setup() {
        subscriptionApiService = mockk()
        repository = SubscriptionRepositoryImpl(subscriptionApiService)
    }

    @Test
    fun `validatePurchase success returns PRO tier`() = runTest {
        // Given
        val userId = "test-user-id"
        val productId = "pro_monthly"
        val purchaseToken = "test-token"
        val platform = "android"

        val expectedResponse = ValidatePurchaseResponse(
            success = true,
            subscription = SubscriptionData(
                tier = SubscriptionTier.PRO,
                productId = productId,
                expiresAt = "2026-05-22T00:00:00Z",
                autoRenewing = true,
                inGracePeriod = false
            )
        )

        coEvery {
            subscriptionApiService.validatePurchase(
                ValidatePurchaseRequest(
                    userId = userId,
                    productId = productId,
                    purchaseToken = purchaseToken,
                    platform = platform
                )
            )
        } returns expectedResponse

        // When
        val result = repository.validatePurchase(userId, productId, purchaseToken, platform)

        // Then
        assertTrue(result.isSuccess)
        assertEquals(SubscriptionTier.PRO, result.getOrNull())
        coVerify(exactly = 1) {
            subscriptionApiService.validatePurchase(any())
        }
    }

    @Test
    fun `validatePurchase failure returns error`() = runTest {
        // Given
        val userId = "test-user-id"
        val productId = "pro_monthly"
        val purchaseToken = "invalid-token"
        val platform = "android"

        val expectedResponse = ValidatePurchaseResponse(
            success = false,
            subscription = null,
            error = "Invalid purchase token"
        )

        coEvery {
            subscriptionApiService.validatePurchase(any())
        } returns expectedResponse

        // When
        val result = repository.validatePurchase(userId, productId, purchaseToken, platform)

        // Then
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull()?.message?.contains("Invalid purchase token") == true)
    }

    @Test
    fun `validatePurchase network error returns failure`() = runTest {
        // Given
        val userId = "test-user-id"
        val productId = "pro_monthly"
        val purchaseToken = "test-token"
        val platform = "android"

        coEvery {
            subscriptionApiService.validatePurchase(any())
        } throws Exception("Network error")

        // When
        val result = repository.validatePurchase(userId, productId, purchaseToken, platform)

        // Then
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull()?.message?.contains("Network error") == true)
    }

    @Test
    fun `syncSubscriptionStatus success returns current tier`() = runTest {
        // Given
        val userId = "test-user-id"
        val expectedData = SubscriptionData(
            tier = SubscriptionTier.PRO,
            productId = "pro_yearly",
            expiresAt = "2027-01-01T00:00:00Z",
            autoRenewing = true,
            inGracePeriod = false
        )

        coEvery {
            subscriptionApiService.syncSubscription(
                SyncSubscriptionRequest(userId = userId)
            )
        } returns expectedData

        // When
        val result = repository.syncSubscriptionStatus(userId)

        // Then
        assertTrue(result.isSuccess)
        assertEquals(SubscriptionTier.PRO, result.getOrNull())
        coVerify(exactly = 1) {
            subscriptionApiService.syncSubscription(any())
        }
    }

    @Test
    fun `syncSubscriptionStatus returns FREE tier when not subscribed`() = runTest {
        // Given
        val userId = "test-user-id"
        val expectedData = SubscriptionData(
            tier = SubscriptionTier.FREE,
            productId = null,
            expiresAt = null,
            autoRenewing = false,
            inGracePeriod = false
        )

        coEvery {
            subscriptionApiService.syncSubscription(any())
        } returns expectedData

        // When
        val result = repository.syncSubscriptionStatus(userId)

        // Then
        assertTrue(result.isSuccess)
        assertEquals(SubscriptionTier.FREE, result.getOrNull())
    }

    @Test
    fun `syncSubscriptionStatus network failure returns error`() = runTest {
        // Given
        val userId = "test-user-id"

        coEvery {
            subscriptionApiService.syncSubscription(any())
        } throws Exception("Server timeout")

        // When
        val result = repository.syncSubscriptionStatus(userId)

        // Then
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull()?.message?.contains("Server timeout") == true)
    }
}

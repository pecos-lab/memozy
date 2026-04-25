package me.pecos.memozy.data.datasource.remote.subscription

import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import me.pecos.memozy.presentation.theme.SubscriptionTier
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SubscriptionApiServiceImplTest {

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        encodeDefaults = true
    }

    @Test
    fun `validatePurchase success returns PRO subscription`() = runTest {
        // Given
        val mockEngine = MockEngine { request ->
            respond(
                content = """
                    {
                        "success": true,
                        "subscription": {
                            "tier": "PRO",
                            "expiresAt": "2026-05-22T00:00:00Z",
                            "autoRenewing": true
                        }
                    }
                """.trimIndent(),
                status = HttpStatusCode.OK,
                headers = headersOf(HttpHeaders.ContentType, "application/json")
            )
        }

        val httpClient = HttpClient(mockEngine) {
            install(ContentNegotiation) {
                json(json)
            }
        }

        val service = SubscriptionApiServiceImpl(httpClient, json)

        // When
        val result = service.validatePurchase(
            ValidatePurchaseRequest(
                userId = "test-user",
                productId = "pro_monthly",
                purchaseToken = "test-token",
                platform = "android"
            )
        )

        // Then
        assertTrue(result.success)
        assertEquals(SubscriptionTier.PRO, result.subscription?.tier)
        assertEquals(true, result.subscription?.autoRenewing)
    }

    @Test
    fun `validatePurchase failure returns error message`() = runTest {
        // Given
        val mockEngine = MockEngine { request ->
            respond(
                content = """
                    {
                        "success": false,
                        "error": "Invalid purchase token"
                    }
                """.trimIndent(),
                status = HttpStatusCode.OK,
                headers = headersOf(HttpHeaders.ContentType, "application/json")
            )
        }

        val httpClient = HttpClient(mockEngine) {
            install(ContentNegotiation) {
                json(json)
            }
        }

        val service = SubscriptionApiServiceImpl(httpClient, json)

        // When
        val result = service.validatePurchase(
            ValidatePurchaseRequest(
                userId = "test-user",
                productId = "pro_monthly",
                purchaseToken = "invalid-token",
                platform = "android"
            )
        )

        // Then
        assertFalse(result.success)
        assertEquals("Invalid purchase token", result.error)
    }

    @Test
    fun `validatePurchase HTTP 401 throws exception`() = runTest {
        // Given
        val mockEngine = MockEngine { request ->
            respond(
                content = """{"error": "Unauthorized"}""",
                status = HttpStatusCode.Unauthorized,
                headers = headersOf(HttpHeaders.ContentType, "application/json")
            )
        }

        val httpClient = HttpClient(mockEngine) {
            install(ContentNegotiation) {
                json(json)
            }
        }

        val service = SubscriptionApiServiceImpl(httpClient, json)

        // When/Then
        try {
            service.validatePurchase(
                ValidatePurchaseRequest(
                    userId = "test-user",
                    productId = "pro_monthly",
                    purchaseToken = "test-token",
                    platform = "android"
                )
            )
            assertTrue("Should have thrown exception", false)
        } catch (e: Exception) {
            // Expected
            assertTrue(true)
        }
    }

    @Test
    fun `syncSubscription returns PRO tier`() = runTest {
        // Given
        val mockEngine = MockEngine { request ->
            respond(
                content = """
                    {
                        "tier": "PRO",
                        "productId": "pro_yearly",
                        "expiresAt": "2027-01-01T00:00:00Z",
                        "autoRenewing": true,
                        "inGracePeriod": false
                    }
                """.trimIndent(),
                status = HttpStatusCode.OK,
                headers = headersOf(HttpHeaders.ContentType, "application/json")
            )
        }

        val httpClient = HttpClient(mockEngine) {
            install(ContentNegotiation) {
                json(json)
            }
        }

        val service = SubscriptionApiServiceImpl(httpClient, json)

        // When
        val result = service.syncSubscription(
            SyncSubscriptionRequest(userId = "test-user")
        )

        // Then
        assertEquals(SubscriptionTier.PRO, result.tier)
        assertEquals("pro_yearly", result.productId)
        assertEquals(true, result.autoRenewing)
        assertEquals(false, result.inGracePeriod)
    }

    @Test
    fun `syncSubscription returns FREE tier`() = runTest {
        // Given
        val mockEngine = MockEngine { request ->
            respond(
                content = """
                    {
                        "tier": "FREE",
                        "productId": null,
                        "expiresAt": null,
                        "autoRenewing": false,
                        "inGracePeriod": false
                    }
                """.trimIndent(),
                status = HttpStatusCode.OK,
                headers = headersOf(HttpHeaders.ContentType, "application/json")
            )
        }

        val httpClient = HttpClient(mockEngine) {
            install(ContentNegotiation) {
                json(json)
            }
        }

        val service = SubscriptionApiServiceImpl(httpClient, json)

        // When
        val result = service.syncSubscription(
            SyncSubscriptionRequest(userId = "test-user")
        )

        // Then
        assertEquals(SubscriptionTier.FREE, result.tier)
        assertEquals(null, result.productId)
        assertEquals(false, result.autoRenewing)
    }

    @Test
    fun `syncSubscription HTTP 500 throws exception`() = runTest {
        // Given
        val mockEngine = MockEngine { request ->
            respond(
                content = """{"error": "Internal server error"}""",
                status = HttpStatusCode.InternalServerError,
                headers = headersOf(HttpHeaders.ContentType, "application/json")
            )
        }

        val httpClient = HttpClient(mockEngine) {
            install(ContentNegotiation) {
                json(json)
            }
        }

        val service = SubscriptionApiServiceImpl(httpClient, json)

        // When/Then
        try {
            service.syncSubscription(
                SyncSubscriptionRequest(userId = "test-user")
            )
            assertTrue("Should have thrown exception", false)
        } catch (e: Exception) {
            // Expected
            assertTrue(true)
        }
    }
}

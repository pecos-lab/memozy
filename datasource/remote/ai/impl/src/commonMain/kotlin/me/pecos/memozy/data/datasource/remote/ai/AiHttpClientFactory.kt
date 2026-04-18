package me.pecos.memozy.data.datasource.remote.ai

import io.ktor.client.HttpClient
import io.ktor.client.plugins.HttpResponseValidator
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.cookies.AcceptAllCookiesStorage
import io.ktor.client.plugins.cookies.HttpCookies
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.request.header
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpStatusCode
import io.ktor.http.isSuccess
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

fun createAiHttpClient(
    json: Json,
    baseUrl: String,
    appSecretKey: String,
    isDebug: Boolean,
): HttpClient = createPlatformHttpClient {
    install(ContentNegotiation) {
        json(json)
    }

    install(Logging) {
        level = if (isDebug) LogLevel.HEADERS else LogLevel.NONE
    }

    install(HttpTimeout) {
        requestTimeoutMillis = 60_000
        connectTimeoutMillis = 15_000
        socketTimeoutMillis = 60_000
    }

    defaultRequest {
        url("$baseUrl/")
        header("x-app-key", appSecretKey)
    }

    HttpResponseValidator {
        validateResponse { response ->
            if (!response.status.isSuccess()) {
                val body = response.bodyAsText()
                throw when (response.status) {
                    HttpStatusCode.Unauthorized -> AIException.AuthenticationException()
                    HttpStatusCode.TooManyRequests -> AIException.RateLimitException()
                    else -> AIException.ServerException(
                        statusCode = response.status.value,
                        message = body,
                    )
                }
            }
        }

        handleResponseExceptionWithRequest { cause, _ ->
            when (cause) {
                is AIException -> throw cause
                else -> throw AIException.NetworkException(
                    message = cause.message ?: "Unknown network error",
                    cause = cause,
                )
            }
        }
    }
}

fun createYouTubeHttpClient(): HttpClient = createPlatformHttpClient {
    install(HttpTimeout) {
        requestTimeoutMillis = 60_000
        connectTimeoutMillis = 15_000
        socketTimeoutMillis = 60_000
    }
    install(HttpCookies) {
        storage = AcceptAllCookiesStorage()
    }
}

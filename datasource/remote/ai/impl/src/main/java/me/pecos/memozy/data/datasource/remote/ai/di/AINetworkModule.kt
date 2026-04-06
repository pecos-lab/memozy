package me.pecos.memozy.data.datasource.remote.ai.di

import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.HttpResponseValidator
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpStatusCode
import io.ktor.http.isSuccess
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import me.pecos.memozy.data.datasource.remote.ai.AIApiService
import me.pecos.memozy.data.datasource.remote.ai.AIApiServiceImpl
import me.pecos.memozy.data.datasource.remote.ai.AIException
import me.pecos.memozy.data.datasource.remote.ai.YouTubeCaptionService
import me.pecos.memozy.data.datasource.remote.ai.YouTubeCaptionServiceImpl
import javax.inject.Qualifier
import me.pecos.memozy.datasource.remote.ai.impl.BuildConfig
import javax.inject.Singleton

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class YouTubeHttpClient

@Module
@InstallIn(SingletonComponent::class)
abstract class AINetworkModule {

    @Binds
    @Singleton
    abstract fun bindAIApiService(impl: AIApiServiceImpl): AIApiService

    @Binds
    @Singleton
    abstract fun bindYouTubeCaptionService(impl: YouTubeCaptionServiceImpl): YouTubeCaptionService

    companion object {

        @Provides
        @Singleton
        fun provideJson(): Json = Json {
            ignoreUnknownKeys = true
            isLenient = true
            encodeDefaults = true
        }

        @Provides
        @Singleton
        fun provideHttpClient(json: Json): HttpClient = HttpClient(OkHttp) {
            install(ContentNegotiation) {
                json(json)
            }

            install(Logging) {
                level = LogLevel.BODY
            }

            install(HttpTimeout) {
                requestTimeoutMillis = 120_000
                connectTimeoutMillis = 15_000
                socketTimeoutMillis = 120_000
            }

            defaultRequest {
                url(BuildConfig.AI_BASE_URL)
            }

            HttpResponseValidator {
                validateResponse { response ->
                    if (!response.status.isSuccess()) {
                        val body = response.bodyAsText()
                        throw when (response.status) {
                            HttpStatusCode.Unauthorized -> AIException.AuthenticationException()
                            HttpStatusCode.TooManyRequests -> {
                                AIException.RateLimitException()
                            }
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
        @Provides
        @Singleton
        @YouTubeHttpClient
        fun provideYouTubeHttpClient(): HttpClient = HttpClient(OkHttp) {
            install(HttpTimeout) {
                requestTimeoutMillis = 15_000
                connectTimeoutMillis = 10_000
                socketTimeoutMillis = 15_000
            }
        }
    }
}

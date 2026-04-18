package me.pecos.memozy.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import io.ktor.client.HttpClient
import kotlinx.serialization.json.Json
import me.pecos.memozy.BuildConfig
import me.pecos.memozy.data.datasource.remote.ai.AIApiService
import me.pecos.memozy.data.datasource.remote.ai.AIApiServiceImpl
import me.pecos.memozy.data.datasource.remote.ai.WebScrapeService
import me.pecos.memozy.data.datasource.remote.ai.WebScrapeServiceImpl
import me.pecos.memozy.data.datasource.remote.ai.YouTubeCaptionService
import me.pecos.memozy.data.datasource.remote.ai.YouTubeCaptionServiceImpl
import me.pecos.memozy.data.datasource.remote.ai.createAiHttpClient
import me.pecos.memozy.data.datasource.remote.ai.createYouTubeHttpClient
import javax.inject.Qualifier
import javax.inject.Singleton

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class YouTubeHttpClient

@Module
@InstallIn(SingletonComponent::class)
object AiNetworkModule {

    @Provides
    @Singleton
    fun provideJson(): Json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        encodeDefaults = true
    }

    @Provides
    @Singleton
    fun provideHttpClient(json: Json): HttpClient = createAiHttpClient(
        json = json,
        baseUrl = BuildConfig.WORKER_URL,
        appSecretKey = BuildConfig.APP_SECRET_KEY,
        isDebug = BuildConfig.DEBUG,
    )

    @Provides
    @Singleton
    @YouTubeHttpClient
    fun provideYouTubeHttpClient(): HttpClient = createYouTubeHttpClient()

    @Provides
    @Singleton
    fun provideAIApiService(
        httpClient: HttpClient,
        json: Json,
    ): AIApiService = AIApiServiceImpl(httpClient, json)

    @Provides
    @Singleton
    fun provideYouTubeCaptionService(
        httpClient: HttpClient,
        json: Json,
    ): YouTubeCaptionService = YouTubeCaptionServiceImpl(httpClient, json)

    @Provides
    @Singleton
    fun provideWebScrapeService(
        httpClient: HttpClient,
        json: Json,
    ): WebScrapeService = WebScrapeServiceImpl(httpClient, json)
}

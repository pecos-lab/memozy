package me.pecos.memozy.di

import io.ktor.client.HttpClient
import kotlinx.serialization.json.Json
import me.pecos.memozy.BuildConfig
import me.pecos.memozy.data.datasource.remote.ai.AIApiService
import me.pecos.memozy.data.datasource.remote.ai.AIApiServiceImpl
import me.pecos.memozy.data.datasource.remote.ai.AiConsentChecker
import me.pecos.memozy.feature.core.viewmodel.settings.AiConsentKeys
import me.pecos.memozy.data.datasource.remote.ai.WebScrapeService
import me.pecos.memozy.data.datasource.remote.ai.WebScrapeServiceImpl
import me.pecos.memozy.data.datasource.remote.ai.YouTubeCaptionService
import me.pecos.memozy.data.datasource.remote.ai.YouTubeCaptionServiceImpl
import me.pecos.memozy.data.datasource.remote.ai.createAiHttpClient
import me.pecos.memozy.data.datasource.remote.ai.createYouTubeHttpClient
import me.pecos.memozy.feature.core.viewmodel.settings.PreferencesProvider
import org.koin.core.qualifier.named
import org.koin.dsl.module

val YouTubeHttpClient = named("YouTubeHttpClient")

val aiNetworkModule = module {
    single<Json> {
        Json {
            ignoreUnknownKeys = true
            isLenient = true
            encodeDefaults = true
        }
    }

    single<HttpClient> {
        createAiHttpClient(
            json = get(),
            baseUrl = BuildConfig.WORKER_URL,
            appSecretKey = BuildConfig.APP_SECRET_KEY,
            isDebug = BuildConfig.DEBUG,
        )
    }

    single<HttpClient>(YouTubeHttpClient) { createYouTubeHttpClient() }

    single<AiConsentChecker> {
        val prefs = get<PreferencesProvider>()
        object : AiConsentChecker {
            override fun isConsentGiven(): Boolean =
                prefs.getBoolean(AiConsentKeys.GIVEN, false)
        }
    }
    single<AIApiService> { AIApiServiceImpl(get(), get(), get()) }
    single<YouTubeCaptionService> { YouTubeCaptionServiceImpl(get(), get()) }
    single<WebScrapeService> { WebScrapeServiceImpl(get(), get()) }
}

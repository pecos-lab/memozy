package me.pecos.memozy.di

import io.ktor.client.HttpClient
import kotlinx.serialization.json.Json
import me.pecos.memozy.BuildConfig
import me.pecos.memozy.data.datasource.remote.ai.createAiHttpClient
import me.pecos.memozy.data.datasource.remote.subscription.SubscriptionApiService
import me.pecos.memozy.data.datasource.remote.subscription.SubscriptionApiServiceImpl
import org.koin.core.qualifier.named
import org.koin.dsl.module

val SubscriptionHttpClient = named("SubscriptionHttpClient")

val subscriptionNetworkModule = module {
    // Reuse AI HttpClient config since it uses the same Supabase Edge Functions
    single<HttpClient>(SubscriptionHttpClient) {
        createAiHttpClient(
            json = get(),
            baseUrl = BuildConfig.WORKER_URL,
            appSecretKey = BuildConfig.APP_SECRET_KEY,
            isDebug = BuildConfig.DEBUG,
        )
    }

    single<SubscriptionApiService> {
        SubscriptionApiServiceImpl(
            httpClient = get(SubscriptionHttpClient),
            json = get(),
        )
    }
}

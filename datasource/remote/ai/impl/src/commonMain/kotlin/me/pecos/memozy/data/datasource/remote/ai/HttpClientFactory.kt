package me.pecos.memozy.data.datasource.remote.ai

import io.ktor.client.HttpClient
import io.ktor.client.HttpClientConfig

internal expect fun createPlatformHttpClient(
    config: HttpClientConfig<*>.() -> Unit,
): HttpClient

package me.pecos.memozy.data.datasource.remote.ai

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

@Serializable
private data class ScrapeRequest(val url: String)

@Serializable
private data class ScrapeResponse(
    val title: String? = null,
    val text: String? = null,
    val error: String? = null
)

@Singleton
class WebScrapeServiceImpl @Inject constructor(
    private val httpClient: HttpClient,
    private val json: Json
) : WebScrapeService {

    override suspend fun scrapeWebPage(url: String): WebPageContent? {
        val response: ScrapeResponse = httpClient.post("web-scrape") {
            contentType(ContentType.Application.Json)
            setBody(ScrapeRequest(url))
        }.body()

        if (response.error != null || response.text.isNullOrBlank()) return null

        return WebPageContent(
            title = response.title,
            text = response.text
        )
    }
}

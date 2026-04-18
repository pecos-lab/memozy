package me.pecos.memozy.data.datasource.remote.ai

interface WebScrapeService {
    suspend fun scrapeWebPage(url: String): WebPageContent?
}

data class WebPageContent(
    val title: String?,
    val text: String
)

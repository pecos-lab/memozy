package me.pecos.memozy.feature.memoplain.impl.di

import me.pecos.memozy.feature.memoplain.api.MemoPlainNavigation
import me.pecos.memozy.feature.memoplain.impl.MemoPlainNavigationImpl
import org.koin.dsl.module

val memoPlainModule = module {
    single<MemoPlainNavigation> {
        MemoPlainNavigationImpl(
            repository = get(),
            aiApiService = get(),
            youtubeSummaryDao = get(),
            captionService = get(),
            aiUsageDao = get(),
            webScrapeService = get(),
        )
    }
}

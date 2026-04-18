package me.pecos.memozy.di

import me.pecos.memozy.platform.htmltext.AndroidHtmlTextService
import me.pecos.memozy.platform.htmltext.HtmlTextService
import org.koin.dsl.module

val htmlTextPlatformModule = module {
    single<HtmlTextService> { AndroidHtmlTextService() }
}

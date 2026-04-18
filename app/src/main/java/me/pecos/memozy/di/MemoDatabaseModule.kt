package me.pecos.memozy.di

import me.pecos.memozy.data.datasource.local.AiUsageDao
import me.pecos.memozy.data.datasource.local.CategoryDao
import me.pecos.memozy.data.datasource.local.MEMO_PREPOPULATE_CALLBACK
import me.pecos.memozy.data.datasource.local.MemoDao
import me.pecos.memozy.data.datasource.local.MemoDatabase
import me.pecos.memozy.data.datasource.local.MemoDatabaseFactory
import me.pecos.memozy.data.datasource.local.YoutubeSummaryDao
import me.pecos.memozy.data.datasource.local.chat.ChatMessageDao
import me.pecos.memozy.data.datasource.local.chat.ChatSessionDao
import me.pecos.memozy.data.datasource.local.migration.ALL_MEMO_MIGRATIONS
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val memoDatabaseModule = module {
    single<MemoDatabase> {
        MemoDatabaseFactory(androidContext())
            .create()
            .addMigrations(*ALL_MEMO_MIGRATIONS)
            .addCallback(MEMO_PREPOPULATE_CALLBACK)
            .build()
    }

    single<MemoDao> { get<MemoDatabase>().memoDao() }
    single<CategoryDao> { get<MemoDatabase>().categoryDao() }
    single<ChatSessionDao> { get<MemoDatabase>().chatSessionDao() }
    single<ChatMessageDao> { get<MemoDatabase>().chatMessageDao() }
    single<YoutubeSummaryDao> { get<MemoDatabase>().youtubeSummaryDao() }
    single<AiUsageDao> { get<MemoDatabase>().aiUsageDao() }
}

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
            // TODO(release 전 반드시 제거): v20 schema export 미일치로 인한 schema mismatch 즉사 회피.
            // 정상 빌드 + schemas/20.json 생성된 환경이면 마이그레이션이 정상 작동하므로 이 줄 삭제할 것.
            .fallbackToDestructiveMigration(dropAllTables = true)
            .build()
    }

    single<MemoDao> { get<MemoDatabase>().memoDao() }
    single<CategoryDao> { get<MemoDatabase>().categoryDao() }
    single<ChatSessionDao> { get<MemoDatabase>().chatSessionDao() }
    single<ChatMessageDao> { get<MemoDatabase>().chatMessageDao() }
    single<YoutubeSummaryDao> { get<MemoDatabase>().youtubeSummaryDao() }
    single<AiUsageDao> { get<MemoDatabase>().aiUsageDao() }
}

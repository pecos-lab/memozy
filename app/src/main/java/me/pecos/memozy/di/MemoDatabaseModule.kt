package me.pecos.memozy.di

import me.pecos.memozy.BuildConfig
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
        val builder = MemoDatabaseFactory(androidContext())
            .create()
            .addMigrations(*ALL_MEMO_MIGRATIONS)
            .addCallback(MEMO_PREPOPULATE_CALLBACK)
        // debug 빌드에서만 schema mismatch 시 wipe — release 에 들어가면 실사용자 데이터 손실.
        // v20 schema export 가 KSP 캐시 문제로 누락된 환경 보호용. schemas/20.json 정상 생성이
        // 검증되면 이 분기 자체도 제거할 것.
        if (BuildConfig.DEBUG) {
            builder.fallbackToDestructiveMigration(dropAllTables = true)
        }
        builder.build()
    }

    single<MemoDao> { get<MemoDatabase>().memoDao() }
    single<CategoryDao> { get<MemoDatabase>().categoryDao() }
    single<ChatSessionDao> { get<MemoDatabase>().chatSessionDao() }
    single<ChatMessageDao> { get<MemoDatabase>().chatMessageDao() }
    single<YoutubeSummaryDao> { get<MemoDatabase>().youtubeSummaryDao() }
    single<AiUsageDao> { get<MemoDatabase>().aiUsageDao() }
}

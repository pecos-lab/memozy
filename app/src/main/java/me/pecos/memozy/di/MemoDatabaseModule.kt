package me.pecos.memozy.di

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
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
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object MemoDatabaseModule {

    @Provides
    @Singleton
    fun provideMemoDatabase(@ApplicationContext context: Context): MemoDatabase {
        return MemoDatabaseFactory(context)
            .create()
            .addMigrations(*ALL_MEMO_MIGRATIONS)
            .addCallback(MEMO_PREPOPULATE_CALLBACK)
            .build()
    }

    @Provides
    @Singleton
    fun provideMemoDao(database: MemoDatabase): MemoDao = database.memoDao()

    @Provides
    @Singleton
    fun provideCategoryDao(database: MemoDatabase): CategoryDao = database.categoryDao()

    @Provides
    @Singleton
    fun provideChatSessionDao(database: MemoDatabase): ChatSessionDao = database.chatSessionDao()

    @Provides
    @Singleton
    fun provideChatMessageDao(database: MemoDatabase): ChatMessageDao = database.chatMessageDao()

    @Provides
    @Singleton
    fun provideYoutubeSummaryDao(database: MemoDatabase): YoutubeSummaryDao = database.youtubeSummaryDao()

    @Provides
    @Singleton
    fun provideAiUsageDao(database: MemoDatabase): AiUsageDao = database.aiUsageDao()
}

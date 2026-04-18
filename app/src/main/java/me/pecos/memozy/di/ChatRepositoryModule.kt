package me.pecos.memozy.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import me.pecos.memozy.data.datasource.local.chat.ChatMessageDao
import me.pecos.memozy.data.datasource.local.chat.ChatSessionDao
import me.pecos.memozy.data.repository.chat.ChatRepository
import me.pecos.memozy.data.repository.chat.ChatRepositoryImpl
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object ChatRepositoryModule {

    @Provides
    @Singleton
    fun provideChatRepository(
        sessionDao: ChatSessionDao,
        messageDao: ChatMessageDao,
    ): ChatRepository = ChatRepositoryImpl(sessionDao, messageDao)
}

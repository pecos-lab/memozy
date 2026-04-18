package me.pecos.memozy.di

import me.pecos.memozy.data.repository.chat.ChatRepository
import me.pecos.memozy.data.repository.chat.ChatRepositoryImpl
import org.koin.dsl.module

val chatRepositoryModule = module {
    single<ChatRepository> { ChatRepositoryImpl(get(), get()) }
}

package me.pecos.memozy.data.repository.di

import me.pecos.memozy.data.repository.MemoRepository
import me.pecos.memozy.data.repository.MemoRepositoryImpl
import org.koin.dsl.module

val repositoryModule = module {
    single<MemoRepository> { MemoRepositoryImpl(get()) }
}

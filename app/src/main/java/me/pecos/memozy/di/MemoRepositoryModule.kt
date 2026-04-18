package me.pecos.memozy.di

import me.pecos.memozy.data.repository.MemoRepository
import me.pecos.memozy.data.repository.MemoRepositoryImpl
import org.koin.dsl.module

val memoRepositoryModule = module {
    single<MemoRepository> { MemoRepositoryImpl(get()) }
}

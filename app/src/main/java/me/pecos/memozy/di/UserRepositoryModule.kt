package me.pecos.memozy.di

import me.pecos.memozy.data.repository.user.AuthRepository
import me.pecos.memozy.data.repository.user.AuthRepositoryImpl
import org.koin.dsl.module

val userRepositoryModule = module {
    single<AuthRepository> { AuthRepositoryImpl(get()) }
}

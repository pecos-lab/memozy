package me.pecos.memozy.di

import me.pecos.memozy.data.repository.subscription.SubscriptionRepository
import me.pecos.memozy.data.repository.subscription.SubscriptionRepositoryImpl
import org.koin.dsl.module

val subscriptionRepositoryModule = module {
    single<SubscriptionRepository> { SubscriptionRepositoryImpl(get()) }
}

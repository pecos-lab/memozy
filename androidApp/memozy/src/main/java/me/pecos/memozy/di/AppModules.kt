package me.pecos.memozy.di

import me.pecos.memozy.data.datasource.local.di.databaseModule
import me.pecos.memozy.data.repository.di.repositoryModule
import me.pecos.memozy.feature.memoplain.impl.di.memoPlainModule
import me.pecos.memozy.presentation.di.homeModule

val appModules = listOf(
    databaseModule,
    repositoryModule,
    memoPlainModule,
    homeModule,
)

package me.pecos.memozy.feature.memoplain.impl.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import me.pecos.memozy.feature.memoplain.api.MemoPlainNavigation
import me.pecos.memozy.feature.memoplain.impl.MemoPlainNavigationImpl

@Module
@InstallIn(SingletonComponent::class)
abstract class MemoPlainModule {

    @Binds
    abstract fun bindMemoPlainNavigation(
        impl: MemoPlainNavigationImpl
    ): MemoPlainNavigation
}

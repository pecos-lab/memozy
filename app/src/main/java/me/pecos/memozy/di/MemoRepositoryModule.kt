package me.pecos.memozy.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import me.pecos.memozy.data.datasource.local.MemoDao
import me.pecos.memozy.data.repository.MemoRepository
import me.pecos.memozy.data.repository.MemoRepositoryImpl
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object MemoRepositoryModule {

    @Provides
    @Singleton
    fun provideMemoRepository(memoDao: MemoDao): MemoRepository =
        MemoRepositoryImpl(memoDao)
}

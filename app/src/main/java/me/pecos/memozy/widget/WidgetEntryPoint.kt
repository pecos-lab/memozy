package me.pecos.memozy.widget

import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import me.pecos.memozy.data.repository.MemoRepository

@EntryPoint
@InstallIn(SingletonComponent::class)
interface WidgetEntryPoint {
    fun memoRepository(): MemoRepository
}

package me.pecos.memozy.shared.umbrella

import me.pecos.memozy.data.datasource.local.MemoDatabase
import me.pecos.memozy.data.datasource.local.MemoDatabaseFactory
import me.pecos.memozy.data.repository.MemoRepository
import me.pecos.memozy.data.repository.MemoRepositoryImpl
import me.pecos.memozy.feature.core.viewmodel.MainViewModel
import me.pecos.memozy.feature.core.viewmodel.TrashViewModel
import org.koin.core.context.startKoin
import org.koin.core.module.Module
import org.koin.dsl.module
import org.koin.mp.KoinPlatform

val sharedModule: Module = module {
    single { MemoDatabaseFactory() }
    single<MemoDatabase> { get<MemoDatabaseFactory>().create().build() }
    single { get<MemoDatabase>().memoDao() }
    single<MemoRepository> { MemoRepositoryImpl(get()) }
    factory { MainViewModel(get()) }
    factory { TrashViewModel(get()) }
}

fun initKoin() {
    startKoin {
        modules(sharedModule)
    }
}

fun provideMainViewModel(): MainViewModel = KoinPlatform.getKoin().get()
fun provideTrashViewModel(): TrashViewModel = KoinPlatform.getKoin().get()

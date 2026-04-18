package me.pecos.memozy.shared.umbrella

import me.pecos.memozy.data.repository.MemoRepository
import me.pecos.memozy.feature.core.viewmodel.MainViewModel
import org.koin.core.context.startKoin
import org.koin.core.module.Module
import org.koin.dsl.module
import org.koin.mp.KoinPlatform

val sharedModule: Module = module {
    single<MemoRepository> { InMemoryMemoRepository() }
    factory { MainViewModel(get()) }
}

// iOS 앱 진입점에서 호출. Android는 기존 MemozyApplication.onCreate()에서 별도 초기화 유지.
// #221 Koin 마이그레이션이 commonMain DI를 완성하면, Android도 이 진입점으로 수렴시킬 수 있다.
fun initKoin() {
    startKoin {
        modules(sharedModule)
    }
}

// Swift에서 MainViewModel을 한 줄로 꺼내기 위한 헬퍼.
// Koin iOS에는 `by viewModel()` DSL이 없으므로 factory로 꺼낸다.
fun provideMainViewModel(): MainViewModel = KoinPlatform.getKoin().get()

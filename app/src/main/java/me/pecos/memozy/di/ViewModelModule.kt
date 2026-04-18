package me.pecos.memozy.di

import me.pecos.memozy.feature.core.viewmodel.MainViewModel
import me.pecos.memozy.feature.core.viewmodel.TrashViewModel
import me.pecos.memozy.presentation.screen.settings.SettingsViewModel
import me.pecos.memozy.worker.BackupWorker
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.workmanager.dsl.worker
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val viewModelModule = module {
    viewModel { MainViewModel(get()) }
    viewModel { TrashViewModel(get()) }
    viewModel {
        SettingsViewModel(
            context = androidContext(),
            repository = get(),
            memoDao = get(),
            authRepository = get(),
            backupRepository = get(),
        )
    }

    worker { BackupWorker(get(), get()) }
}

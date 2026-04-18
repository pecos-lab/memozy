package me.pecos.memozy.di

import me.pecos.memozy.feature.core.viewmodel.MainViewModel
import me.pecos.memozy.feature.core.viewmodel.SettingsViewModel
import me.pecos.memozy.feature.core.viewmodel.TrashViewModel
import me.pecos.memozy.feature.core.viewmodel.settings.AndroidFileUriBridge
import me.pecos.memozy.feature.core.viewmodel.settings.FileUriBridge
import me.pecos.memozy.feature.core.viewmodel.settings.PreferencesProvider
import me.pecos.memozy.feature.core.viewmodel.settings.SharedPreferencesProvider
import me.pecos.memozy.worker.BackupWorker
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.workmanager.dsl.worker
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val viewModelModule = module {
    single<PreferencesProvider> { SharedPreferencesProvider(androidContext()) }
    single<FileUriBridge> { AndroidFileUriBridge(androidContext()) }

    viewModel { MainViewModel(get()) }
    viewModel { TrashViewModel(get()) }
    viewModel {
        SettingsViewModel(
            preferences = get(),
            fileUriBridge = get(),
            repository = get(),
            memoDao = get(),
            authRepository = get(),
            backupRepository = get(),
        )
    }

    worker { BackupWorker(get(), get()) }
}

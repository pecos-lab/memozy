package me.pecos.memozy.presentation.di

import me.pecos.memozy.presentation.screen.home.MainViewModel
import me.pecos.memozy.presentation.screen.settings.SettingsViewModel
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val homeModule = module {
    viewModel { MainViewModel(get()) }
    viewModel { SettingsViewModel(androidContext(), get()) }
}

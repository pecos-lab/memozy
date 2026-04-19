package me.pecos.memozy.shared.umbrella

import me.pecos.memozy.data.backup.BackupRepository
import me.pecos.memozy.data.datasource.local.MemoDatabase
import me.pecos.memozy.data.datasource.local.MemoDatabaseFactory
import me.pecos.memozy.data.datasource.remote.auth.AuthService
import me.pecos.memozy.data.repository.MemoRepository
import me.pecos.memozy.data.repository.MemoRepositoryImpl
import me.pecos.memozy.data.repository.user.AuthRepository
import me.pecos.memozy.data.repository.user.AuthRepositoryImpl
import me.pecos.memozy.feature.core.viewmodel.MainViewModel
import me.pecos.memozy.feature.core.viewmodel.SettingsViewModel
import me.pecos.memozy.feature.core.viewmodel.TrashViewModel
import me.pecos.memozy.feature.core.viewmodel.settings.FileUriBridge
import me.pecos.memozy.feature.core.viewmodel.settings.IosFileUriBridge
import me.pecos.memozy.feature.core.viewmodel.settings.NSUserDefaultsPreferencesProvider
import me.pecos.memozy.feature.core.viewmodel.settings.PreferencesProvider
import me.pecos.memozy.platform.ads.AdsService
import me.pecos.memozy.platform.ads.IosAdsService
import me.pecos.memozy.platform.credential.CredentialService
import me.pecos.memozy.platform.credential.IosCredentialService
import me.pecos.memozy.platform.intent.AppInfo
import me.pecos.memozy.platform.intent.AppRestarter
import me.pecos.memozy.platform.intent.ClipboardService
import me.pecos.memozy.platform.intent.HapticService
import me.pecos.memozy.platform.intent.IosAppInfo
import me.pecos.memozy.platform.intent.IosAppRestarter
import me.pecos.memozy.platform.intent.IosClipboardService
import me.pecos.memozy.platform.intent.IosHapticService
import me.pecos.memozy.platform.intent.IosPermissionService
import me.pecos.memozy.platform.intent.IosSharedContentReader
import me.pecos.memozy.platform.intent.IosSharer
import me.pecos.memozy.platform.intent.IosToastPresenter
import me.pecos.memozy.platform.intent.IosUrlLauncher
import me.pecos.memozy.platform.intent.PermissionService
import me.pecos.memozy.platform.intent.SharedContentReader
import me.pecos.memozy.platform.intent.Sharer
import me.pecos.memozy.platform.intent.ToastPresenter
import me.pecos.memozy.platform.intent.UrlLauncher
import me.pecos.memozy.shared.umbrella.stub.IosStubAuthService
import me.pecos.memozy.shared.umbrella.stub.IosStubBackupRepository
import org.koin.core.context.startKoin
import org.koin.core.module.Module
import org.koin.dsl.module
import org.koin.mp.KoinPlatform

val sharedModule: Module = module {
    // Local DB
    single { MemoDatabaseFactory() }
    single<MemoDatabase> { get<MemoDatabaseFactory>().create().build() }
    single { get<MemoDatabase>().memoDao() }

    // Repositories
    single<MemoRepository> { MemoRepositoryImpl(get()) }

    // Preferences + file bridge
    single<PreferencesProvider> { NSUserDefaultsPreferencesProvider() }
    single<FileUriBridge> { IosFileUriBridge() }

    // Platform intent (iOS actuals)
    single<UrlLauncher> { IosUrlLauncher() }
    single<AppInfo> { IosAppInfo() }
    single<ClipboardService> { IosClipboardService() }
    single<HapticService> { IosHapticService() }
    single<Sharer> { IosSharer() }
    single<ToastPresenter> { IosToastPresenter() }
    single<AppRestarter> { IosAppRestarter() }
    single<PermissionService> { IosPermissionService() }
    single<SharedContentReader> { IosSharedContentReader() }

    // Credential
    single<CredentialService> { IosCredentialService() }

    // Ads (iOS no-op — 이슈 #280 옵션 A)
    single<AdsService> { IosAdsService() }

    // Stubs (Supabase 연결 완료 전까지 임시)
    single<AuthService> { IosStubAuthService() }
    single<AuthRepository> { AuthRepositoryImpl(get()) }
    single<BackupRepository> { IosStubBackupRepository() }

    // ViewModels
    factory { MainViewModel(get()) }
    factory { TrashViewModel(get()) }
    factory {
        SettingsViewModel(
            preferences = get(),
            fileUriBridge = get(),
            repository = get(),
            memoDao = get(),
            authRepository = get(),
            backupRepository = get(),
        )
    }
}

fun initKoin() {
    startKoin {
        modules(sharedModule)
    }
}

fun provideMainViewModel(): MainViewModel = KoinPlatform.getKoin().get()
fun provideTrashViewModel(): TrashViewModel = KoinPlatform.getKoin().get()
fun provideSettingsViewModel(): SettingsViewModel = KoinPlatform.getKoin().get()

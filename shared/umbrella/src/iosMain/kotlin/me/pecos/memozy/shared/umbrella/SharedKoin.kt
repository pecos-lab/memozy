package me.pecos.memozy.shared.umbrella

import io.ktor.client.HttpClient
import kotlinx.serialization.json.Json
import me.pecos.memozy.data.backup.BackupRepository
import me.pecos.memozy.data.datasource.local.AiUsageDao
import me.pecos.memozy.data.datasource.local.MemoDatabase
import me.pecos.memozy.data.datasource.local.MemoDatabaseFactory
import me.pecos.memozy.data.datasource.local.YoutubeSummaryDao
import me.pecos.memozy.data.datasource.remote.ai.AIApiService
import me.pecos.memozy.data.datasource.remote.ai.AIApiServiceImpl
import me.pecos.memozy.data.datasource.remote.ai.WebScrapeService
import me.pecos.memozy.data.datasource.remote.ai.WebScrapeServiceImpl
import me.pecos.memozy.data.datasource.remote.ai.YouTubeCaptionService
import me.pecos.memozy.data.datasource.remote.ai.YouTubeCaptionServiceImpl
import me.pecos.memozy.data.datasource.remote.ai.createAiHttpClient
import me.pecos.memozy.data.datasource.remote.ai.createYouTubeHttpClient
import me.pecos.memozy.data.datasource.remote.auth.AuthService
import me.pecos.memozy.data.repository.MemoRepository
import me.pecos.memozy.data.repository.MemoRepositoryImpl
import me.pecos.memozy.data.repository.user.AuthRepository
import me.pecos.memozy.data.repository.user.AuthRepositoryImpl
import me.pecos.memozy.feature.core.viewmodel.MainViewModel
import me.pecos.memozy.feature.core.viewmodel.SettingsViewModel
import me.pecos.memozy.feature.core.viewmodel.TrashViewModel
import me.pecos.memozy.feature.memoplain.api.MemoPlainNavigation
import me.pecos.memozy.feature.memoplain.impl.di.memoPlainModule
import me.pecos.memozy.feature.core.viewmodel.settings.FileUriBridge
import me.pecos.memozy.feature.core.viewmodel.settings.IosFileUriBridge
import me.pecos.memozy.feature.core.viewmodel.settings.NSUserDefaultsPreferencesProvider
import me.pecos.memozy.feature.core.viewmodel.settings.PreferencesProvider
import me.pecos.memozy.platform.ads.AdsService
import me.pecos.memozy.platform.ads.IosAdsService
import me.pecos.memozy.platform.credential.CredentialService
import me.pecos.memozy.platform.credential.IosCredentialService
import me.pecos.memozy.platform.media.AudioFileStore
import me.pecos.memozy.platform.media.IosAudioFileStore
import me.pecos.memozy.platform.media.IosMediaService
import me.pecos.memozy.platform.media.MediaService
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
import org.koin.core.qualifier.named
import org.koin.dsl.module
import org.koin.mp.KoinPlatform

val YouTubeHttpClient = named("YouTubeHttpClient")

val sharedModule: Module = module {
    // Local DB
    single { MemoDatabaseFactory() }
    single<MemoDatabase> { get<MemoDatabaseFactory>().create().build() }
    single { get<MemoDatabase>().memoDao() }
    single<AiUsageDao> { get<MemoDatabase>().aiUsageDao() }
    single<YoutubeSummaryDao> { get<MemoDatabase>().youtubeSummaryDao() }

    // Repositories
    single<MemoRepository> { MemoRepositoryImpl(get()) }

    // Audio file store (iOS NSFileManager 기반)
    single<AudioFileStore> { IosAudioFileStore() }

    // MediaService — iOS no-op stub. AVAudioPlayer/Recorder 실구현은 후속 PR.
    single<MediaService> { IosMediaService() }

    // AI / Web / YouTube — iOS 에서 secrets 파이프 미정. baseUrl/appSecretKey 빈 값 주입 →
    // DI 는 성공하나 실제 AI 호출은 401/실패. secrets 통합은 #276 후속 PR (Wave 3-F) 스코프.
    single<Json> {
        Json {
            ignoreUnknownKeys = true
            isLenient = true
            encodeDefaults = true
        }
    }
    single<HttpClient> {
        createAiHttpClient(
            json = get(),
            baseUrl = "",
            appSecretKey = "",
            isDebug = true,
        )
    }
    single<HttpClient>(YouTubeHttpClient) { createYouTubeHttpClient() }
    single<AIApiService> { AIApiServiceImpl(get(), get()) }
    single<YouTubeCaptionService> { YouTubeCaptionServiceImpl(get(), get()) }
    single<WebScrapeService> { WebScrapeServiceImpl(get(), get()) }

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
        modules(sharedModule, memoPlainModule)
    }
}

fun provideMainViewModel(): MainViewModel = KoinPlatform.getKoin().get()
fun provideTrashViewModel(): TrashViewModel = KoinPlatform.getKoin().get()
fun provideSettingsViewModel(): SettingsViewModel = KoinPlatform.getKoin().get()
fun provideMemoPlainNavigation(): MemoPlainNavigation = KoinPlatform.getKoin().get()

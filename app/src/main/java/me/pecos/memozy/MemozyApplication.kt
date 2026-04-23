package me.pecos.memozy

import android.app.Application
import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Intent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import me.pecos.memozy.data.repository.MemoRepository
import me.pecos.memozy.widget.MemoWidgetReceiver
import me.pecos.memozy.di.adsPlatformModule
import me.pecos.memozy.di.aiNetworkModule
import me.pecos.memozy.di.authModule
import me.pecos.memozy.di.backupModule
import me.pecos.memozy.di.billingPlatformModule
import me.pecos.memozy.di.chatRepositoryModule
import me.pecos.memozy.di.credentialPlatformModule
import me.pecos.memozy.di.htmlTextPlatformModule
import me.pecos.memozy.di.mediaPlatformModule
import me.pecos.memozy.di.memoDatabaseModule
import me.pecos.memozy.di.memoRepositoryModule
import me.pecos.memozy.di.userRepositoryModule
import me.pecos.memozy.di.viewModelModule
import me.pecos.memozy.feature.memoplain.impl.di.memoPlainModule
import me.pecos.memozy.platform.ads.AdsService
import me.pecos.memozy.platform.intent.di.platformIntentModule
import org.koin.android.ext.android.inject
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.workmanager.koin.workManagerFactory
import org.koin.core.context.startKoin

class MemozyApplication : Application() {

    private val appScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@MemozyApplication)
            workManagerFactory()
            modules(
                memoDatabaseModule,
                aiNetworkModule,
                authModule,
                backupModule,
                memoRepositoryModule,
                chatRepositoryModule,
                userRepositoryModule,
                viewModelModule,
                memoPlainModule,
                billingPlatformModule,
                adsPlatformModule,
                credentialPlatformModule,
                mediaPlatformModule,
                htmlTextPlatformModule,
                platformIntentModule(),
            )
        }
        val adsService: AdsService by inject()
        appScope.launch {
            adsService.initialize()
        }

        // 메모 DB 변경 시 홈 위젯 갱신 — system broadcast 단일 경로 사용
        // distinctUntilChanged로 동일 상태 연속 emit 차단 (저장 중 Room이 여러 번 emit 하는 케이스 방어)
        val memoRepository: MemoRepository by inject()
        appScope.launch {
            memoRepository.getMemos()
                .distinctUntilChanged { old, new ->
                    old.size == new.size &&
                        old.zip(new).all { (a, b) -> a.id == b.id && a.updatedAt == b.updatedAt }
                }
                .collect {
                    val appWidgetManager = AppWidgetManager.getInstance(this@MemozyApplication)
                    val ids = appWidgetManager.getAppWidgetIds(
                        ComponentName(this@MemozyApplication, MemoWidgetReceiver::class.java)
                    )
                    if (ids.isNotEmpty()) {
                        val intent = Intent(this@MemozyApplication, MemoWidgetReceiver::class.java).apply {
                            action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
                            putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids)
                        }
                        sendBroadcast(intent)
                    }
                }
        }
    }
}

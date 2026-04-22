package me.pecos.memozy

import android.app.Application
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
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
import me.pecos.memozy.di.subscriptionNetworkModule
import me.pecos.memozy.di.subscriptionRepositoryModule
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
                subscriptionNetworkModule,
                subscriptionRepositoryModule,
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
    }
}

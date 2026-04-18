package me.pecos.memozy

import android.app.Application
import androidx.work.Configuration
import androidx.work.WorkerFactory
import com.google.android.gms.ads.MobileAds
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import me.pecos.memozy.di.aiNetworkModule
import me.pecos.memozy.di.authModule
import me.pecos.memozy.di.backupModule
import me.pecos.memozy.di.chatRepositoryModule
import me.pecos.memozy.di.memoDatabaseModule
import me.pecos.memozy.di.memoRepositoryModule
import me.pecos.memozy.di.userRepositoryModule
import me.pecos.memozy.di.viewModelModule
import me.pecos.memozy.feature.memoplain.impl.di.memoPlainModule
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.workmanager.koin.workManagerFactory
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.koin.core.context.startKoin

class MemozyApplication : Application(), Configuration.Provider, KoinComponent {

    private val workerFactory: WorkerFactory by inject()

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()

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
            )
        }
        CoroutineScope(Dispatchers.IO).launch {
            MobileAds.initialize(this@MemozyApplication)
        }
    }
}

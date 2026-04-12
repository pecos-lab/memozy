package me.pecos.memozy

import android.app.Application
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import dagger.hilt.android.HiltAndroidApp
import me.pecos.memozy.data.datasource.remote.auth.AuthService
import me.pecos.memozy.data.datasource.remote.auth.AuthState
import me.pecos.memozy.worker.BackupScheduler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltAndroidApp
class MemozyApplication : Application(), Configuration.Provider {

    @Inject lateinit var workerFactory: HiltWorkerFactory
    @Inject lateinit var authService: AuthService

    private val appScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()

    override fun onCreate() {
        super.onCreate()

        // 로그인 상태 변경 시 자동 백업 스케줄링
        appScope.launch {
            authService.authState.collectLatest { state ->
                when (state) {
                    is AuthState.Authenticated -> BackupScheduler.schedule(this@MemozyApplication)
                    is AuthState.Unauthenticated -> BackupScheduler.cancel(this@MemozyApplication)
                    else -> {}
                }
            }
        }
    }
}

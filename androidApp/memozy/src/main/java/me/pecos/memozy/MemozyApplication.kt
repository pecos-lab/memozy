package me.pecos.memozy

import android.app.Application
import me.pecos.memozy.di.appModules
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class MemozyApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@MemozyApplication)
            modules(appModules)
        }
    }
}

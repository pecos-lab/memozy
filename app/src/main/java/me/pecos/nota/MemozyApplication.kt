package me.pecos.nota

import android.app.Application
import dagger.hilt.android.HiltAndroidApp
import net.danlew.android.joda.JodaTimeAndroid

@HiltAndroidApp
class MemozyApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        JodaTimeAndroid.init(this)
    }
}

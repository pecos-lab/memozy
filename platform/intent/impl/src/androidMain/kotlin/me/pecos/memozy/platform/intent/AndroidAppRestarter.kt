package me.pecos.memozy.platform.intent

import android.app.Activity
import android.os.Handler
import android.os.Looper

/**
 * 현재 Activity 를 얻기 위해 홀더 패턴 사용. main/MainActivity 에서
 * onCreate 시 provider 를 등록.
 */
class AndroidAppRestarter(
    private val activityProvider: () -> Activity?,
) : AppRestarter {
    override fun restart() {
        val activity = activityProvider() ?: return
        Handler(Looper.getMainLooper()).postDelayed({
            @Suppress("DEPRECATION")
            activity.overridePendingTransition(0, 0)
            activity.recreate()
        }, 200)
    }
}

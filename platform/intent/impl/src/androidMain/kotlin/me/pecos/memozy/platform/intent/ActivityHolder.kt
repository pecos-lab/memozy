package me.pecos.memozy.platform.intent

import android.app.Activity
import java.lang.ref.WeakReference

/**
 * Koin 에 등록된 싱글톤 서비스들이 현재 Activity 를 필요로 할 때 참조하는 홀더.
 * MainActivity 가 onCreate/onDestroy 에서 set/clear 해준다.
 */
object ActivityHolder {
    private var ref: WeakReference<Activity>? = null

    fun set(activity: Activity) {
        ref = WeakReference(activity)
    }

    fun clear() {
        ref = null
    }

    fun current(): Activity? = ref?.get()
}

package me.pecos.memozy.platform.intent

import android.app.Activity
import android.content.Intent
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat

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
            // recreate() 만으로는 Compose 리소스 로케일이 갱신 안 되는 케이스가 있어 (특히 ComponentActivity)
            // 런처 인텐트로 새 태스크 시작 + 현재 Activity 종료 = 사실상 풀 재시작. "껏다키기"와 동등.
            val intent = activity.packageManager.getLaunchIntentForPackage(activity.packageName)
            if (intent != null) {
                intent.addFlags(
                    Intent.FLAG_ACTIVITY_CLEAR_TOP or
                        Intent.FLAG_ACTIVITY_NEW_TASK or
                        Intent.FLAG_ACTIVITY_CLEAR_TASK
                )
                activity.startActivity(intent)
                @Suppress("DEPRECATION")
                activity.overridePendingTransition(0, 0)
                activity.finish()
            } else {
                activity.recreate()
            }
        }, 200)
    }

    override fun applyAppLanguage(code: String) {
        // 즉시 반영은 MainActivity 의 LocalConfiguration override 가 담당.
        // 여기서는 시스템 per-app locale 만 등록 — 시스템 설정·다음 cold start 일관성용. restart() 호출 안 함.
        AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags(code))
    }
}

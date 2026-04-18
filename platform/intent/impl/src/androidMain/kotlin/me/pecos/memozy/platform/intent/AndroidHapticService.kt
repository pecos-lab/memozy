package me.pecos.memozy.platform.intent

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager

class AndroidHapticService(
    private val context: Context,
) : HapticService {
    // Vibrator 경로라 view.performHapticFeedback 과 달리 system haptic 설정을
    // 직접 존중하지는 않는다. 정확한 UX 매칭은 실사용처 교체 PR 에서 LocalHapticFeedback
    // 기반으로 다시 재단할 여지가 있음.
    private val vibrator: Vibrator? by lazy {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val manager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
            manager?.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
        }
    }

    override fun perform(kind: HapticKind) {
        val v = vibrator?.takeIf { it.hasVibrator() } ?: return
        val millis = when (kind) {
            HapticKind.KeyboardTap -> 10L
            HapticKind.ContextClick -> 15L
            HapticKind.LongPress -> 30L
            HapticKind.ConfirmSuccess -> 40L
            HapticKind.RejectError -> 60L
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            v.vibrate(VibrationEffect.createOneShot(millis, VibrationEffect.DEFAULT_AMPLITUDE))
        } else {
            @Suppress("DEPRECATION")
            v.vibrate(millis)
        }
    }
}

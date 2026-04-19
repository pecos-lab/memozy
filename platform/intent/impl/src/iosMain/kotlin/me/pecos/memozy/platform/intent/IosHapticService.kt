package me.pecos.memozy.platform.intent

import platform.UIKit.UIImpactFeedbackGenerator
import platform.UIKit.UIImpactFeedbackStyle
import platform.UIKit.UINotificationFeedbackGenerator
import platform.UIKit.UINotificationFeedbackType

class IosHapticService : HapticService {
    override fun perform(kind: HapticKind) {
        when (kind) {
            HapticKind.KeyboardTap -> impact(UIImpactFeedbackStyle.UIImpactFeedbackStyleLight)
            HapticKind.ContextClick -> impact(UIImpactFeedbackStyle.UIImpactFeedbackStyleLight)
            HapticKind.LongPress -> impact(UIImpactFeedbackStyle.UIImpactFeedbackStyleMedium)
            HapticKind.ConfirmSuccess -> notify(UINotificationFeedbackType.UINotificationFeedbackTypeSuccess)
            HapticKind.RejectError -> notify(UINotificationFeedbackType.UINotificationFeedbackTypeError)
        }
    }

    private fun impact(style: UIImpactFeedbackStyle) {
        val generator = UIImpactFeedbackGenerator(style)
        generator.prepare()
        generator.impactOccurred()
    }

    private fun notify(type: UINotificationFeedbackType) {
        val generator = UINotificationFeedbackGenerator()
        generator.prepare()
        generator.notificationOccurred(type)
    }
}

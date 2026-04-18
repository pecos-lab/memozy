package me.pecos.memozy.platform.intent

// TODO(C-1): UIImpactFeedbackGenerator / UINotificationFeedbackGenerator 매핑.
class IosHapticService : HapticService {
    override fun perform(kind: HapticKind) {
        println("[platform-intent] IosHapticService.perform stub: $kind")
    }
}

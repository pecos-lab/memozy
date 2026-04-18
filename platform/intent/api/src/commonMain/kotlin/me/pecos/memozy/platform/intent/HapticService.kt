package me.pecos.memozy.platform.intent

enum class HapticKind {
    KeyboardTap,
    LongPress,
    ContextClick,
    ConfirmSuccess,
    RejectError,
}

interface HapticService {
    fun perform(kind: HapticKind)
}

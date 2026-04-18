package me.pecos.memozy.platform.intent

// TODO(C-1): iOS 에는 네이티브 Toast 가 없음. SwiftUI 셸 쪽에 SnackBar/overlay 를 두고
// 해당 host 에 이벤트를 흘리는 방식(C-1 세션에서 확정)으로 구현 예정.
class IosToastPresenter : ToastPresenter {
    override fun show(text: String, duration: ToastDuration) {
        println("[platform-intent] IosToastPresenter.show stub ($duration): $text")
    }
}

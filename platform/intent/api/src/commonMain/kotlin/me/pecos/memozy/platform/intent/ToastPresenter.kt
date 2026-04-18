package me.pecos.memozy.platform.intent

enum class ToastDuration { Short, Long }

interface ToastPresenter {
    fun show(text: String, duration: ToastDuration = ToastDuration.Short)
}

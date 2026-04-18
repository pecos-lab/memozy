package me.pecos.memozy.platform.intent

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.widget.Toast

class AndroidToastPresenter(
    private val context: Context,
) : ToastPresenter {
    private val mainHandler = Handler(Looper.getMainLooper())

    override fun show(text: String, duration: ToastDuration) {
        val androidDuration = when (duration) {
            ToastDuration.Short -> Toast.LENGTH_SHORT
            ToastDuration.Long -> Toast.LENGTH_LONG
        }
        if (Looper.myLooper() == Looper.getMainLooper()) {
            Toast.makeText(context, text, androidDuration).show()
        } else {
            mainHandler.post { Toast.makeText(context, text, androidDuration).show() }
        }
    }
}

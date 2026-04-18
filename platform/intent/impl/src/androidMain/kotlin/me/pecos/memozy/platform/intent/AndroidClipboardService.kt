package me.pecos.memozy.platform.intent

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context

class AndroidClipboardService(
    private val context: Context,
) : ClipboardService {
    private val manager: ClipboardManager?
        get() = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager

    override fun copyPlainText(label: String, text: String) {
        manager?.setPrimaryClip(ClipData.newPlainText(label, text))
    }

    override fun readPrimaryText(): String? =
        manager?.primaryClip?.takeIf { it.itemCount > 0 }
            ?.getItemAt(0)
            ?.coerceToText(context)
            ?.toString()
}

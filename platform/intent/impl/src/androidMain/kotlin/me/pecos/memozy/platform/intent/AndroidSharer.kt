package me.pecos.memozy.platform.intent

import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import java.io.File

class AndroidSharer(
    private val context: Context,
) : Sharer {
    override fun shareText(text: String, chooserTitle: String?) {
        val send = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, text)
        }
        launchChooser(send, chooserTitle)
    }

    override fun shareFile(path: String, mimeType: String, chooserTitle: String?) {
        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            File(path),
        )
        val send = Intent(Intent.ACTION_SEND).apply {
            type = mimeType
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        launchChooser(send, chooserTitle)
    }

    private fun launchChooser(send: Intent, chooserTitle: String?) {
        val chooser = Intent.createChooser(send, chooserTitle)
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(chooser)
    }
}
